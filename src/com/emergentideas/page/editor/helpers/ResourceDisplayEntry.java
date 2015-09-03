package com.emergentideas.page.editor.helpers;

import com.emergentideas.page.editor.helpers.PagesResourceSource.PageResource;
import com.emergentideas.page.editor.service.PageEditorService;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.FileStreamableResource;
import com.emergentideas.webhandle.files.NamedResource;
import com.emergentideas.webhandle.files.Resource;

public class ResourceDisplayEntry {

	protected Resource resource;
	protected String currentPath;
	
	protected String unknownName = "unknown";

	public ResourceDisplayEntry(String currentPath, Resource resource) {
		super();
		this.resource = resource;
		this.currentPath = currentPath;
	}
	
	
	public String getName() {
		if(resource instanceof NamedResource) {
			return ((NamedResource)resource).getName();
		}
		if(resource == null) {
			return "..";
		}
		
		return unknownName;
	}
	
	public boolean getIsFile() {
		return !(resource == null || resource instanceof Directory);
	}
	
	public String getBrowseTileTemplate() {
		if(resource == null) {
			return "page-editor/browse-tile-directory";
		}
		if(resource instanceof Directory) {
			return "page-editor/browse-tile-directory";
		}
		return "page-editor/browse-tile-image";
	}

	public String getIconClass() {
		if(resource == null) {
			return "folder";
		}
		if(resource instanceof Directory) {
			return "folder";
		}
		return "file";
	}
	public String getEditPath() {
		if(resource instanceof PageResource) {
			return ((PageResource)resource).getPath();
		}
		return null;
	}
	
	public String getViewPath() {
		if(resource == null) {
			return "/files/view" + PageEditorService.bookendWithSlash(currentPath);
		}
		else if(resource instanceof PageResource) {
			return ((PageResource)resource).getPath();
		}
		else if(resource instanceof FileStreamableResource) {
			return PageEditorService.bookendWithSlash(currentPath) + ((FileStreamableResource)resource).getName();
		}
		else if(resource instanceof Directory && resource instanceof NamedResource) {
			return "/files/view" + PageEditorService.bookendWithSlash(currentPath) + ((NamedResource)resource).getName() + "/";
		}
		return null;
	}
	
	public String getResourcePath() {
		if(resource == null) {
			return PageEditorService.bookendWithSlash(currentPath);
		}
		else if(resource instanceof PageResource) {
			return ((PageResource)resource).getPath();
		}
		else if(resource instanceof FileStreamableResource) {
			return PageEditorService.bookendWithSlash(currentPath) + ((FileStreamableResource)resource).getName();
		}
		else if(resource instanceof Directory && resource instanceof NamedResource) {
			return PageEditorService.bookendWithSlash(currentPath) + ((NamedResource)resource).getName();
		}
		return null;
	}
	
	public String getLargeTileImage() {
		if(resource == null) {
			return "/vrsc/123/page-editor/img/yellow-folder-icon.jpg";
		}
		if(resource instanceof Directory) {
			return "/vrsc/123/page-editor/img/yellow-folder-icon.jpg";
		}
		if(resource instanceof NamedResource) {
			NamedResource nr = (NamedResource)resource;
			if(PageEditorService.isImageFilename(nr.getName())) {
				String s = getResourcePath();
				if(s.startsWith("/") == false) {
					s = "/" + s;
				}
				return s;
			}
		}
		return "/page-editor/img/file-icon-md.png";
		
	}
	

	public Resource getResource() {
		return resource;
	}


	public void setResource(Resource resource) {
		this.resource = resource;
	}


	public String getUnknownName() {
		return unknownName;
	}


	public void setUnknownName(String unknownName) {
		this.unknownName = unknownName;
	}
	
	
	
}
