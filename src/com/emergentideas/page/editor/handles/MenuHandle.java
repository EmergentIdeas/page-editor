package com.emergentideas.page.editor.handles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.emergentideas.page.editor.helpers.MenuItem;
import com.emergentideas.page.editor.helpers.PageEditorConstants;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

public class MenuHandle {
	
	@Resource
	protected PostsHandle postsHandle;
	
	@Resource
	protected LayoutHandle layoutHandle;
	
	@Resource
	protected AuthorHandle authorHandle;
	
	@Resource
	protected Object userManagementHandle;

	@GET
	@Path("/menu")
	@Template
	@Wrap("app_page")
	@RolesAllowed({PageEditorConstants.ADMINISTRATOR_ROLE, PageEditorConstants.PAGE_EDITORS_ROLE, PageEditorConstants.BLOG_EDITORS_ROLE})
	public Object menu(Location location, HttpServletRequest request) {
		List<MenuItem> items = new ArrayList<MenuItem>();
		location.put("menuItems", items);
		
		if(isAllowedPages(request)) {
			items.add(new MenuItem("View Files", "View, upload and delete files", "/files/view/", null, null));
			items.add(new MenuItem("Create Page", "Create a new page", "/files/create", null, null));
		}
		
		if(postsHandle != null && isAllowedBlogs(request)) {
			items.add(new MenuItem("Manage Posts", "Manage blog posts", "/post", null, null));
		}
		
		if(layoutHandle != null && isAllowedBlogs(request)) {
			items.add(new MenuItem("Manage Post Layouts", "Manage the layouts for blog posts", "/layout", null, null));
		}
		
		if(authorHandle != null && isAllowedBlogs(request)) {
			items.add(new MenuItem("Manage Blog Authors", "Manage the authors for blog posts", "/author", null, null));
		}
		
		if(userManagementHandle != null && request.isUserInRole(PageEditorConstants.ADMINISTRATOR_ROLE)) {
			items.add(new MenuItem("Manage Users", "Manage the system users", "/users", null, null));
			items.add(new MenuItem("Manage Groups", "Manage access groups", "/groups", null, null));
		}
		
		return "page-editor/menu-items";
	}
	
	protected boolean isAllowedBlogs(HttpServletRequest request) {
		return request.isUserInRole(PageEditorConstants.BLOG_EDITORS_ROLE) || request.isUserInRole(PageEditorConstants.ADMINISTRATOR_ROLE);
	}
	
	protected boolean isAllowedPages(HttpServletRequest request) {
		return request.isUserInRole(PageEditorConstants.PAGE_EDITORS_ROLE) || request.isUserInRole(PageEditorConstants.ADMINISTRATOR_ROLE);
	}

}
