package com.emergentideas.page.editor.handles;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Comment;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.helpers.PageEditorConstants;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.webhandle.Inject;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.composites.db.Db;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

@Path("/comment")
@RolesAllowed({PageEditorConstants.ADMINISTRATOR_ROLE, PageEditorConstants.BLOG_EDITORS_ROLE})
@Resource
public class CommentHandle {

	@Resource
	protected PostService postService;
	
	@Path("/{id:\\d+}")
	@GET
	@Template
	@Wrap("app_page")
	public Object editForm(Location location, @Db("id") Comment comment) {
		
		location.add(comment);
		
		return "page-editor-blog/comment/comment-edit";
	}
	
	@Path("/{id:\\d+}")
	@POST
	@Template
	@Wrap("app_page")
	public Object saveComment(Location location, @Db("id") @Inject Comment comment, String save, String publish, String delete) {
		int itemId = comment.getItem().getId();
		
		if(StringUtils.isNotBlank(publish)) {
			comment.setStatus(PubStatus.PUBLISH);
		}
		else if(StringUtils.isNotBlank(delete)) {
			return deleteComment(location, comment);
		}
		
		return new Show("/post/" + itemId);
	}

	@Path("/{id:\\d+}/delete")
	@POST
	@Template
	@Wrap("app_page")
	public Object deleteComment(Location location, @Db("id") @Inject Comment comment) {
		int itemId = comment.getItem().getId();
		postService.deleteComment(comment);
		
		return new Show("/post/" + itemId);
	}


}
