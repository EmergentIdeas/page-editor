package com.emergentideas.page.editor.handles;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import com.ei.tools.crud.CRUDHandle;
import com.emergentideas.page.editor.data.Category;

@Path("/blog-categories")
@RolesAllowed("administrators")
public class CategoriesHandle extends CRUDHandle<Category> {

	@Override
	public String getTemplatePrefix() {
		return "page-editor-blog/categories/";
	}
	
	

}
