package com.emergentideas.page.editor.handles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.emergentideas.page.editor.helpers.MenuItem;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

public class MenuHandle {

	@GET
	@Path("/menu")
	@Template
	@Wrap("app_page")
	@RolesAllowed("page-editors")
	public Object menu(Location location) {
		List<MenuItem> items = new ArrayList<MenuItem>();
		location.put("menuItems", items);
		
		items.add(new MenuItem("View Files", "View, upload and delete files", "/files/view/", null, null));
		items.add(new MenuItem("Create Page", "Create a new page", "/files/create", null, null));
		
		
		return "page-editor/menu-items";
	}
}
