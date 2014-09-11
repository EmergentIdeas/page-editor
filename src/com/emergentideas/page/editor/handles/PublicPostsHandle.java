package com.emergentideas.page.editor.handles;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Comment;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Layout;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.webhandle.Inject;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.NotNull;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.output.Show;
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
	
	protected PubStatus defaultPubStatus = PubStatus.DRAFT;
	
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

	@Path("/posts/{slug}/comment")
	@Template
	@POST
	public Object createDraftPost(Location location, String slug, @NotNull @Inject Comment commentObj,
			Boolean ajaxStyle, RequestMessages messages) {
		Item item = postService.getPostBySlug(slug);
		if(StringUtils.isNotBlank(commentObj.getName())) {
			commentObj.setName(StringEscapeUtils.escapeHtml(commentObj.getName()));
		}
		if(StringUtils.isNotBlank(commentObj.getEmail())) {
			commentObj.setEmail(StringEscapeUtils.escapeHtml(commentObj.getEmail()));
		}
		if(StringUtils.isNotBlank(commentObj.getComment())) {
			commentObj.setComment(StringEscapeUtils.escapeHtml(commentObj.getComment()));
		}
		
		commentObj.setItem(item);
		commentObj.setSubmitted(new Date());
		item.getComments().add(commentObj);
		
		if(Boolean.TRUE.equals(ajaxStyle)) {
			return "page-editor-blog/create-comment-success";
		}
		else {
			messages.getSuccessMessages().add("Your comment has been submitted.");
			messages.persistMessages();
			return new Show("/posts/" + slug);
		}
	}
	
	public void setDefaultPubStatus(String defaultPubStatus) {
		try {
			this.defaultPubStatus = PubStatus.valueOf(defaultPubStatus);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
