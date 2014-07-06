package com.emergentideas.page.editor.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.emergentideas.page.editor.data.MenuItem;

public class MenuServiceTest {

	protected MenuService menuService = new MenuService();
	
	@Test
	public void testTreeItems() throws Exception {
		List<MenuItem> items = new ArrayList<MenuItem>();
		
		items.add(createItem("a", 0, 5));
		items.add(createItem("b", 1, 4));
		items.add(createItem("c", 2, 3));
		
		menuService.treeListOfItems(items);
		
		assertEquals(1, items.size());
		MenuItem item = items.get(0);
		assertEquals("a", item.getLabel());
		
		assertEquals(1, item.getChildren().size());
		
		item = item.getChildren().get(0);
		assertEquals("b", item.getLabel());
		assertEquals(1, item.getChildren().size());
		
		item = item.getChildren().get(0);
		assertEquals("c", item.getLabel());
		assertEquals(0, item.getChildren().size());
		
		
		items = new ArrayList<MenuItem>();
		
		items.add(createItem("a", 0, 5));
		items.add(createItem("b", 1, 2));
		items.add(createItem("c", 3, 4));
		
		menuService.treeListOfItems(items);
		
		assertEquals(1, items.size());
		item = items.get(0);
		assertEquals("a", item.getLabel());
		
		assertEquals(2, item.getChildren().size());
		
		List<MenuItem> children = item.getChildren();
		item = item.getChildren().get(0);
		assertEquals("b", item.getLabel());
		assertEquals(0, item.getChildren().size());
		
		item = children.get(1);
		assertEquals("c", item.getLabel());
		assertEquals(0, item.getChildren().size());
		
		
		
		items = new ArrayList<MenuItem>();
		
		items.add(createItem("a", 0, 1));
		items.add(createItem("b", 2, 5));
		items.add(createItem("c", 3, 4));
		
		menuService.treeListOfItems(items);
		
		assertEquals(2, items.size());
		item = items.get(0);
		assertEquals("a", item.getLabel());
		
		assertEquals(0, item.getChildren().size());
	}
	
	@Test
	public void testSetBounds() throws Exception {
		
		List<MenuItem> items = new ArrayList<MenuItem>();
		
		MenuItem root = createItem("a", -1, -1);
		items.add(root);
		
		MenuItem b = createItem("b", -1, -1);
		root.getChildren().add(b);
		
		MenuItem c = createItem("c", -1, -1);
		root.getChildren().add(c);
		
		menuService.addBounds(items, 0);
		
		assertEquals(0, root.getLeft());
		assertEquals(5, root.getRight());
		assertEquals(1, b.getLeft());
		assertEquals(2, b.getRight());
		assertEquals(3, c.getLeft());
		assertEquals(4, c.getRight());
		
		
		items = new ArrayList<MenuItem>();
		
		root = createItem("a", -1, -1);
		items.add(root);
		
		b = createItem("b", -1, -1);
		root.getChildren().add(b);
		
		c = createItem("c", -1, -1);
		root.getChildren().add(c);
		
		MenuItem d = createItem("d", -1, -1);
		root.getChildren().add(d);
		
		menuService.addBounds(items, 0);
		
		assertEquals(0, root.getLeft());
		assertEquals(7, root.getRight());
		assertEquals(1, b.getLeft());
		assertEquals(2, b.getRight());
		assertEquals(3, c.getLeft());
		assertEquals(4, c.getRight());
		assertEquals(5, d.getLeft());
		assertEquals(6, d.getRight());
		
	}
	
	protected MenuItem createItem(String label, int left, int right) {
		MenuItem item = new MenuItem();
		item.setLabel(label);
		item.setLeft(left);
		item.setRight(right);
		return item;
	}
}
