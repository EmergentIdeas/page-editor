package com.emergentideas.page.editor.handles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.fileupload.FileItem;

import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.SiteSet;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.page.editor.service.WordpressFeedImporter;
import com.emergentideas.webhandle.InvocationContext;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.apps.oak.crud.CRUDHandle;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

@Path("/post")
public class PostsHandle extends CRUDHandle<Item> {
	
	@Resource
	protected PostService postService;

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

	@Path("/upload-wordpress-file")
	@GET
	@Template
	@Wrap("app_page")
	public Object uploadForm() {
		
		return "page-editor-blog/upload-wordpress-file";
	}
	
	@Path("/upload-wordpress-file")
	@POST
	public Object uploadForm(InvocationContext context, Item focus, Location location, RequestMessages messages, FileItem file) throws Exception {
		WordpressFeedImporter wfi = new WordpressFeedImporter();
		wfi.setPostService(postService);
		
		SiteSet site = wfi.parseRSS(file.getInputStream());
		
		postService.save(site);

		return new Show(getPostCreateURL(context, focus, location, messages));
	}

}
