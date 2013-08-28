package com.emergentideas.page.editor.service;

import org.junit.Test;

import com.emergentideas.page.editor.service.PageEditorService;

import static junit.framework.Assert.*;

public class PageEditorServiceTest {

	@Test
	public void testRemoveSuffix() throws Exception {
		PageEditorService service = new PageEditorService();
		
		assertEquals("hello", service.getNameWithoutSuffix("hello.there"));
		assertEquals("hello.there", service.getNameWithoutSuffix("hello.there."));
		assertEquals("hello.there", service.getNameWithoutSuffix("hello.there.world"));
		assertNull(service.getNameWithoutSuffix("hello"));
	}
}
