package com.emergentideas.page.editor.handles;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Comment;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Layout;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.webhandle.Inject;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.NotNull;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.assumptions.oak.interfaces.EmailService;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;
import com.emergentideas.webhandle.templates.TemplateInstance;

@Resource(type = PublicPostsHandle.class)
public class PublicPostsHandle {
	
	@Resource
	protected PostService postService;
	
	@Resource
	protected EmailService emailService;
	
	@Resource
	protected EntityManager entityManager;
	
	protected String allPostsTemplate = "page-editor-blog/all-posts";
	protected String specificPostsTemplate = "page-editor-blog/single-post";
	
	protected String defaultPostLayoutTemplate = "page-editor-blog/post";
	
	protected PubStatus defaultPubStatus = PubStatus.DRAFT;
	
	protected String commentNotificationEmail;
	
	protected String commentNotificationFromEmail = "comments@emergentideas.com";
	
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
			Boolean ajaxStyle, RequestMessages messages, HttpServletRequest request) {
		Item item = postService.getPostBySlug(slug);
		if(StringUtils.isNotBlank(commentObj.getComment())) {
			commentObj.setName(postService.scrubPostComment(commentObj.getName()));
			commentObj.setEmail(postService.scrubPostComment(commentObj.getEmail()));
			commentObj.setComment(postService.scrubPostComment(commentObj.getComment()));
			
			commentObj.setItem(item);
			commentObj.setSubmitted(new Date());
			item.getComments().add(commentObj);
			
			// commit the transaction so we can get the id for the comment
			entityManager.getTransaction().commit();
			entityManager.getTransaction().begin();
			
			sendNotificationEmail(request, location, commentObj);
		}
		if(Boolean.TRUE.equals(ajaxStyle)) {
			return "page-editor-blog/create-comment-success";
		}
		else {
			messages.getSuccessMessages().add("Your comment has been submitted.");
			messages.persistMessages();
			return new Show("/posts/" + slug);
		}
	}
	
	protected void sendNotificationEmail(HttpServletRequest request, Location location, Comment comment) {
		if(StringUtils.isNotBlank(commentNotificationEmail) && emailService != null) {
			location.put("commentUrl", createCommentPath(request, comment));
			location.add(comment);
			
			WebAppLocation wloc = new WebAppLocation(location);
			TemplateInstance ti = wloc.getTemplateSource().get("page-editor-blog/comment-notification-email");
			SegmentedOutput so = new SegmentedOutput();
			
			ti.render(so, location, "body", null);
			try {
				emailService.sendEmail(new String[] { commentNotificationEmail }, commentNotificationFromEmail, null, null, 
						"A New Comment", null, so.getStream("body").toString());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	protected String createCommentPath(HttpServletRequest request, Comment comment) {
		StringBuilder sb = new StringBuilder();
		String scheme = request.getScheme();
		int port = request.getServerPort();
		sb.append(scheme);
		sb.append("://");
		sb.append(request.getServerName());
		if(shouldIncludePort(scheme, port)) {
			// So, we don't have a standard port here
			sb.append(":" + port);
		}

		sb.append("/comment/" + comment.getId());
		return sb.toString();
	}
	
	protected boolean shouldIncludePort(String scheme, int port) {
		return !(("https".equals(scheme) && port == 443) || ("http".equals(scheme) && port == 80));
	}

	
	public void setDefaultPubStatus(String defaultPubStatus) {
		try {
			this.defaultPubStatus = PubStatus.valueOf(defaultPubStatus);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getCommentNotificationFromEmail() {
		return commentNotificationFromEmail;
	}

	public void setCommentNotificationFromEmail(String commentNotificationFromEmail) {
		this.commentNotificationFromEmail = commentNotificationFromEmail;
	}

	public String getCommentNotificationEmail() {
		return commentNotificationEmail;
	}

	public void setCommentNotificationEmail(String commentNotificationEmail) {
		this.commentNotificationEmail = commentNotificationEmail;
	}

	public String getAllPostsTemplate() {
		return allPostsTemplate;
	}

	public void setAllPostsTemplate(String allPostsTemplate) {
		this.allPostsTemplate = allPostsTemplate;
	}

	public String getSpecificPostsTemplate() {
		return specificPostsTemplate;
	}

	public void setSpecificPostsTemplate(String specificPostsTemplate) {
		this.specificPostsTemplate = specificPostsTemplate;
	}

	public String getDefaultPostLayoutTemplate() {
		return defaultPostLayoutTemplate;
	}

	public void setDefaultPostLayoutTemplate(String defaultPostLayoutTemplate) {
		this.defaultPostLayoutTemplate = defaultPostLayoutTemplate;
	}

	public PubStatus getDefaultPubStatus() {
		return defaultPubStatus;
	}

	public void setDefaultPubStatus(PubStatus defaultPubStatus) {
		this.defaultPubStatus = defaultPubStatus;
	}

	public void setDefaultPubStatusString(String defaultPubStatus) {
		if(defaultPubStatus == null) {
			this.defaultPubStatus = null;
		}
		else {
			try {
				this.defaultPubStatus = PubStatus.valueOf(defaultPubStatus);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	
}
