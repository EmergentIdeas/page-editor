package com.emergentideas.page.editor.handles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.helpers.PagesResourceSource;
import com.emergentideas.page.editor.helpers.ResourceDisplayEntry;
import com.emergentideas.page.editor.service.PageEditorService;
import com.emergentideas.webhandle.AppLocation;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.Name;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.assumptions.oak.ParmManipulator;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.exceptions.CouldNotHandle;
import com.emergentideas.webhandle.files.CompositeStreamableResourceSource;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.DirectoryManipulator;
import com.emergentideas.webhandle.files.DirectoryResource;
import com.emergentideas.webhandle.files.FileStreamableResource;
import com.emergentideas.webhandle.files.FileStreamableResourceSink;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResource;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.files.StreamableResourceSource;
import com.emergentideas.webhandle.files.StreamableResourcesHandler;
import com.emergentideas.webhandle.json.JSON;
import com.emergentideas.webhandle.output.DirectRespondent;
import com.emergentideas.webhandle.output.NoResponse;
import com.emergentideas.webhandle.output.ResponsePackage;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;
import com.emergentideas.webhandle.templates.TemplateSource;

@Path("/files/")
@javax.annotation.Resource
public class FilesHandle {
	protected String pagesSinkName;
	protected String staticResourcesSinkName;
	protected String imagesDirectoryPrefix = "/img/";
	protected String pageTemplatesLocation = "page-templates"; 
	
	@javax.annotation.Resource
	protected PageEditorService pageEditorService;
	
	protected String resizeWidth = "200x";
	protected File thumbsRoot;
	protected FileStreamableResourceSink thumbsSink;
	protected StreamableResourcesHandler thumbsHandle;
	
	public FilesHandle() throws IOException {
		thumbsRoot = File.createTempFile("thumbnails", "dir");
		thumbsRoot.delete();
		thumbsRoot.mkdir();
		thumbsSink = new FileStreamableResourceSink(thumbsRoot);
		thumbsHandle = new StreamableResourcesHandler(thumbsSink);
	}

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
		Collections.sort(destinations);
		
