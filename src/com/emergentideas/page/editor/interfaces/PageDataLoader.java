package com.emergentideas.page.editor.interfaces;

import javax.servlet.http.HttpServletRequest;

import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.templates.TemplateInstance;

public interface PageDataLoader {
	
	public void loadData(HttpServletRequest request, Location location, TemplateInstance template, String page);

}
