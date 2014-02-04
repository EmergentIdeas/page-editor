package com.emergentideas.page.editor.service;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.data.SiteSet;
import com.emergentideas.page.editor.initialservice.PostService;

public class WordpressFeedImporter {
	
	@Resource
	protected PostService postService;
	
	protected SiteSet parseRSS(InputStream wordpressRss) throws ParserConfigurationException, IOException, SAXException {
		
		SiteSet site = new SiteSet();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(wordpressRss);
	 
		doc.getDocumentElement().normalize();
		
		NodeList authorsList = doc.getElementsByTagNameNS("*", "wp_author");
		for(int i = 0; i < authorsList.getLength(); i++) {
			Element authorElement = (Element)authorsList.item(i);
			String loginName = getElementContent(authorElement, "author_login");
			Author author = getAuthor(site, loginName);
			if(author == null) {
				author = new Author();
				site.getAuthors().add(author);
				author.setLoginName(loginName);
				author.setEmail(getElementContent(authorElement, "author_email"));
				author.setDisplayName(getElementContent(authorElement, "author_display_name"));
				author.setFirstName(getElementContent(authorElement, "author_first_name"));
				author.setLastName(getElementContent(authorElement, "author_last_name"));
			}
		}
		
		
		
		return site;
	}
	
	protected Author getAuthor(SiteSet site, String loginName) {
		Author author = site.getAuthorByLogin(loginName);
		if(author == null) {
			author = postService.getAuthorByLoginName(loginName);
			if(author != null) {
				site.getAuthors().add(author);
			}
		}
		
		return author;
	}

	protected String getElementContent(Element parent, String elementName) {
		Element child = (Element)parent.getElementsByTagNameNS("*", elementName);
		if(child != null) {
			return child.getTextContent();
		}
		
		return null;
	}
}
