package com.emergentideas.page.editor.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Item {

	public enum ItemType { POST, PAGE, ATTACHMENT }
	
	public enum PubStatus { DRAFT, PUBLISH }
	
	@Id
	@GeneratedValue
	protected Integer id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	protected Author author;
	
	@Lob
	@Column(length = 1000)
	protected String title;
	
	@Lob
	@Column(length = 1000)
	protected String slug;
	
	protected Date pubDate;
	
	@Lob
	@Column(length = 2000)
	protected String description;
	
	@Lob
	@Column(length = 200000)
	protected String content;
	
	@ManyToMany(cascade = CascadeType.ALL)
	protected List<Category> categories = new ArrayList<Category>();
	
	protected ItemType type = ItemType.POST;
	
	protected PubStatus status = PubStatus.DRAFT;
	
	@ManyToOne
	protected Layout layout;
	
	@OneToMany(cascade = {CascadeType.ALL})
	protected List<Attachment> attachments = new ArrayList<Attachment>();

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

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public PubStatus getStatus() {
		return status;
	}

	public void setStatus(PubStatus status) {
		this.status = status;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	

	
}
