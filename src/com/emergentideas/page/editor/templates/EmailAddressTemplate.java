package com.emergentideas.page.editor.templates;


import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/email-address")
public class EmailAddressTemplate implements TemplateInstance {

	protected Random rand = new Random();
	
	@Override
	public void render(SegmentedOutput output, Location location, String elementSourceName, String... processingHints) {
		Object addrObj = location.get("$this");
		String addr = null;
		String name = null;
		if(addrObj != null) {
			if(addrObj instanceof String) {
				addr = addrObj.toString();
				name = addr;
			}
			else if(addrObj instanceof List) {
				List l = (List)addrObj;
				switch (l.size()) {
				case 2:
					name = l.get(1).toString();
				case 1:
					addr = l.get(0).toString();
				}
			}
			else if(addrObj instanceof Object[]) {
				Object[] l = (Object[])addrObj;
				switch (l.length) {
				case 2:
					name = l[1].toString();
				case 1:
					addr = l[0].toString();
				}
			}
			else {
				addr = addrObj.toString();
			}
			
			
			if(name == null) {
				name = addr;
			}
			
			
			if(StringUtils.isNotBlank(addr)) {
				StringBuilder sb = output.getStream(elementSourceName);
				
				int divide = rand.nextInt(addr.length() - 2) + 1;
				int divide2 = rand.nextInt(name.length() - 2) + 1;
				
				String s = "var mpr = '&#109;a' + 'i&#108;' + '&#116;o';\n" + 
						" var pattr = 'hr' + 'ef' + '=';\n" + 
						" var abcd = '" + getEntities(addr.substring(0, divide)) + "';\n" + 
						" abcd = abcd + '" + getEntities(addr.substring(divide)) + "';\n" + 
						" var abcde = '" + getEntities(name.substring(0, divide2)) + "';\n" + 
						" abcde = abcde + '" + getEntities(name.substring(divide2)) + "';\n" + 
						" document.write('<a ' + pattr + '\\'' + mpr + ':' + abcd + '\\'>');\n" + 
						" document.write(abcde);\n" + 
						" document.write('<\\/a>');";
				sb.append("<script type=\"text/javascript\">\n<!--\n" + s + "\n-->\n</script>");
			}
		}
	}
	
	protected String getEntities(String text) {
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < text.length(); i++) {
			result.append("&#" + text.codePointAt(i) + ";");
		}
		
		return result.toString();
	}


	protected void addTextAsScript(StringBuilder sb, String text) {
		for(int i = 0; i < text.length(); i++) {
			if(i % 10 == 0 ) {
				if(i != 0) {
					sb.append("'); ");
				}
				sb.append("document.write('");
			}
			sb.append("&#" + text.codePointAt(i) + ";");
		}
		
		sb.append("'); ");
	}
}
