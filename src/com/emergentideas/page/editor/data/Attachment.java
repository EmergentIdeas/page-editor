package com.emergentideas.page.editor.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class Attachment {

	@Id
	@GeneratedValue
	protected Integer id;
	
	@Lob
	@Column(length = 1000)
	protected String url;
	
	@ManyToOne
	protected Item item;
	
	public Attachment() {}

	public Attachment(Item item, String url) {
		super();
		this.url = url;
		this.item = item;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
	
}
