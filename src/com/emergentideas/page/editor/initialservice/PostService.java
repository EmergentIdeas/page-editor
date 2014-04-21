package com.emergentideas.page.editor.initialservice;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.data.Category;
import com.emergentideas.page.editor.data.Category.CategoryType;
import com.emergentideas.page.editor.data.Item.ItemType;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Layout;
import com.emergentideas.page.editor.data.SiteSet;

@Resource
public class PostService {
	
	@Resource
	protected EntityManager entityManager;
	
	public List<Item> getAllPublishedPostsMostRecentFirst() {
		return getLastXPublishedPostsMostRecentFirst(null);
	}
	
	public List<Item> getLastXPublishedPostsMostRecentFirst(Integer numberOfPosts) {
		Query q = entityManager.createQuery("select i from Item i where type = :type and status = :status order by pubDate desc")
		.setParameter("type", ItemType.POST).setParameter("status", PubStatus.PUBLISH);
		
		if(numberOfPosts != null) {
			q.setMaxResults(numberOfPosts);
		}
		return q.getResultList();
	}
	
	public List<Item> getAllPostsMostRecentFirst() {
		return entityManager.createQuery("select i from Item i where type = :type order by id desc")
		.setParameter("type", ItemType.POST).getResultList();
	}
	
	public Item getPostBySlug(String slug) {
		Query q = entityManager.createQuery("select i from Item i where type = :type and status = :status and slug = :slug order by pubDate desc")
		.setParameter("type", ItemType.POST).setParameter("status", PubStatus.PUBLISH).setParameter("slug", slug);
		
		List<Item> l = q.getResultList();
		if(l.size() == 0) {
			return null;
		}
		return l.get(0);
	}
	
	public boolean hasLayouts() {
		return getLayouts().size() > 0;
	}
	
	public List<Layout> getLayouts() {
		return entityManager.createQuery("select l from Layout l").getResultList();
	}
	
	/**
	 * Returns an item identified by <code>id</code> or null if not found.
	 */
	public Item getItem(Integer id) {
		return entityManager.find(Item.class, id);
	}
	
	public Layout getLayout(Integer id) {
		if(id == null) {
			return null;
		}
		return entityManager.find(Layout.class, id);
	}
	
	public void remove(Object o) {
		entityManager.remove(o);
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
