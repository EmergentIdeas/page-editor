package com.emergentideas.page.editor.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.emergentideas.utils.BindingUtils;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;

@Resource
public class InputToStaticTextTransformer {

	protected String anyChar = "[\\s\\S]";
	protected String anyNotGreaterThan = "[!>]";
	
	protected String inputExpression = "<input " + anyChar + "*?>";
	protected Pattern inputPattern = Pattern.compile(inputExpression);
	
	protected String valueExpression = "value=\"(.*?)\"";
	protected Pattern valuePattern = Pattern.compile(valueExpression);
	
	protected String selectExpression = "<select " + anyChar + "*?</select>";
	protected Pattern selectPattern = Pattern.compile(selectExpression);
	
	protected String optionExpression = "<option.*?selected=.*?>(" + anyChar + "*?)</option>";
	protected Pattern optionPattern = Pattern.compile(optionExpression);
	
	protected String textareaExpression = "<textarea" + anyChar + "*?>(" + anyChar + "*?)</textarea>";
	protected Pattern textareaPattern = Pattern.compile(textareaExpression);

	protected String labelExpression = "<label " + anyNotGreaterThan + "*?for=\".*?\"" + anyNotGreaterThan + "*?>" + anyChar + "*?</label>";
	protected Pattern labelPattern = Pattern.compile(labelExpression);
	
	public void transform(SegmentedOutput output, Location location) {
		StringBuilder sb = output.getStream("body");
		String result = rewrite(sb.toString());
		sb.delete(0, sb.length());
		sb.append(result);
	}
	
	public String rewrite(String html) {
		Map<String, String> replacements = new HashMap<String, String>();
		
		// input
		Matcher m = inputPattern.matcher(html);
		while(m.find()) {
			String part = m.group();
			
			String value = "";
			if(shouldGetValue(part)) {
				Matcher mValue = valuePattern.matcher(part);
				if(mValue.find()) {
					value = "<strong>" + mValue.group(1) + "</strong>";
					
				}
			}
			replacements.put(part, value);
		}
		
		// select
		m = selectPattern.matcher(html);
		while(m.find()) {
			String part = m.group();
			Matcher mValue = optionPattern.matcher(part);
			String value = "";
			if(mValue.find()) {
				value = "<strong>" + mValue.group(1) + "</strong>";
				
			}
			replacements.put(part, value);
		}
		
		
		m = textareaPattern.matcher(html);
		while(m.find()) {
			String part = m.group();
			String value = m.group(1);
			replacements.put(part, "<strong>" + value + "</strong>");
		}

		for(String key : replacements.keySet()) {
			html = html.replace(key, replacements.get(key));
		}
		
		return html;
	}
	
	public String removeLabelWithFor(String html) {
		Map<String, String> replacements = new HashMap<String, String>();
		
		// input
		Matcher m = labelPattern.matcher(html);
		while(m.find()) {
			String part = m.group();
			
			replacements.put(part, "");
		}
		
		for(String key : replacements.keySet()) {
			html = html.replace(key, replacements.get(key));
		}
		
		return html;
	}
	
	/**
	 * Returns true if the value type radio or checkbox and is checked or the input is some other type.
	 * @param value
	 * @return
	 */
	protected boolean shouldGetValue(String value) {
		if(value.contains("\"radio\"") || value.contains("\"checkbox\"")) {
			return value.contains("\"checked\"");
		}
		else {
			return true;
		}
	}
}
