package com.emergentideas.page.editor.helpers;

import javax.servlet.http.HttpServletRequest;

import com.emergentideas.page.editor.interfaces.PageDataLoader;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.templates.TemplateInstance;
import com.emergentideas.webhandle.templates.TripartateTemplate;

public class PageNamePageDataLoader implements PageDataLoader {

	@Override
	public void loadData(HttpServletRequest request, Location location,
			TemplateInstance template, String page) {
		location.put("thePageName", page);
		if(template instanceof TripartateTemplate) {
			location.put("templateKeyOne", ((TripartateTemplate)template).getTemplateData().get("one"));
		}
	}

}
