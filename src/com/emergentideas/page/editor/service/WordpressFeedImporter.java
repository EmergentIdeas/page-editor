package com.emergentideas.page.editor.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.data.Category;
import com.emergentideas.page.editor.data.Category.CategoryType;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Item.ItemType;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.data.SiteSet;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.utils.DateUtils;

@Resource
public class WordpressFeedImporter {
	
	@Resource
	protected PostService postService;
	
	public SiteSet parseRSS(InputStream wordpressRss) throws ParserConfigurationException, IOException, SAXException, ParseException {
		
		SiteSet site = new SiteSet();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(wordpressRss);
	 
		doc.getDocumentElement().normalize();
		
		NodeList authorsList = doc.getElementsByTagName("wp:wp_author");
		for(int i = 0; i < authorsList.getLength(); i++) {
			Element authorElement = (Element)authorsList.item(i);
			String loginName = getElementContent(authorElement, "wp:author_login");
			Author author = getAuthor(site, loginName);
			if(author == null) {
				author = new Author();
				site.getAuthors().add(author);
				author.setLoginName(loginName);
				author.setEmail(getElementContent(authorElement, "wp:author_email"));
				author.setDisplayName(getElementContent(authorElement, "wp:author_display_name"));
				author.setFirstName(getElementContent(authorElement, "wp:author_first_name"));
				author.setLastName(getElementContent(authorElement, "wp:author_last_name"));
			}
		}
		
		NodeList categoriesList = doc.getElementsByTagName("wp:category");
		for(int i = 0; i < categoriesList.getLength(); i++) {
			Element categoryElement = (Element)categoriesList.item(i);
			String slug = getElementContent(categoryElement, "wp:category_nicename");
			Category cat = getCategory(site, slug, CategoryType.CATEGORY);
			if(cat == null) {
				cat = new Category();
				site.getCategories().add(cat);
				cat.setType(CategoryType.CATEGORY);
				cat.setSlug(slug);
				cat.setName(getElementContent(categoryElement, "wp:cat_name"));
			}
		}
		
		NodeList tagList = doc.getElementsByTagName("wp:tag");
		for(int i = 0; i < tagList.getLength(); i++) {
			Element categoryElement = (Element)tagList.item(i);
			String slug = getElementContent(categoryElement, "wp:tag_slug");
			Category cat = getCategory(site, slug, CategoryType.TAG);
			if(cat == null) {
				cat = new Category();
				site.getCategories().add(cat);
				cat.setType(CategoryType.TAG);
				cat.setSlug(slug);
				cat.setName(getElementContent(categoryElement, "wp:tag_name"));
			}
		}
		
		NodeList itemList = doc.getElementsByTagName("item");
		for(int i = 0; i < itemList.getLength(); i++) {
			Element itemElement = (Element)itemList.item(i);
			Item item = new Item();
			site.getItems().add(item);
			item.setContent(getElementContent(itemElement, "content:encoded"));
			item.setDescription(getElementContent(itemElement, "description"));
			item.setPubDate(parsePubDate(getElementContent(itemElement, "pubDate")));
			item.setSlug(getElementContent(itemElement, "wp:post_name"));
			item.setTitle(getElementContent(itemElement, "title"));
			
			addPostCategoriesToPostAndSite(itemElement, site, item);
			
			String itemTypeString = getElementContent(itemElement, "wp:post_type");
			item.setType(getItemType(itemTypeString));
			
			item.setStatus(getPubStatus(getElementContent(itemElement, "wp:status")));
			
			String authorShortName = getElementContent(itemElement, "dc:creator");
			Author author = getAuthor(site, authorShortName);
			item.setAuthor(author);
		}
		
		
		return site;
	}
	
	protected void addPostCategoriesToPostAndSite(Element itemElement, SiteSet site, Item item) {
		NodeList itemCategories = itemElement.getElementsByTagName("category");
		for(int catCount = 0; catCount < itemCategories.getLength(); catCount++) {
			Element catElement = (Element)itemCategories.item(catCount);
			CategoryType type = "category".equals(catElement.getAttribute("domain")) ? CategoryType.CATEGORY : CategoryType.TAG;
			String catSlug = catElement.getAttribute("nicename");
			Category cat = getCategory(site, catSlug, type);
			if(cat == null) {
				cat = new Category();
				cat.setType(type);
				cat.setSlug(catSlug);
				cat.setName(catElement.getTextContent());
				site.getCategories().add(cat);
			}
			item.getCategories().add(cat);
		}
		
	}
	
	protected ItemType getItemType(String itemTypeString) {
		ItemType itemType = null;
		if("post".equals(itemTypeString)) {
			itemType = ItemType.POST;
		}
		else if("page".equals(itemTypeString)) {
			itemType = ItemType.PAGE;
		}
		if("attachment".equals(itemTypeString)) {
			itemType = ItemType.ATTACHMENT;
		}
		
		return itemType;
	}
	
	protected PubStatus getPubStatus(String statusString) {
		if("draft".equals(statusString)) {
			return PubStatus.DRAFT;
		}
		if("publish".equals(statusString)) {
			return PubStatus.PUBLISH;
		}
		return PubStatus.DRAFT;
	}
	
	protected Date parsePubDate(String pubDateString) throws ParseException {
		// like Wed, 02 Jan 2013 17:06:49 +0000
		if(StringUtils.isNotBlank(pubDateString)) {
			return DateUtils.htmlExpiresDateFormat().parse(pubDateString);
		}
		
		return null;
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

	protected Category getCategory(SiteSet site, String name, CategoryType type) {
		Category cat = site.getCategory(name, type);
		if(cat == null) {
			cat = postService.getCategory(name, type);
			if(cat != null) {
				site.getCategories().add(cat);
			}
		}
		
		return cat;
	}

	protected String getElementContent(Element parent, String elementName) {
		StringBuilder sb = new StringBuilder();
		
		NodeList nl = parent.getElementsByTagName(elementName);
		if(nl.getLength() > 0) {
			Element child = (Element)nl.item(0);
			if(child != null) {
				sb.append(child.getTextContent());
//				NodeList textChildren = child.getChildNodes();
//				for(int i = 0; i < textChildren.getLength(); i++) {
//					sb.append(textChildren.item(i).getTextContent());
//				}
			}
			return sb.toString();
		}
		
		return null;
	}
	
//	protected String getElementContent2(Element parent, String elementName) {
//		StringBuilder sb = new StringBuilder();
//		
//		Element child = getChildElement(parent, elementName);
//		if(child != null) {
//			sb.append(child.getTextContent());
//			NodeList textChildren = child.getChildNodes();
//			for(int i = 0; i < textChildren.getLength(); i++) {
//				sb.append(textChildren.item(i).getTextContent());
//			}
//			return sb.toString();
//		}
//		
//		return null;
//	}
	
	protected Element getChildElement(Element parent, String childName) {
		NodeList nl = parent.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n instanceof Element) {
				if(childName.equals(n.getLocalName())) {
					return (Element)n;
				}
			}
		}
		
		return null;
	}

	public PostService getPostService() {
		return postService;
	}

	public void setPostService(PostService postService) {
		this.postService = postService;
	}
	
	
}
