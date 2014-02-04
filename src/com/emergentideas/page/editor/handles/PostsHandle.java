package com.emergentideas.page.editor.handles;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import com.emergentideas.page.editor.data.Item;
import com.emergentideas.webhandle.apps.oak.crud.CRUDHandle;

@Path("/post")
public class PostsHandle extends CRUDHandle<Item> {

	@Override
	public String getTemplatePrefix() {
		return "page-editor-blog/item/";
	}

	@Override
	protected List<String> determinePropertyNames() {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("slug");
		properties.add("status");
		properties.add("pubDate");
		
		return properties;
	}

	
}
