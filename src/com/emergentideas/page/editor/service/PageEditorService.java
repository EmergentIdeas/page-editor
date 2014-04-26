package com.emergentideas.page.editor.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.helpers.ResourceDisplayComparator;
import com.emergentideas.page.editor.helpers.ResourceDisplayEntry;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.NamedResource;
import com.emergentideas.webhandle.files.StreamableResource;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.files.StreamableResourceSource;

@Resource
public class PageEditorService {

	/**
	 * Gets the names of all of the resources (minus the suffix) in a given directory.
	 * @param source
	 * @param directoryName
	 * @return
	 */
	public List<String> getTemplateNamesInDirectory(StreamableResourceSource source, String directoryName) {
		List<String> results = new ArrayList<String>();
		
		com.emergentideas.webhandle.files.Resource r = source.get(directoryName);
		
		if(r != null && r instanceof Directory) {
			Directory d = (Directory)r;
			for(com.emergentideas.webhandle.files.Resource child : d.getEntries()) {
				if(child instanceof NamedResource && child instanceof Directory == false) {
					String name = getNameWithoutSuffix(((NamedResource)child).getName());
					if(name != null && results.contains(name) == false) {
						results.add(name);
					}
				}
			}
		}
		
		return results;
	}
	
	public void getDescendentDirectoryNames(StreamableResourceSource source, String directoryName, List<String> directoryNames) {
		
		com.emergentideas.webhandle.files.Resource r = source.get(directoryName);
		
		if(r != null && r instanceof Directory) {
			Directory d = (Directory)r;
			for(com.emergentideas.webhandle.files.Resource child : d.getEntries()) {
				if(child instanceof NamedResource && child instanceof Directory) {
					String name = ((NamedResource)child).getName();
					String path = directoryName + "/" + name;
					directoryNames.add(path);
					getDescendentDirectoryNames(source, path, directoryNames);
				}
			}
		}
	}
	
	public void copyTemplate(StreamableResourceSink sink, String sourceLocation, String sourceName, String destinationTemplatePath) throws IOException {
		clipFirstSlashIfPresent(destinationTemplatePath);
		
		Directory d = (Directory)sink.get(sourceLocation);
		for(com.emergentideas.webhandle.files.Resource child : d.getEntries()) {
			if(child instanceof NamedResource && child instanceof StreamableResource && child instanceof Directory == false) {
				NamedResource nr = (NamedResource)child;
				if(sourceName.equals(getNameWithoutSuffix(nr.getName()))) {
					sink.write(destinationTemplatePath + "." + getNameSuffix(nr.getName()), ((StreamableResource)child).getContent());
				}
			}
		}
	}
	