		return "page-editor/create-page";
	}

	@GET
	@Path("api/all-pages")
	@Template
	@JSON
	@RolesAllowed("page-editors")
	public Object allPages(Location location) {
		return pageEditorService.getAllPages(findPagesSink(location));
	}
	
	@POST
	@Path("api/write/{path:.*}")
	@Template
	@RolesAllowed("page-editors")
	public Object writeFile(Location location, String path, String data, FileItem contents) throws IOException {
		if(isInsecurePath(path)) {
			return new CouldNotHandle() {
			};
		}

		byte[] dataToWrite;
		if(StringUtils.isBlank(data)) {
			dataToWrite = contents.get();
		}
		else {
			dataToWrite = Base64.decodeBase64(data);
		}
		writeFileToStatic(location, path, dataToWrite);
		return "success";
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
		DirectoryManipulator templateSink = (DirectoryManipulator)findPagesSink(location);
		
		if(path.endsWith("/") == false) {
			path += "/";
		}
		sink.makeDirectory(path + directoryName);
		templateSink.makeDirectory(path + directoryName);

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
			pageEditorService.sortResources(resources);
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
	public Object uploadFromListScreen(Location location, FileItem contents, String Referer, 
			String path, String dataUrl, String dataFilename) throws IOException {
		
		boolean dataUrlStyle = StringUtils.isNotBlank(dataUrl);
		
		String filename;
		if(dataUrlStyle) {
			filename = dataFilename;
			if(StringUtils.isBlank(filename)) {
				filename = System.currentTimeMillis() + "." + 
						pageEditorService.getSuffixForMimeType(pageEditorService.getMimeTypeFromDataURI(dataUrl));
			}
		}
		else {
			filename = PageEditorService.getLastSectionName(contents.getName());
		}
		
		path = PageEditorService.bookendWithSlash(path);
		
		
		String resourcePath = path + filename;
		String sinkPath = resourcePath.substring(1);
		
		if(isInsecurePath(sinkPath) || isInsecurePath(resourcePath)) {
			return new CouldNotHandle() {
			};
		}

		byte[] dataToWrite;
		if(StringUtils.isBlank(dataUrl)) {
			dataToWrite = contents.get();
		}
		else {
			dataToWrite = IOUtils.toByteArray(pageEditorService.convertDataURIToBytes(dataUrl));
		}
		writeFileToStatic(location, sinkPath, dataToWrite);
		
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
	
	@Path("thumbnails/urls/{path:.*}")
	@JSON
	public Object showUrls(Location location, String path, String fileType) {
		path = removeSlashes(path);
		StreamableResourceSource source = findStaticSink(location);
		Resource rsc = source.get(path);
		List<String> result = new ArrayList<String>();
		
		if(rsc instanceof DirectoryResource) {
			DirectoryResource dir = (DirectoryResource)rsc;
			for(Resource child : dir.getEntries()) {
				if(child instanceof StreamableResource) {
					
					FileStreamableResource childRsc = (FileStreamableResource)child;
					if("image".equalsIgnoreCase(fileType)) {
						if(isPathImage(childRsc.getName())) {
							result.add("/" + createPath(path, childRsc.getName()));
						}
					}
					else {
						result.add("/" + createPath(path, childRsc.getName()));
					}
				}
			}
		}
		
		return result;
	}
	
	protected boolean isPathImage(String path) {
		if(path == null) {
			return false;
		}
		path = path.toLowerCase();
		if(path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif")) {
			return true;
		}
		return false;
	}

	
	protected String createPath(String dirSegment, String lastSegment) {
		dirSegment = removeSlashes(dirSegment);
		lastSegment = removeSlashes(lastSegment);
		return dirSegment + "/" + lastSegment;
	}
	
	/**
	 * Remove the starting and ending slashes.
	 */
	protected String removeSlashes(String s) {
		if(s.startsWith("/")) {
			s = s.substring(1);
		}
		if(s.endsWith("/")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	@Path("thumbnails/thumb/{path:.+}")
	@GET
	public Object showThumbnailImage(Location location, HttpServletResponse response, String path,
			ServletContext servletContext, @Name("If-None-Match") String existingETag,
			HttpServletRequest request, String resizeWidth) throws IOException, InterruptedException {
		if(findStaticSink(location).get(path) == null || isPathImage(path) == false) {
			return new NoResponse();
		}
		
		if(StringUtils.isBlank(resizeWidth)) {
			resizeWidth = this.resizeWidth;
		}
		if(resizeWidth.indexOf('x') < 0) {
			resizeWidth += "x";
		}
		resizeWidth = cleanResize(resizeWidth);
		
		String thumbnailPath = createResizePath(removeSlashes(path), resizeWidth);
		Resource rsc = thumbsSink.get(thumbnailPath);
		if(rsc == null) {
			createThumbnail(location, path, resizeWidth);
		}
		
		return thumbsHandle.handle(thumbnailPath, servletContext, existingETag, location, request);
	}
	
	protected String createResizePath(String path, String resizeWidth) {
		int period = path.lastIndexOf('.');
		return path.substring(0, period) + "-" + resizeWidth + path.substring(period);
		
	}
	
	protected String cleanResize(String s) {
		StringBuilder sb = new StringBuilder();
		for(char c : s.toCharArray()) {
			if(Character.isDigit(c) || c == 'x') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	protected void createThumbnail(Location location, String path, String width) throws IOException, InterruptedException {
		
		Resource rsc = findStaticSink(location).get(path);
		if(rsc != null && rsc instanceof StreamableResource) {
			String thumbPath = createResizePath(removeSlashes(path), width);
			StreamableResource in = (StreamableResource)rsc;
			
			thumbsSink.write(path, in.getContent());
			
			
			String absoluteOrigin = "/" + createPath(thumbsRoot.getAbsolutePath(), path);
			String absoluteDestination = "/" + createPath(thumbsRoot.getAbsolutePath(), thumbPath);
			String command = String.format("convert -resize %s '%s' '%s'", width, absoluteOrigin, absoluteDestination);
//			Runtime.getRuntime().exec(command).waitFor();
			Runtime.getRuntime().exec(new String[] { "convert", "-resize", width, absoluteOrigin, absoluteDestination}).waitFor();
		}
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
	
	public StreamableResourceSink findPagesResourceSource(Location location) {
		return new PagesResourceSource(findPagesSink(location));
	}
	
	public StreamableResourceSink findPagesSink(Location location) {
		return findSink(location, pagesSinkName);
	}
	
	public StreamableResourceSink findStaticSink(Location location) {
		return findSink(location, staticResourcesSinkName);
	}
	
	public StreamableResourceSink findSink(Location location, String sinkName) {
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
