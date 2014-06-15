package com.emergentideas.page.editor.handles;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("/menu/test1")
public class EditorTest {

	@Path("")
	@GET
	public Object getData() {
		return "[\n" + 
				"	{\n" + 
				"		\"id\": \"i1\",\n" + 
				"        \"label\": \"node1\",\n" + 
				"        \"children\": [\n" + 
				"            { \"id\": \"i2\", \"label\": \"child1\" },\n" + 
				"            { \"id\": \"i3\", \"label\": \"child2\" }\n" + 
				"        ],\n" + 
				"        \"customClasses\": \"one two\",\n" + 
				"        \"itemUrl\": \"/index.html\"\n" + 
				"    },\n" + 
				"    {\n" + 
				"		\"id\": \"i4\",\n" + 
				"        \"label\": \"node2\",\n" + 
				"        \"children\": [\n" + 
				"            { \"id\": \"i5\", \"label\": \"child3\" }\n" + 
				"        ]\n" + 
				"    }\n" + 
				"]";
	}
	
	@Path("")
	@PUT
	public Object updateData(String data) {
		System.out.println(data);
		return "";
	}
}