	public static String clipFirstSlashIfPresent(String path) {
		while(path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

	
	/**
	 * Removes the last dot and anything following. If there is no period null is returned.
	 * @param name
	 * @return
	 */
	protected String getNameWithoutSuffix(String name) {
		int index = name.lastIndexOf('.');
		if(index < 0) {
			return null;
		}
		return name.substring(0, index);
	}
	
	protected String getNameSuffix(String name) {
		int index = name.lastIndexOf('.');
		if(index < 0) {
			return null;
		}
		return name.substring(index + 1);
	}
	
	public void sortResources(List<? extends com.emergentideas.webhandle.files.Resource> resources) {
		Collections.sort(resources, new ResourceDisplayComparator());
	}
	
	public void filterAllButImages(List<? extends com.emergentideas.webhandle.files.Resource> resources) {
		Iterator<? extends com.emergentideas.webhandle.files.Resource> it = resources.iterator();
		while(it.hasNext()) {
			com.emergentideas.webhandle.files.Resource r = it.next();
			if(r instanceof StreamableResource && r instanceof NamedResource) {
				NamedResource nr = (NamedResource)r;
				if(isImageFilename(nr.getName())) {
					// keep the resource
				}
				else {
					it.remove();
				}
			}
			
		}
	}
	
	/**
	 * Returns true if the filename indicates that it is an image
	 */
	public static boolean isImageFilename(String filename) {
		filename = filename.toLowerCase();
		if(
				filename.endsWith(".jpg") ||
				filename.endsWith(".jpeg") ||
				filename.endsWith(".png") ||
				filename.endsWith(".gif") ||
				filename.endsWith(".bmp") ||
				filename.endsWith(".tiff")
				) {
			return true;
		}
		return false;
	}
	
	public List<ResourceDisplayEntry> getDisplayEntries(String currentPath, List<com.emergentideas.webhandle.files.Resource> resources) {
		List<ResourceDisplayEntry> result = new ArrayList<ResourceDisplayEntry>();
		for(com.emergentideas.webhandle.files.Resource r : resources) {
			result.add(new ResourceDisplayEntry(currentPath, r));
		}
		return result;
	}
	
	public ResourceDisplayEntry createParentEntry(String currentPath) {
		ResourceDisplayEntry result = new ResourceDisplayEntry(bookendWithSlash(chopLastSection(currentPath)), null);
		return result;
	}
	
	/**
	 * Writes a file to the named sink. This can be used by other handlers to write files to
	 * disk and then associate them with their objects however they'd like
	 */
	public String writeNewAttachment(Location location, FileItem item, String sinkName, String relativePathPrefix) throws IOException {
		StreamableResourceSink sink = findSink(location, sinkName);
		String path = getRelativeFileName(relativePathPrefix, getShortFileName(item));
		sink.write(path, item.get());
		
		if(path.startsWith("/") == false) {
			path = "/" + path;
		}
		
		return path;
	}
	
	/**
	 * Writes a file to the named sink. This can be used by other handlers to write files to
	 * disk and then associate them with their objects however they'd like
	 */
	public String writeNewAttachment(Location location, String originalFileName, String sinkName, String relativePathPrefix, InputStream data) throws IOException {
		StreamableResourceSink sink = findSink(location, sinkName);
		String path = getRelativeFileName(relativePathPrefix, getLastSectionName(originalFileName));
		sink.write(path, data);
		
		if(path.startsWith("/") == false) {
			path = "/" + path;
		}
		
		return path;
	}
	
	/**
	 * Creates a short file name for use with a {@link FileItem}. Checks for non-allowed path
	 * segments like .. and will create a new timestamp based name if blank.
	 */
	public String getShortFileName(FileItem item) {
		String name = item.getName();
		if(StringUtils.isBlank(name)) {
			name = createNewName();
		}
		
		name = getLastSectionName(name);
		
		if(StringUtils.isBlank(name) || name.contains("..") || name.startsWith("~/")) {
			name = createNewName();
		}
		
		return name;
	}
	
	/**
	 * Creates a new timestamp based file name
	 */
	protected String createNewName() {
		return "attachment" + System.currentTimeMillis() + ".jpg";
	}

	/**
	 * Gets the sink by name
	 */
	public StreamableResourceSink findSink(Location location, String sinkName) {
		return (StreamableResourceSink)new WebAppLocation(location).getServiceByName(sinkName);
	}

	/**
	 * Prefixes a short name with a relative path.
	 */
	protected String getRelativeFileName(String relativePathPrefix, String shortName) {
		if(relativePathPrefix.endsWith("/") == false) {
			relativePathPrefix += "/";
		}
		return relativePathPrefix + shortName;
	}
	
	public static String bookendWithSlash(String path) {
		if(path.startsWith("/") == false) {
			path = "/" + path;
		}
		if(path.endsWith("/") == false) {
			path = path + "/";
		}
		return path;
	}
	

	
	/**
	 * Removes the last path portion (essentially returning the parent) including what would
	 * be the trailing slash 
	 */
	public static String chopLastSection(String path) {
		path = transformSlashes(path);
		
		if(path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		int index = path.lastIndexOf('/');
		if(index <= 0) {
			return "";
		}
		return path.substring(0, index);
	}
	
	/**
	 * Returns the last non-blank section of a path. For example:
	 * /hello/ -> hello
	 * /hello/there -> there
	 * /hello/there.jpg -> there.jpg
	 */
	public static String getLastSectionName(String path) {
		path = transformSlashes(path);
		
		if(path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		int index = path.lastIndexOf('/');
		if(index < 0) {
			return path;
		}
		else if(index == 0) {
			return path.substring(1);
		}
		return path.substring(index + 1);
	}
	
	/**
	 * Transforms all back slashes to forward slashes
	 */
	public static String transformSlashes(String path) {
		if(path == null) {
			return null;
		}
		
		return path.replace('\\', '/');
	}
	
	
}
