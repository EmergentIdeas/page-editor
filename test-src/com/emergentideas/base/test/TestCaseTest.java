package com.emergentideas.base.test;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestCaseTest extends FullEnvTestCase {

	@Test
	public void testGetTestFile() throws Exception {
		
		String result = call("/test1.html", "GET", null);
		assertTrue(result.contains("This is the page, test1"));
	}
}
