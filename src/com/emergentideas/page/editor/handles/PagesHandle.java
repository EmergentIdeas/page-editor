package com.emergentideas.page.editor.handles;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emergentideas.page.editor.interfaces.PageDataLoader;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.bootstrap.ConfigurationAtom;
import com.emergentideas.webhandle.bootstrap.Integrate;
import com.emergentideas.webhandle.bootstrap.Integrator;
import com.emergentideas.webhandle.bootstrap.Loader;
import com.emergentideas.webhandle.exceptions.CouldNotHandle;
import com.emergentideas.webhandle.exceptions.UnauthorizedAccessException;
import com.emergentideas.webhandle.exceptions.UserRequiredException;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResource;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.templates.TemplateInstance;
import com.emergentideas.webhandle.templates.TemplateSource;
import com.emergentideas.webhandle.templates.TripartateTemplate;

@Integrate
public class PagesHandle implements Integrator {
	
	/**
	 * The name of the sink to write changes to.
	 */
	protected String sinkName;
	
	/**
	 * The character set used in converting a string to bytes for the disk.
	 */
	protected String characterSet = "UTF-8";
	
	/**
	 * Will only show the edit bar and allow changes from users in the following group
	 */
	protected String roleToEdit = "page-editors";
	
	/**
	 * Any request parameter prefixed with this string is assumed to be a key value pair for the template
	 * data where the portion of the parameter name following the prefix is the key and the value of the
	 * parameter is the data parameter.
	 */
	protected String dataParameterPrefix = "template_data_";
	
	
	protected String defaultComplexPropertiesTemplateName = "page-editor/complex-page-properties";
	
	protected String complexPropertiesTemplateProperty = "complexPagePropertiesTemplate";
	
	protected List<PageDataLoader> pageDataLoaders = Collections.synchronizedList(new ArrayList<PageDataLoader>());
	
	@GET
	@Path("/{page:.+}")
	@Template
	public Object serve(Location location, String page, HttpServletRequest request) throws IOException {
		
		if(isAllowedPageName(page)) {
			TemplateInstance template = findTemplate(location, page);
			
			if(template != null) {
				
				if(shouldEdit(request)) {
					TripartateTemplate tt = (TripartateTemplate)template;
					List<String> sections = getEditableSectionNames(tt);
					location.put("editableSectionNames", sections);
					location.put("pageIsEditable", true);
					location.put("editableSubmitAction", "/" + page);
					location.put("editablePageTitle", getTitle(tt));
					
					Map<String, String> templateData = tt.getTemplateData();
					if(templateData.containsKey(complexPropertiesTemplateProperty) == false) {
						// If the page is going to be edited and doesn't specify a template to be used for
						// the complex page editing tasks like included slideshow, etc. then add the default
						// template name (which exists but may not have any content)
						location.put(complexPropertiesTemplateProperty, defaultComplexPropertiesTemplateName);
					}
					
					addMetaToLocation(location, tt);
				}
				
				invokePageDataLoaders(request, location, template, page);
				
				return page;
			}
		}
		
		
		return new CouldNotHandle() {};
	}
	
	protected boolean shouldEdit(HttpServletRequest request) {
		return request.isUserInRole(getRoleToEdit());
	}
	
	protected String getTitle(TripartateTemplate tt) {
		String s = tt.getSections().get("title");
		if(s == null) {
			return "";
		}
		return s;
	}
	
	protected void addMetaToLocation(Location location, TripartateTemplate tt) throws IOException {
		String meta = tt.getSections().get("namedMeta");
		Properties prop = new Properties();
		if(StringUtils.isNotBlank(meta)) {
			prop.load(new StringReader(meta));
			addIfNotBlank(location, "editablePageKeywords", "keywords", prop);
			addIfNotBlank(location, "editablePageDescription", "description", prop);
		}
	}
	
	protected void invokePageDataLoaders(HttpServletRequest request, Location location, TemplateInstance template, String page) {
		for(PageDataLoader loader : pageDataLoaders) {
			loader.loadData(request, location, template, page);
		}
	}
	
