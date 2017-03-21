package com.emergentideas.page.editor.data;

public class Link {

	protected String label;
	protected String url;
	
	public Link() {}
	
	
	public Link(String label, String url) {
		super();
		this.label = label;
		this.url = url;
	}


	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
