package com.emergentideas.page.editor.helpers;

public class MenuItem {

	protected String title;
	protected String description;
	protected String url;
	protected String image;
	protected String section;
	
	public MenuItem() {}
	

	public MenuItem(String title, String description, String url, String image,
			String section) {
		super();
		this.title = title;
		this.description = description;
		this.url = url;
		this.image = image;
		this.section = section;
	}


	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	
	
}
