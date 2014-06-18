package com.emergentideas.page.editor.handles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.helpers.PagesResourceSource;
import com.emergentideas.page.editor.helpers.ResourceDisplayEntry;
import com.emergentideas.page.editor.service.PageEditorService;
import com.emergentideas.webhandle.AppLocation;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.assumptions.oak.ParmManipulator;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.exceptions.CouldNotHandle;
import com.emergentideas.webhandle.files.CompositeStreamableResourceSource;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.DirectoryManipulator;
import com.emergentideas.webhandle.files.FileStreamableResource;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.files.StreamableResourceSource;
import com.emergentideas.webhandle.output.ResponsePackage;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;
import com.emergentideas.webhandle.templates.TemplateSource;

@Path("/files/")
public class FilesHandle {
	protected String pagesSinkName;
	protected String staticResourcesSinkName;
	protected String imagesDirectoryPrefix = "/img/";
	protected String pageTemplatesLocation = "page-templates"; 
	
	@javax.annotation.Resource
	protected PageEditorService pageEditorService;
	
	
	@GET
	@Path("create")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object createForm(Location location) {
		
		location.put("templateNames", pageEditorService.getTemplateNamesInDirectory(findPagesSink(location), pageTemplatesLocation));
		
		List<String> destinations = new ArrayList<String>();
		destinations.add("/");
		location.put("destinations", destinations);
		pageEditorService.getDescendentDirectoryNames(findPagesSink(location), "", destinations);
		removeDestination(destinations, pageTemplatesLocation);
		
		
		
		return "page-editor/create-page";
	}
	
	@POST
	@Path("create")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object create(Location location, String template, String destination, String pageName, RequestMessages messages, ParmManipulator manip) throws IOException {
		TemplateSource ts = new WebAppLocation(location).getTemplateSource();
		
		if(destination.endsWith("/") == false) {
			destination += "/";
		}
		String pagePath = destination + pageName + ".html";
		
		if(ts.get(PageEditorService.clipFirstSlashIfPresent(pagePath)) != null) {
			messages.getErrorMessages().add("That page already exists! Please choose another name.");
			manip.addRequestParameters(location);
			return createForm(location);
		}
		
		pageEditorService.copyTemplate(findPagesSink(location), pageTemplatesLocation, template, pagePath);
		
		return new Show(pagePath);
	}
	
	protected void removeDestination(List<String> destinations, String destination) {
		if(destinations.contains(destination)) {
			destinations.remove(destination);
		}
		if(destinations.contains("/" + destination)) {
			destinations.remove("/" + destination);
		}
	}
	
	@GET
	@Path("view/{path:.*}")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object showFiles(Location location, String path) {
		
		if(StringUtils.isNotBlank(path) && path.endsWith("/") == false) {
			return new Show("/files/view/" + path + "/");
		}
		
		List<Resource> resources = new ArrayList<Resource>();
		
		Resource r = findPagesResourceSource(location).get(path);
		processFoundResource(resources, r);
		
		r = findStaticSink(location).get(path);
		processFoundResource(resources, r);
		
		pageEditorService.sortResources(resources);
		List<ResourceDisplayEntry> disp = pageEditorService.getDisplayEntries(path, resources);
		disp.add(0, pageEditorService.createParentEntry(path));
		location.put("resources", disp);
		
		location.put("currentPath", path);
		
		return "page-editor/list-files";
	}
	
	@GET
	@Path("mkdir/{path:.*}")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object showMakeDirectory(Location location, String path) {
		
		location.put("parentDirectory", path);
		return "page-editor/make-directory";
	}
	
	@POST
	@Path("mkdir/{path:.*}")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object showMakeDirectory(Location location, RequestMessages messages, String path, String directoryName) {
		DirectoryManipulator sink = (DirectoryManipulator)findStaticSink(location);
		
		if(path.endsWith("/") == false) {
			path += "/";
		}
		sink.makeDirectory(path + directoryName);

		messages.getInfoMessages().add("Directory created");
		String showPath = "/files/view/" + path;
		showPath = showPath.replace("//", "/");
		return new Show(showPath);
	}


	
	@GET
	@Path("browse/type/image")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object browseImages(Location location, String CKEditor, String CKEditorFuncNum, String path) {
		return browseFiles(location, CKEditor, CKEditorFuncNum, path, "image", true);
	}
	
	@GET
	@Path("browse/type/all")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object browseAllTypes(Location location, String CKEditor, String CKEditorFuncNum, String path) {
		return browseFiles(location, CKEditor, CKEditorFuncNum, path, "all", false);
	}
	
	
	public Object browseFiles(Location location, String CKEditor, String CKEditorFuncNum, String path, String browseType, boolean imagesOnly) {
		if(path == null) {
			path = "";
		}
		
		if(isInsecurePath(path)) {
			// a security check to make sure that we're never asked for a relative path
			return new CouldNotHandle() {
			};
		}
		
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		
		location.put("CKEditor", CKEditor);
		location.put("CKEditorFuncNum", CKEditorFuncNum);
		
		List<Resource> resources = new ArrayList<Resource>();
		
		Resource r = findStaticSink(location).get(path);
		processFoundResource(resources, r);
		
		pageEditorService.sortResources(resources);
		if(imagesOnly) {
			pageEditorService.filterAllButImages(resources);
		}
		else {
			r = findPagesResourceSource(location).get(path);
			processFoundResource(resources, r);
		}
		
		List<ResourceDisplayEntry> disp = pageEditorService.getDisplayEntries(path, resources);
		disp.add(0, pageEditorService.createParentEntry(path));
		location.put("resources", disp);
		
		location.put("currentPath", path);
		location.put("browseType", browseType);
		
		return "page-editor/images-browser";
	}
	
