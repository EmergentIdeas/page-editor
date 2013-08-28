package com.emergentideas.page.editor.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.emergentideas.page.editor.service.PageEditorService;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.NamedResource;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResource;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.files.StreamableResourceSource;

/**
 * Wraps another resource source and creates "new" resources that represent a page where
 * a page is defined as a bunch of templates that have a name ending with .html or .htm
 * @author kolz
 *
 */
public class PagesResourceSource implements StreamableResourceSink {
	
	protected Pattern pageTemplateNamePattern = Pattern.compile("([^.]+\\.(html|htm))\\.[^.]+");
	
	protected StreamableResourceSink templateResourceSource;
	
	
	
	public PagesResourceSource(StreamableResourceSink templateResourceSource) {
		this.templateResourceSource = templateResourceSource;
	}

	@Override
	public Resource get(String location) {
		Resource resource = templateResourceSource.get(location);
		if(resource instanceof Directory) {
			Directory dir = (Directory)resource;
			return new FilteredDirectoryResource(location, dir instanceof NamedResource ? ((NamedResource)dir).getName() : location, dir);
		}
		resource = templateResourceSource.get(PageEditorService.chopLastSection(location));
		if(resource instanceof Directory) {
			Directory dir = (Directory)resource;
			for(Resource child : dir.getEntries()) {
				if(child instanceof NamedResource && ((NamedResource)child).getName().startsWith(PageEditorService.getLastSectionName(location) + ".")) {
					return new PageResource(PageEditorService.getLastSectionName(location), location);
				}
			}
		}
		
		return null;
	}
	
	protected List<Resource> getDirectoryResources(Directory dir, String currentLocation) {
		currentLocation = ensureSlashTerminated(currentLocation);
		List<Resource> resources = new ArrayList<Resource>();
		List<String> foundFiles = new ArrayList<String>();
		
		for(Resource entry : dir.getEntries())
		{
			NamedResource nr = (NamedResource)entry;
			
			if(entry instanceof NamedResource == false) {
				continue;
			}
			
			if(entry instanceof Directory) {
				resources.add(new FilteredDirectoryResource(currentLocation + ((NamedResource)entry).getName(), ((NamedResource)entry).getName(), (Directory)entry));
			}
			else if(entry instanceof StreamableResource) {
				String name = nr.getName();
				name = getPageName(name);
				if(name == null || foundFiles.contains(name)) {
					// Either this isn't a file which represents the templates for a page or we've seen it before
					continue;
				}
				foundFiles.add(name);
				resources.add(new PageResource(name, currentLocation + name));
			}
		}
		
		return resources;
	}
	
	/**
	 * Returns the page name if this name is formated as a page template (something.html.body for example) or null
	 * if it doesn't meet the formatting guidelines.
	 * @param name
	 * @return
	 */
	protected String getPageName(String name) {
		Matcher m = pageTemplateNamePattern.matcher(name);
		if(!m.matches()) {
			return null;
		}
		return m.group(1);
	}
	
	
	
	@Override
	public void write(String location, InputStream data) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(String location, byte[] data) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String location) throws IOException {
		String parentPath = PageEditorService.chopLastSection(location);
		Resource resource = templateResourceSource.get(parentPath);
		if(resource instanceof Directory) {
			Directory dir = (Directory)resource;
			for(Resource child : dir.getEntries()) {
				if(child instanceof NamedResource && ((NamedResource)child).getName().startsWith(PageEditorService.getLastSectionName(location) + ".")) {
					String childFullPath = ensureSlashTerminated(PageEditorService.chopLastSection(location)) + ((NamedResource)child).getName();
					templateResourceSource.delete(childFullPath);
				}
			}
		}
		
	}

	protected String ensureSlashTerminated(String location) {
		return location.endsWith("/") ? location : (location + "/");
	}
	
	public Pattern getPageTemplateNamePattern() {
		return pageTemplateNamePattern;
	}

	public void setPageTemplateNamePattern(Pattern pageTemplateNamePattern) {
		this.pageTemplateNamePattern = pageTemplateNamePattern;
	}

	public StreamableResourceSink getTemplateResourceSource() {
		return templateResourceSource;
	}

	public void setTemplateResourceSource(
			StreamableResourceSink templateResourceSource) {
		this.templateResourceSource = templateResourceSource;
	}


	/**
	 * A resource representing a Page object. Since a page is a composite of many files, it's not really 
	 * a streamable object, but it does have a name and a path.
	 * @author kolz
	 *
	 */
	class PageResource implements NamedResource, Resource {
		protected String name;
		protected String path;
		
		
		public PageResource(String name, String path) {
			super();
			this.name = name;
			this.path = path;
		}

		@Override
		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
	
	/**
	 * Represents a directory where calls to getEntries() will also return the entries filtered to show only directories
	 * and pages.
	 * @author kolz
	 *
	 */
	class FilteredDirectoryResource implements Directory, NamedResource {
		
		/**
		 * The full source relative path of the directory
		 */
		protected String path;
		
		/**
		 * The directory object from the underlying source
		 */
		protected Directory directory;
		
		/**
		 * The name of this entry
		 */
		protected String name;

		public FilteredDirectoryResource(String path, String name, Directory directory) {
			super();
			this.path = path;
			this.directory = directory;
			this.name = name;
		}

		@Override
		public List<Resource> getEntries() {
			return getDirectoryResources(directory, path);
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public Directory getDirectory() {
			return directory;
		}

		public void setDirectory(Directory directory) {
			this.directory = directory;
		}

		@Override
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
