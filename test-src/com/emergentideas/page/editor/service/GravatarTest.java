package com.emergentideas.page.editor.service;

import org.junit.Test;

import com.emergentideas.utils.CryptoUtils;

public class GravatarTest {

	@Test
	public void testGravatarHash() throws Exception {
		
		System.out.println(CryptoUtils.generateMD5Hash("dan@emergentideas.com"));
	}
}
