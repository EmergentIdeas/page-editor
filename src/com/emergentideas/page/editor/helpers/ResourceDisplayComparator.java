package com.emergentideas.page.editor.helpers;

import java.util.Comparator;

import com.emergentideas.webhandle.files.Directory;
import com.emergentideas.webhandle.files.NamedResource;
import com.emergentideas.webhandle.files.Resource;

public class ResourceDisplayComparator implements Comparator<Resource> {

	@Override
	public int compare(Resource one, Resource two) {
		
		if(one == null || two == null) {
			return 0;
		}
		
		if(one instanceof Directory && two instanceof Directory) {
			return compareByName(one, two);
		}
		
		if(one instanceof Directory && two instanceof Directory == false) {
			return -1;
		}
		else if(one instanceof Directory == false && two instanceof Directory) {
			return 1;
		}
		
		return compareByName(one, two);
	}

	protected int compareByName(Resource one, Resource two) {
		return getName(one).compareTo(getName(two));
	}
	
	protected String getName(Resource r) {
		if(r instanceof NamedResource) {
			return ((NamedResource)r).getName().toLowerCase();
		}
		return "";
	}
}
