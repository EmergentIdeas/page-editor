package com.emergentideas.page.editor.handles;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

public class PublicPostsHandle {
	
	@Resource
	protected PostService postService;
	
	@Path("/posts")
	@Template
	@GET
	@Wrap("public_page")
	public Object allPosts(Location location) {
		location.put("posts", postService.getAllPublishedPostsMostRecentFirst());
		return "page-editor-blog/all-posts";
	}

}
