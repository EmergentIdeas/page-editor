package com.emergentideas.page.editor.templates;

import java.text.DateFormat;
import java.util.Date;

import com.emergentideas.utils.DateUtils;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/human-time")
public class FormatTime implements TemplateInstance {

	protected DateFormat format;
	protected String formatString = "h:mm a";
	
	@Override
	public void render(SegmentedOutput output, Location location,
			String elementSourceName, String... processingHints) {
		
		Object o = location.get("$this");
		if(o != null && o instanceof Date) {
			Date d = (Date)o;
			output.getStream(elementSourceName).append(format(d));
		}

	}
	
	protected String format(Date d) {
		return getFormat().format(d);
	}

	public DateFormat getFormat() {
		if(format == null) {
			format = DateUtils.newDateFormat(formatString);
		}
		return format;
	}

	public void setFormat(DateFormat format) {
		this.format = format;
	}

	public String getFormatString() {
		return formatString;
	}

	public void setFormatString(String formatString) {
		this.formatString = formatString;
		format = null;
	}
	
	

}
