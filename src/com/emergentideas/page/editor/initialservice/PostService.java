package com.emergentideas.page.editor.initialservice;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import com.emergentideas.page.editor.data.Author;
import com.emergentideas.page.editor.data.Category;
import com.emergentideas.page.editor.data.Category.CategoryType;
import com.emergentideas.page.editor.data.Item;

public class PostService {
	
	@Resource
	protected EntityManager entityManager;
	
	public Category getCategory(String name, CategoryType type) {
		return null;
	}
	
	public Author getAuthorByLoginName(String loginName) {
		List<Author> l = entityManager.createQuery("select a from Author a where loginName = :loginName").setParameter("loginName", loginName).getResultList();
		if(l.size() > 0) {
			return l.get(0);
		}
		
		return null;
	}
	
	
	
	public void save(Category category) {
		
	}
	
	public void save(Item item) {
		
	}
	
	

}
