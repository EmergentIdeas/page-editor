package com.emergentideas.page.editor.handles;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.helpers.PageEditorConstants;
import com.emergentideas.webhandle.apps.oak.crud.CRUDHandle;

@Path("/author")
@RolesAllowed({PageEditorConstants.ADMINISTRATOR_ROLE, PageEditorConstants.BLOG_EDITORS_ROLE})
@Resource
public class AuthorHandle extends CRUDHandle<Author> {

	@Override
	public String getTemplatePrefix() {
		return "page-editor-blog/author/";
	}

	
}
