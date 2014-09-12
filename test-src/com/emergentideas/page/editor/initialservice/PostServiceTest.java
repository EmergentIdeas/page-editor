package com.emergentideas.page.editor.initialservice;

import org.junit.Test;
import static org.junit.Assert.*;

public class PostServiceTest {
	
	@Test
	public void testScrub() throws Exception {
		PostService ps = new PostService();
		
		String s;
		s = ps.scrubPostComment("&amp;lt;script&amp;gt;");
		assertEquals("lt;scriptgt;", s);
		
		s = ps.scrubPostComment("&lt;script&gt;");
		assertEquals("", s);
		
		s = ps.scrubPostComment("<script>");
		assertEquals("", s);

		s = ps.scrubPostComment("hello\nthere");
		assertEquals("hello\nthere", s);
	}

}
