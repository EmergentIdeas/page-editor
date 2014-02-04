package com.emergentideas.page.editor.data;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Item {

	public enum ItemType { POST, PAGE, ATTACHMENT }
	
	public enum PubStatus { DRAFT, PUBLISH }
	
	@Id
	@GeneratedValue
	protected Integer id;
	
	@ManyToOne
	protected Author author;
	
	@Lob
	protected String title;
	
	@Lob
	protected String slug;
	
	protected Date pubDate;
	
	@Lob
	protected String description;
	
	@Lob
	protected String content;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	

	
}
