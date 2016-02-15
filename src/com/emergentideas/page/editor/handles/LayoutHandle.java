package com.emergentideas.page.editor.handles;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import com.ei.tools.crud.CRUDHandle;
import com.emergentideas.page.editor.data.Layout;

@Path("/layout")
@RolesAllowed({"administrators", "blog-editors"})
@Resource
public class LayoutHandle extends CRUDHandle<Layout> {

	@Override
	public String getTemplatePrefix() {
		return "page-editor-blog/layout/";
	}
	
}
