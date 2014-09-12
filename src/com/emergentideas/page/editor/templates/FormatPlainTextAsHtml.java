package com.emergentideas.page.editor.templates;

import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/text-as-html")
public class FormatPlainTextAsHtml implements TemplateInstance {

	@Override
	public void render(SegmentedOutput output, Location location,
			String elementSourceName, String... processingHints) {
		
		Object o = location.get("$this");
		if(o != null) {
			String s = o.toString();
			output.getStream(elementSourceName).append(format(s));
		}

	}
	
	protected String format(String s) {
		return s.replace("\n", "<br/>");
	}


}
