package com.emergentideas.page.editor.service;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import com.emergentideas.page.editor.data.MenuItem;

@Resource(type = MenuService.class)
public class MenuService {
	
	@Resource
	protected EntityManager entityManager;
	
	/**
	 * Gets the flat list of menu items sorted by left bound.
	 * @param menuName The name of the menu
	 */
	public List<MenuItem> getItemsForMenu(String menuName) {
		List<MenuItem> items = entityManager.createQuery("select mi from MenuItem mi where menuName = :menuName order by mi.left").setParameter("menuName", menuName).getResultList();
		
		return items;
	}
	
	/**
	 * Gets the tree of menu items where the list returned contains all of the items at the root of the tree.
	 * @param menuName The name of the menu
	 */
	public List<MenuItem> getTreedItemsForMenu(String menuName) {
		List<MenuItem> items = getItemsForMenu(menuName);
		return treeListOfItems(items);
	}
	
	/**
	 * Takes a tree of menu items, assigns them correct right and left bounds, and saves them to the database,
	 * replacing all of the existing menu items with the new ones.
	 * @param menuName The menu to replace with the following items.
	 * @param items
	 */
	public void setMenu(String menuName, List<MenuItem> items) {
		entityManager.createQuery("delete from MenuItem mi where mi.menuName = :menuName").setParameter("menuName", menuName).executeUpdate();
		setMenuName(items, menuName);
		
		addBounds(items, 0);
		for(MenuItem item: items) {
			entityManager.persist(item);
		}
		
	}
	
	/**
	 * Sets the menu name for all items in the tree.
	 */
	protected void setMenuName(List<MenuItem> items, String menuName) {
		if(items == null) {
			return;
		}
		
		for(MenuItem item: items) {
			item.setMenuName(menuName);
			setMenuName(item.getChildren(), menuName);
		}
	}
	
	/**
	 * Sets the bounds and returns the right bound of the last item
	 * @param items
	 * @param starter
	 * @return
	 */
	protected int addBounds(List<MenuItem> items, int starter) {
		for(MenuItem item : items) {
			item.setLeft(starter++);
			starter = addBounds(item.getChildren(), starter) + 1;
			item.setRight(starter++);
		}
		
		return starter - 1;
	}
	
	protected List<MenuItem> treeListOfItems(List<MenuItem> items) {
		Stack<MenuItem> parents = new Stack<MenuItem>();
		
		Iterator<MenuItem> itItems = items.iterator();
		while(itItems.hasNext()) {
			MenuItem item = itItems.next();
			popParents(parents, item.getLeft());
			if(!parents.isEmpty()) {
				parents.peek().getChildren().add(item);
				itItems.remove();
			}
			parents.push(item);
		}
		
		return items;
	}
	
	protected void popParents(Stack<MenuItem> parents, int currentLeft) {
		while(!parents.isEmpty()) {
			if(parents.peek().getRight() < currentLeft) {
				parents.pop();
			}
			else {
				return;
			}
		}
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	
	
}
