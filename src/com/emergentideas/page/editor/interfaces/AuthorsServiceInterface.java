package com.emergentideas.page.editor.interfaces;

import java.util.List;

import com.emergentideas.page.editor.data.AuthorInterface;

public interface AuthorsServiceInterface {

	public List<AuthorInterface> getAuthors();
	
	public AuthorInterface getAuthor(int id);
}
