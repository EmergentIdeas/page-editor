package com.emergentideas.page.editor.templates;

import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/no-escape")
public class NoEscape implements TemplateInstance {

	@Override
	public void render(SegmentedOutput output, Location location,
			String elementSourceName, String... processingHints) {
		output.getStream(elementSourceName).append(location.get("$this"));
	}

	
}
