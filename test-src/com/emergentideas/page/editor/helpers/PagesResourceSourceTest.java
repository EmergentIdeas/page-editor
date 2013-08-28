package com.emergentideas.page.editor.helpers;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.emergentideas.page.editor.helpers.PagesResourceSource;
import com.emergentideas.page.editor.service.PageEditorService;
import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.FileStreamableResourceSink;
import com.emergentideas.webhandle.files.FileStreamableResourceSource;
import com.emergentideas.webhandle.files.NamedResource;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResourceSource;


public class PagesResourceSourceTest {

	@Test
	public void testGetContents() throws Exception {
		
		File root = createAndPopulateTestDirectory();
		FileStreamableResourceSink underlying = new FileStreamableResourceSink(root);
		PagesResourceSource pageSource = new PagesResourceSource(underlying);
		
		Directory d = (Directory)pageSource.get("");
		List<Resource> resources = d.getEntries();
		assertEquals(2, resources.size());
		
		Resource fileResource = null;
		Resource dirResource = null;
		
		for(Resource r : resources) {
			if(r instanceof Directory) {
				dirResource = r;
			}
			else {
				fileResource = r;
			}
		}
		
		assertEquals("three.html", ((NamedResource)fileResource).getName());
		assertEquals("sub", ((NamedResource)dirResource).getName());
		
		resources = ((Directory)dirResource).getEntries();
		assertEquals(1, resources.size());
		assertEquals("six.htm", ((NamedResource)resources.get(0)).getName());
		
		assertNotNull(pageSource.get("three.html"));
		assertNotNull(pageSource.get("sub/six.htm"));
		
		assertNull(pageSource.get("three.html.body"));
		assertNull(pageSource.get("sub/six.htm.title"));
	}
	
	
	@Test
	public void testDelete() throws Exception {
		
		File root = createAndPopulateTestDirectory();
		FileStreamableResourceSink underlying = new FileStreamableResourceSink(root);
		PagesResourceSource pageSource = new PagesResourceSource(underlying);
		
		assertNotNull(pageSource.get("three.html"));
		pageSource.delete("three.html");
		assertNull(pageSource.get("three.html"));
		
		
	}	
	
	@Test
	public void testChop() throws Exception {
		assertEquals("/one", PageEditorService.chopLastSection("/one/two"));
		assertEquals("/one", PageEditorService.chopLastSection("/one/two/"));
		assertEquals("/one", PageEditorService.chopLastSection("/one/two.html"));
		assertEquals("", PageEditorService.chopLastSection("/one"));
		assertEquals("", PageEditorService.chopLastSection("/"));

	}
	
	@Test
	public void testLastSectionName() throws Exception {
		assertEquals("two", PageEditorService.getLastSectionName("/one/two"));
		assertEquals("two", PageEditorService.getLastSectionName("/one/two/"));
		assertEquals("two.html", PageEditorService.getLastSectionName("/one/two.html"));
		assertEquals("one", PageEditorService.getLastSectionName("/one"));
		assertEquals("", PageEditorService.getLastSectionName("/"));

	}
	
	protected File createAndPopulateTestDirectory() throws Exception {
		File root = File.createTempFile("temp", System.currentTimeMillis() + "");
		root.delete();
		root.mkdir();
		
		createChildFile(root, "one.jpg");
		createChildFile(root, "two.html");
		createChildFile(root, "three.html.body");
		createChildFile(root, "three.html.title");
		
		File sub = new File(root, "sub");
		sub.mkdir();
		
		createChildFile(sub, "four.png");
		createChildFile(sub, "five.html");
		createChildFile(sub, "six.htm.body");
		createChildFile(sub, "six.htm.somethingelse");
		
		
		root.deleteOnExit();
		return root;
	}
	
	protected File createChildFile(File dir, String name) throws Exception {
		File create = new File(dir, name);
		create.createNewFile();
		return create;
	}
	
	@Test
	public void testGetPageName() throws Exception {
		PagesResourceSource source = new PagesResourceSource(null);
		
		assertEquals("one.html", source.getPageName("one.html.body"));
		assertEquals("one.htm", source.getPageName("one.htm.body"));
		assertEquals("one.htm", source.getPageName("one.htm.title"));
		assertNull(source.getPageName("one.htmf.title"));
		assertNull(source.getPageName("one.htm"));
		assertNull(source.getPageName("one.html"));
		assertNull(source.getPageName("one.jpg"));

	}
}
