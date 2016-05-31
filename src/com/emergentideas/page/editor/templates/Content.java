package com.emergentideas.page.editor.templates;

import static com.emergentideas.webhandle.Constants.APPLICATION_ON_DISK_LOCATION;

import java.io.File;

import org.apache.commons.io.IOUtils;

import com.emergentideas.utils.FileUtils;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.files.FileStreamableResourceSource;
import com.emergentideas.webhandle.files.Resource;
import com.emergentideas.webhandle.files.StreamableResource;
import com.emergentideas.webhandle.files.StreamableResourceSource;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.templates.TemplateDef;
import com.emergentideas.webhandle.templates.TemplateInstance;

@TemplateDef("page-editor/content")
public class Content implements TemplateInstance {
	
	protected StreamableResourceSource source;

	@Override
	public void render(SegmentedOutput output, Location location,
			String elementSourceName, String... processingHints) {
		try {
			Object o = location.get("$this");
			if(o != null && o instanceof String) {
				StreamableResource r = (StreamableResource)getSource(location).get((String)o);
				if(r == null) {
					return;
				}
				String s = IOUtils.toString(r.getContent());
				output.getStream(elementSourceName).append(s);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	protected StreamableResourceSource getSource(Location location) {
		if(source == null) {
			WebAppLocation webApp = new WebAppLocation(location);
			String appLocation = (String)webApp.getServiceByName(APPLICATION_ON_DISK_LOCATION);
			File root = new File(appLocation);
			source = new FileStreamableResourceSource(root);
		}
		return source;
	}
}
