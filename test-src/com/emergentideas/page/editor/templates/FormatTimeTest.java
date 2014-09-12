package com.emergentideas.page.editor.templates;

import java.util.Calendar;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormatTimeTest {

	@Test
	public void testTimeFormat() throws Exception {
		FormatTime ft = new FormatTime();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 55);
		
		assertEquals("1:55 PM", ft.format(c.getTime()));
	}
}
