package com.emergentideas.page.editor.templates;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/url-encode")
public class UrlEncode implements TemplateInstance {

	@Override
	public void render(SegmentedOutput output, Location location,
			String elementSourceName, String... processingHints) {
		try {
			output.getStream(elementSourceName).append(replaceHash(location.get("$this").toString()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	protected String replaceHash(String s) throws UnsupportedEncodingException {
		return s.replaceAll("#", URLEncoder.encode("#", "utf-8"));
	}
}
