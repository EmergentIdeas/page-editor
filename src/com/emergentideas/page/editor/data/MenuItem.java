package com.emergentideas.page.editor.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class MenuItem {

	@Id
	@GeneratedValue
	protected Integer id;
	
	@Column(length = 1000)
	protected String menuName;
	
	@Column(length = 1000)
	protected String label;
	
	@Column(length = 1000)
	protected String customClasses;
	
	@Column(length = 10000)
	protected String itemUrl;
	
	protected int leftBound;
	
	protected int rightBound;
	
	@Transient
	protected List<MenuItem> children = new ArrayList<MenuItem>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCustomClasses() {
		return customClasses;
	}

	public void setCustomClasses(String customClasses) {
		this.customClasses = customClasses;
	}

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}

	public int getLeft() {
		return leftBound;
	}

	public void setLeft(int left) {
		this.leftBound = left;
	}

	public int getRight() {
		return rightBound;
	}

	public void setRight(int right) {
		this.rightBound = right;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public void setChildren(List<MenuItem> children) {
		this.children = children;
	}
}