	protected void addIfNotBlank(Location location, String locationName, String propName, Properties prop) {
		String value = prop.getProperty(propName);
		if(StringUtils.isNotBlank(value)) {
			location.put(locationName, value);
		}
	}
	
	
	@POST
	@Path("/{page:.+}")
	@Template
	public Object change(Location location, String page, String editablePageTitle, String editablePageKeywords, String editablePageDescription, 
			HttpServletRequest request, String Referer) throws Exception {
		if(isAllowedPageName(page)) {
			TemplateInstance ti = findTemplate(location, page);
			if(ti instanceof TripartateTemplate) {
				if(shouldEdit(request) == false) {
					if(request.getUserPrincipal() == null) {
						throw new UserRequiredException();
					}
					else {
						throw new UnauthorizedAccessException();
					}
				}
				TripartateTemplate tt = (TripartateTemplate)ti;
				
				StreamableResourceSink sink = findSink(location);
				
				// Change any of the meta or title info that has been submitted
				if(StringUtils.isNotBlank(editablePageTitle)) {
					String filename = page + ".title";
					Resource r = sink.get(filename);
					sink.write(filename, editablePageTitle.getBytes(characterSet));
				}
				
				if(StringUtils.isNotBlank(editablePageKeywords) || StringUtils.isNotBlank(editablePageDescription)) {
					String filename = page + ".namedMeta";
					Resource r = sink.get(filename);
					if(r == null || r instanceof StreamableResource) {
						Properties namedMeta = new Properties();
						if(r != null) {
							namedMeta.load(((StreamableResource)r).getContent());
						}
						
						if(StringUtils.isNotBlank(editablePageKeywords)) {
							namedMeta.put("keywords", editablePageKeywords);
						}
						if(StringUtils.isNotBlank(editablePageDescription)) {
							namedMeta.put("description", editablePageDescription);
						}
						
						StringWriter sw = new StringWriter();
						namedMeta.store(sw, null);
						
						sink.write(filename, sw.toString().getBytes(characterSet));
					}
				}
				
				saveTemplateDataSubmissions(request, tt, sink, page);
				
				// Change any of the body segments which have but submitted
				String bodyText = tt.getSections().get("body");
				
				Document doc = Jsoup.parse(bodyText);
				Elements inlineSections = doc.select(".edit-content-inline");
				for(int sectionIndex = 0; sectionIndex < inlineSections.size(); sectionIndex++) {
					Element section = inlineSections.get(sectionIndex);
					String parmName = getParameterName(section);
					String value = request.getParameter(parmName);
					
					if(value != null && value.length() > 0) {
						replaceContent(section, value);
						
						String newContent = doc.select("body").html();
						
						String filename = page + ".body";
						Resource r = sink.get(filename);
						if(r != null) {
							sink.write(filename, newContent.getBytes(characterSet));
						}
					}
				}
			}
			
			return new Show(Referer);
		}
		
		
		return new CouldNotHandle() {};
	}
	
	protected void saveTemplateDataSubmissions(HttpServletRequest request, TripartateTemplate tt, StreamableResourceSink sink, String page) 
			throws IOException {
		// Process any template data parameters
		Map<String, String> parameters = request.getParameterMap();
		Map<String, String> submittedData = new HashMap<String, String>();
		for(String key : parameters.keySet()) {
			if(key.startsWith(dataParameterPrefix)) {
				String value = request.getParameter(key);
				key = key.substring(dataParameterPrefix.length());
				submittedData.put(key, value);
			}
		}
		
		if(submittedData.size() > 0) {
			String filename = page + ".data";
			Resource r = sink.get(filename);
			if(r == null || r instanceof StreamableResource) {
				Properties templateData = new Properties();
				if(r != null) {
					templateData.load(((StreamableResource)r).getContent());
				}
				
				for(String key : submittedData.keySet()) {
					templateData.put(key, submittedData.get(key));
				}
				
				StringWriter sw = new StringWriter();
				templateData.store(sw, null);
				
				sink.write(filename, sw.toString().getBytes(characterSet));
			}
		}
		
	}
	
	protected boolean isModifyable(String templateContent) {
		return !templateContent.contains("__");
	}
	
	protected void replaceContent(Element section, String content) {
		for(Element child : section.children()) {
			child.remove();
		}
		section.html("");
		section.append(content);
	}
	
	protected List<String> getEditableSectionNames(TripartateTemplate tt) {
		List<String> result = new ArrayList<String>();
		String bodyText = tt.getSections().get("body");
		
		Document doc = Jsoup.parse(bodyText);
		Elements inlineSections = doc.select(".edit-content-inline");
		for(int sectionIndex = 0; sectionIndex < inlineSections.size(); sectionIndex++) {
			Element section = inlineSections.get(sectionIndex);
			String parmName = getParameterName(section);
			result.add(parmName);
		}
		
		return result;
	}
	
	protected String getParameterName(Element section) {
		String id = section.attr("data-input-identifier");
		if(id.startsWith("#") || id.startsWith(".")) {
			id = id.substring(1);
		}
		return id;
	}
	
	protected boolean isAllowedPageName(String name) {
		return name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".htm");
	}
	
	protected TemplateInstance findTemplate(Location location, String name) {
		TemplateSource templateSource = new WebAppLocation(location).getTemplateSource();
		return templateSource.get(name);
	}
	
	protected StreamableResourceSink findSink(Location location) {
		return (StreamableResourceSink)new WebAppLocation(location).getServiceByName(sinkName);
	}

	
	@Override
	public void integrate(Loader loader, Location location,
			ConfigurationAtom atom, Object focus) {
		if(focus != null && focus instanceof PageDataLoader) {
			pageDataLoaders.add((PageDataLoader)focus);
		}
	}

	public String getSinkName() {
		return sinkName;
	}

	public void setSinkName(String sinkName) {
		this.sinkName = sinkName;
	}

	public String getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}

	public String getRoleToEdit() {
		return roleToEdit;
	}

	public void setRoleToEdit(String roleToEdit) {
		this.roleToEdit = roleToEdit;
	}

	
	
}
