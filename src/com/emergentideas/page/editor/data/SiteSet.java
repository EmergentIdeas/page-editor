package com.emergentideas.page.editor.data;

import java.util.ArrayList;
import java.util.List;

import com.emergentideas.page.editor.data.Category.CategoryType;

public class SiteSet {

	public List<Author> authors = new ArrayList<Author>();
	
	public List<Category> categories = new ArrayList<Category>();
	
	public List<Item> items = new ArrayList<Item>();

	public Author getAuthorByLogin(String loginName) {
		for(Author author : authors) {
			if(loginName.equals(author.getLoginName())) {
				return author;
			}
		}
		
		return null;
	}
	
	public Category getCategory(String slug, CategoryType type) {
		for(Category category : categories) {
			if(slug.equals(category.getSlug()) && (type == null || type.equals(category.getType()))) {
				return category;
			}
		}
		
		return null;
	}
	
	public Item getItemBySlug(String slug) {
		for(Item item : items) {
			if(slug.equals(item.getSlug())) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}
	
	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	
}
