package com.emergentideas.page.editor.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.mockito.Mockito;

import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.SiteSet;
import com.emergentideas.page.editor.data.Item.ItemType;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.utils.DateUtils;

public class WordpressFeedImporterTest {

	@Test
	public void testImport() throws Exception {
		File f = new File(new File("").getAbsoluteFile(), "codingfit.wordpress.2014-02-04.xml");
		InputStream is = new FileInputStream(f);
		
		PostService ps = Mockito.mock(PostService.class);
		WordpressFeedImporter wfi = new WordpressFeedImporter();
		wfi.setPostService(ps);
		
		SiteSet site = wfi.parseRSS(is);
		
		Item item = site.getItemBySlug("like-a-black-hole");
		
		assertNotNull(item);
		
		assertEquals(likeABlackHoleContet, item.getContent());
		assertTrue(DateUtils.htmlExpiresDateFormat().format(item.getPubDate()).startsWith("Tue, 08 Jan 2013 14:24:33"));
		assertEquals(PubStatus.PUBLISH, item.getStatus());
		assertEquals(ItemType.POST, item.getType());
		assertEquals("Like a Black Hole", item.getTitle());
		assertEquals("Dan Kolz", item.getAuthor().getDisplayName());
		assertEquals("coding", item.getCategories().get(0).getSlug());
		
	}
	
	protected void printPost(Item item) {
		System.out.println("content: " + item.getContent());
	}
	
	protected String likeABlackHoleContet = "I've got two cats, Olive and Squirt. Olive is jet black and in some lights looks like a hole in the floor. She is, as I just discovered, also a great absorber of sound.\n" + 
			"\n" + 
			"My desk has speakers on either side of the monitor which are playing this morning as I work. Olive just walked in front of the one on left and I thought I'd gone deaf on that side. I tried it with a box that's handy and only got a diminished volume on that side.\n" + 
			"\n" + 
			"So, pro tip, I guess: If you want a room to be really quite, fill it with black cats.";
}
