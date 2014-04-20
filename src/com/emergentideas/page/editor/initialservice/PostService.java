package com.emergentideas.page.editor.initialservice;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.data.Category;
import com.emergentideas.page.editor.data.Category.CategoryType;
import com.emergentideas.page.editor.data.Item.ItemType;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.SiteSet;

@Resource
public class PostService {
	
	@Resource
	protected EntityManager entityManager;
	
	public List<Item> getAllPublishedPostsMostRecentFirst() {
		return entityManager.createQuery("select i from Item i where type = :type and status = :status order by pubDate desc")
		.setParameter("type", ItemType.POST).setParameter("status", PubStatus.PUBLISH).getResultList();
	}
	
	public Author getAuthorByLoginName(String loginName) {
		List<Author> l = entityManager.createQuery("select a from Author a where loginName = :loginName").setParameter("loginName", loginName).getResultList();
		if(l.size() > 0) {
			return l.get(0);
		}
		
		return null;
	}
	
	public Category getCategory(String slug, CategoryType type) {
		List<Category> l = entityManager.createQuery("select c from Category c where slug = :slug").setParameter("slug", slug).getResultList();
		if(type == null) {
			if(l.size() > 0) {
				return l.get(0);
			}
		}
		
		for(Category c : l) {
			if(type.equals(c.getType())) {
				return c;
			}
		}
		
		return null;
	}
	
	public void save(SiteSet site) {
		for(Author author : site.getAuthors()) {
			save(author);
		}
		for(Category cat : site.getCategories()) {
			save(cat);
		}
		for(Item item : site.getItems()) {
			save(item);
		}
	}
	
	public void save(Author author) {
		entityManager.persist(author);
	}
	
	public void save(Category category) {
		entityManager.persist(category);
	}
	
	public void save(Item item) {
		entityManager.persist(item);
	}
	
	

}
