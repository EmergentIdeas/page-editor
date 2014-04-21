package com.emergentideas.page.editor.handles;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Layout;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

public class PublicPostsHandle {
	
	@Resource
	protected PostService postService;
	
	@Resource
	protected EntityManager entityManager;
	
	protected String allPostsTemplate = "page-editor-blog/all-posts";
	protected String specificPostsTemplate = "page-editor-blog/single-post";
	
	protected String defaultPostLayoutTemplate = "page-editor-blog/post";
	
	@Path("/posts")
	@Template
	@GET
	@Wrap("public_page")
	public Object allPosts(Location location) {
		
		List<Item> l = postService.getAllPublishedPostsMostRecentFirst();
		location.put("posts", l);
		
		for(Item item : l) {
			// Add a default layout so we can treat these consistently
			if(item.getLayout() == null) {
				entityManager.detach(item);
				Layout layout = new Layout();
				layout.setTemplateName(defaultPostLayoutTemplate);
				item.setLayout(layout);
			}
		}
		return allPostsTemplate;
	}
	
	@Path("/posts/{slug}")
	@Template
	@GET
	@Wrap("public_page")
	public Object showPosts(Location location, String slug) {
		Item item = postService.getPostBySlug(slug);
		location.put("post", item);
		location.add(item);
		
		if(item.getLayout() != null) {
			return item.getLayout().getTemplateName();
		}
		
		return specificPostsTemplate;
	}


}
