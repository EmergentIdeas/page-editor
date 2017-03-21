package com.emergentideas.page.editor.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Link;
import com.emergentideas.page.editor.helpers.ResourceDisplayComparator;
import com.emergentideas.page.editor.helpers.ResourceDisplayEntry;
import com.emergentideas.page.editor.interfaces.TemplateRewriter;
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
		if(r == null && directoryName.length() > 1 && directoryName.charAt(0) == '/') {
			r = source.get(directoryName.substring(1));
		}
		
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
	
	public List<Link> getAllPages(StreamableResourceSource source) {
		List<String> directoryNames = new ArrayList<String>();
		getDescendentDirectoryNames(source, "", directoryNames);
		
		List<Link> pagePaths = new ArrayList<Link>();
		for(String dir : directoryNames) {
			for(String page : getTemplateNamesInDirectory(source, dir.substring(1))) {
				if(page.endsWith(".html") == false) {
					continue;
				}
				pagePaths.add(new Link(page, dir + "/" + page));
			}
		}
		return pagePaths;
	}

	public void copyTemplate(StreamableResourceSink sink, String sourceLocation, String sourceName, String destinationTemplatePath) throws IOException {
		copyTemplateAndRewrite(sink, sourceLocation, sourceName, destinationTemplatePath);
	}
	public void copyTemplateAndRewrite(StreamableResourceSink sink, String sourceLocation, String sourceName, String destinationTemplatePath,
			TemplateRewriter ... rewriters) throws IOException {
		clipFirstSlashIfPresent(destinationTemplatePath);
		
		Directory d = (Directory)sink.get(sourceLocation);
		for(com.emergentideas.webhandle.files.Resource child : d.getEntries()) {
			if(child instanceof NamedResource && child instanceof StreamableResource && child instanceof Directory == false) {
				NamedResource nr = (NamedResource)child;
				if(sourceName.equals(getNameWithoutSuffix(nr.getName()))) {
					InputStream content = ((StreamableResource)child).getContent();
					String destPath = destinationTemplatePath + "." + getNameSuffix(nr.getName());
					if(rewriters != null) {
						for(TemplateRewriter rewriter : rewriters) {
							content = new ByteArrayInputStream(rewriter.transform(IOUtils.toString(content), destPath).getBytes());
						}
					}
					
					sink.write(destPath, content);
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
		Set<String> seen = new HashSet<String>();
		List<ResourceDisplayEntry> result = new ArrayList<ResourceDisplayEntry>();
		for(com.emergentideas.webhandle.files.Resource r : resources) {
			ResourceDisplayEntry entry = new ResourceDisplayEntry(currentPath, r);
			if(seen.contains(entry.getName())) {
				continue;
			}
			seen.add(entry.getName());
			result.add(entry);
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
	
	public InputStream convertDataURIToBytes(String base64Data) throws UnsupportedEncodingException {
		if(StringUtils.isBlank(base64Data)) {
			return null;
		}
		int i = base64Data.indexOf(',');
		
		String data = base64Data.substring(i + 1);
		String prefix = base64Data.substring(0, i);
		prefix = prefix.toLowerCase();
		
		byte[] bData = null;
		if(prefix.endsWith("base64")) {
			bData = Base64.decodeBase64(data);
		}
		else {
			bData = data.getBytes("UTF-8");
		}
		
		return new ByteArrayInputStream(bData);
	}
	
	public String getSuffixForMimeType(String mimeType) {
		if(StringUtils.isBlank(mimeType)) {
			return "ukn";
		}
		if(mimeType.contains("png")) {
			return "png";
		}
		if(mimeType.contains("jpeg")) {
			return "jpg";
		}
		
		return "ukn";
	}
	
	public String getMimeTypeFromDataURI(String base64Data) {
		if(StringUtils.isBlank(base64Data)) {
			return null;
		}
		int i = base64Data.indexOf(',');
		
		String prefix = base64Data.substring(0, i);
		prefix = prefix.toLowerCase();
		if(prefix.startsWith("data:")) {
			prefix = prefix.substring(5);
		}
		
		Integer firstStop = getLowestPositiveInt(prefix.charAt(','), prefix.charAt(';'));
		
		if(firstStop != null) {
			return prefix.substring(0, firstStop);
		}
		return prefix;
	}
	
	/**
	 * Returns the lowest integer which is zero or greater. Returns
	 * null if no integer was greater than or equal to zero.
	 */
	protected Integer getLowestPositiveInt(int ... i) {
		Integer result = null;
		for(int item : i) {
			if(item >= 0) {
				if(result == null) {
					result = item;
				}
				else {
					if(item < result) {
						result = item;
					}
				}
			}
		}
		
		return result;
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