	@POST
	@Path("upload-file")
	@RolesAllowed("page-editors")
	public Object uploadAFile(Location location, String CKEditorFuncNum, FileItem upload, HttpServletRequest request) throws Exception {
		
		String fileName = PageEditorService.getLastSectionName(upload.getName());
		
		String resourcePath = imagesDirectoryPrefix + fileName;
		String sinkPath = resourcePath.substring(1);
		
		if(isInsecurePath(sinkPath) || isInsecurePath(resourcePath)) {
			return new CouldNotHandle() {
			};
		}
		
		
		if(CKEditorFuncNum == null) {
			// because there's a big in the body value source right now that causes it not to find the request
			// url parameters if the post is multi-part
			CKEditorFuncNum = request.getParameter("CKEditorFuncNum");
		}
		writeFileToStatic(location, sinkPath, upload.get());
		
		return "<script type=\"text/javascript\">window.top.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ", '" + resourcePath + "');</script>";
	}
	
	@POST
	@Path("upload/{path:.*}")
	@Template
	@ResponsePackage("body-only")
	@RolesAllowed("page-editors")
	public Object uploadFromListScreen(Location location, FileItem contents, String Referer, String path) throws IOException {
		
		final String filename = PageEditorService.getLastSectionName(contents.getName());
		
		path = PageEditorService.bookendWithSlash(path);
		String resourcePath = path + filename;
		String sinkPath = resourcePath.substring(1);
		
		if(isInsecurePath(sinkPath) || isInsecurePath(resourcePath)) {
			return new CouldNotHandle() {
			};
		}

		writeFileToStatic(location, sinkPath, contents.get());
		
		Resource r = new FileStreamableResource(new File(filename));
		ResourceDisplayEntry entry = new ResourceDisplayEntry(path, r);
		
		
		TemplateSource ts = new WebAppLocation(location).getTemplateSource();
		
		
		Location temp = new AppLocation(location);
		temp.add(entry);
		SegmentedOutput out = new SegmentedOutput();
		ts.get("page-editor/file-entry").render(out, temp, "body");
		
		location.put("thenewentry", out.getStream("body").toString());
		
		return "page-editor/success-upload-response";
	}
	
	protected boolean isInsecurePath(String path) {
		return path.contains("..") || path.trim().startsWith("~");
	}
	
	protected void writeFileToStatic(Location location, String path, byte[] content) throws IOException {
		StreamableResourceSink sink = findStaticSink(location);
		sink.write(path, content);
	}
	
	@POST
	@Path("delete/{path:.*}")
	@RolesAllowed("page-editors")
	public Object deleteAFile(Location location, String path, String referer, RequestMessages messages) throws Exception {
		if(StringUtils.isBlank(path)) {
			return new Show(referer);
		}
		
		if(isInsecurePath(path)) {
			return new CouldNotHandle() {
			};
		}
		
		StreamableResourceSink sink = findStaticSink(location);
		if(sink.get(path) != null) {
			sink.delete(path);
			messages.getInfoMessages().add("Deleted " + path);
		}
		else {
			sink = findPagesResourceSource(location);
			if(sink.get(path) != null) {
				sink.delete(path);
				messages.getInfoMessages().add("Deleted " + path);
			}
		}
		return new Show(referer);
	}

	protected void processFoundResource(List<Resource> resources, Resource r) {
		if(r != null && r instanceof Directory) {
			resources.addAll(((Directory)r).getEntries());
		}
	}
	
	protected StreamableResourceSource findCompositeSource(Location location) {
		CompositeStreamableResourceSource comp = new CompositeStreamableResourceSource();
		comp.addSource(findStaticSink(location));
		comp.addSource(new PagesResourceSource(findPagesSink(location)));
		return comp;
	}
	
	protected StreamableResourceSink findPagesResourceSource(Location location) {
		return new PagesResourceSource(findPagesSink(location));
	}
	
	protected StreamableResourceSink findPagesSink(Location location) {
		return findSink(location, pagesSinkName);
	}
	
	protected StreamableResourceSink findStaticSink(Location location) {
		return findSink(location, staticResourcesSinkName);
	}
	
	protected StreamableResourceSink findSink(Location location, String sinkName) {
		return (StreamableResourceSink)new WebAppLocation(location).getServiceByName(sinkName);
	}

	
	public String getPagesSinkName() {
		return pagesSinkName;
	}
	public void setPagesSinkName(String pagesSinkName) {
		this.pagesSinkName = pagesSinkName;
	}
	public String getStaticResourcesSinkName() {
		return staticResourcesSinkName;
	}
	public void setStaticResourcesSinkName(String staticResourcesSinkName) {
		this.staticResourcesSinkName = staticResourcesSinkName;
	}
	public String getImagesDirectoryPrefix() {
		return imagesDirectoryPrefix;
	}
	public void setImagesDirectoryPrefix(String imagesDirectoryPrefix) {
		this.imagesDirectoryPrefix = imagesDirectoryPrefix;
	}
	public PageEditorService getPageEditorService() {
		return pageEditorService;
	}
	public void setPageEditorService(PageEditorService pageEditorService) {
		this.pageEditorService = pageEditorService;
	}
	public String getPageTemplatesLocation() {
		return pageTemplatesLocation;
	}
	public void setPageTemplatesLocation(String pageTemplatesLocation) {
		this.pageTemplatesLocation = pageTemplatesLocation;
	}
	
}
