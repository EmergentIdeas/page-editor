package com.emergentideas.page.editor.data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class Category {
	
	public enum CategoryType { CATEGORY, TAG }
	
	@Id
	@GeneratedValue
	protected Integer id;
	
	protected String slug;
	
	protected String name;
	
	protected CategoryType type;
	
	@ManyToOne
	protected Category category;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public CategoryType getType() {
		return type;
	}

	public void setType(CategoryType type) {
		this.type = type;
	}
	
	

}
