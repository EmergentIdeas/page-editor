package com.emergentideas.page.editor.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

@Entity
public class Item {

	public enum ItemType { POST, PAGE, ATTACHMENT }
	
	public enum PubStatus { DRAFT, PUBLISH }
	
	@Id
	@GeneratedValue
	protected Integer id;
	
	@ElementCollection
	protected List<Integer> authors = new ArrayList<Integer>();
	
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
	
	@Lob
	protected String postImage; 
	
	@ManyToMany(cascade = CascadeType.ALL)
	protected List<Category> categories = new ArrayList<Category>();
	
	protected ItemType type = ItemType.POST;
	
	protected PubStatus status = PubStatus.DRAFT;
	
	@OneToMany(cascade = CascadeType.ALL)
	@OrderColumn(name = "submitted")
	protected List<Comment> comments = new ArrayList<Comment>();
	
	@ManyToOne
	protected Layout layout;
	
	@OneToMany(cascade = {CascadeType.ALL})
	protected List<Attachment> attachments = new ArrayList<Attachment>();
	
	public Integer getPrimaryAuthorId() {
		if(authors != null && authors.size() > 0) {
			return authors.get(0);
		}
		return null;
	}

	public int getCommentCount() {
		return comments.size();
	}
	
	public int getPublishedCommentCount() {
		return getPublishedComments().size();
	}
	
	public List<Comment> getPublishedComments() {
		List<Comment> published = new ArrayList<Comment>();
		for(Comment comment : comments) {
			if(comment.getStatus() == PubStatus.PUBLISH) {
				published.add(comment);
			}
		}
		
		return published;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

//	public AuthorInterface getAuthor() {
//		return author;
//	}
//
//	public void setAuthor(AuthorInterface author) {
//		this.author = author;
//	}
	
	

	public String getTitle() {
		return title;
	}

	public List<Integer> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Integer> authors) {
		this.authors = authors;
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

	public List<Comment> getComments() {
		return comments;
	}

	public String getPostImage() {
		return postImage;
	}

	public void setPostImage(String postImage) {
		this.postImage = postImage;
	}

	
}
