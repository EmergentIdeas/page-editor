package com.emergentideas.page.editor.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.utils.CryptoUtils;

@Entity
public class Comment {
	@Id
	@GeneratedValue
	protected Integer id;

	protected PubStatus status = PubStatus.DRAFT;

	@Column(length = 400)
	protected String name;
	
	@Column(length = 400)
	protected String email;
	
	@Column(length = 40000)
	protected String comment;
	
	protected Date submitted;

	@ManyToOne
	protected Item item;
	
	public Comment() {}
	
	
	public String getGravatarImg() {
		if(StringUtils.isNotBlank(email)) {
			try {
				return "//www.gravatar.com/avatar/" + CryptoUtils.generateMD5Hash(email);
			}
			catch(Exception e) {
				
			}
		}
		return "//www.gravatar.com/avatar/00000000000000000000000000000000";
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PubStatus getStatus() {
		return status;
	}

	public void setStatus(PubStatus status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Date getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Date submitted) {
		this.submitted = submitted;
	}
	
	
}
