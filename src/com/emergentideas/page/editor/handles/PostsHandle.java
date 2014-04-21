package com.emergentideas.page.editor.handles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.page.editor.data.Attachment;
import com.emergentideas.page.editor.data.Item;
import com.emergentideas.page.editor.data.Item.PubStatus;
import com.emergentideas.page.editor.data.Layout;
import com.emergentideas.page.editor.data.SiteSet;
import com.emergentideas.page.editor.helpers.PageEditorConstants;
import com.emergentideas.page.editor.initialservice.PostService;
import com.emergentideas.page.editor.service.WordpressFeedImporter;
import com.emergentideas.webhandle.Inject;
import com.emergentideas.webhandle.InvocationContext;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.NotNull;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.apps.oak.crud.CRUDHandle;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.assumptions.oak.interfaces.User;
import com.emergentideas.webhandle.composites.db.Db;
import com.emergentideas.webhandle.files.StreamableResourceSink;
import com.emergentideas.webhandle.handlers.Handle;
import com.emergentideas.webhandle.handlers.HttpMethod;
import com.emergentideas.webhandle.output.DirectRespondent;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;

@Path("/post")
@RolesAllowed({PageEditorConstants.ADMINISTRATOR_ROLE, PageEditorConstants.BLOG_EDITORS_ROLE})
@Resource
public class PostsHandle extends CRUDHandle<Item> {
	
	@Resource
	protected PostService postService;

	protected String sinkName = "staticResources";
	protected String attachmentPrefix = "img/";

	@Override
	public String getTemplatePrefix() {
		return "page-editor-blog/item/";
	}
	
	@Path("/{id:\\d+}/delete-attachment/{attachmentId:\\d+}")
	@POST
	public Object deleteAttachement(Integer id, Integer attachmentId) {
		if(id == null || attachmentId == null) {
			return new DirectRespondent(null, 500, null);
		}
		
		Item item = postService.getItem(id);
		Iterator<Attachment> attachements = item.getAttachments().iterator();
		while(attachements.hasNext()) {
			Attachment attachment = attachements.next();
			if(attachmentId == attachment.getId()) {
				postService.remove(attachment);
				attachements.remove();
				break;
			}
		}
		postService.save(item);
		return new DirectRespondent(null, 200, null);
	}
	
	@Handle(value = "/create", method = HttpMethod.POST)
	@Template
	@Wrap("app_page")
	public Object createPost(InvocationContext context, @NotNull @Inject Item focus, Integer layoutId,
			FileItem file1, FileItem file2, FileItem file3, FileItem file4, FileItem file5, FileItem file6, FileItem file7, 
			Location location, RequestMessages messages) throws IOException {
		addAttachements(location, focus, new FileItem[] {file1, file2, file3, file4, file5, file6, file7});
		addPubDateIfNeeded(focus);
		focus.setLayout(postService.getLayout(layoutId));
		return super.createPost(context, focus, location, messages);
	}

	@Handle(value = "/{id:\\d+}", method = HttpMethod.POST)
	@Template
	@Wrap("app_page")
	public Object editPost(InvocationContext context, User user, @Db("id") @Inject Item focus, Integer layoutId,
			FileItem file1, FileItem file2, FileItem file3, FileItem file4, FileItem file5, FileItem file6, FileItem file7, 
			Location location, RequestMessages messages) throws IOException {
		addAttachements(location, focus, new FileItem[] {file1, file2, file3, file4, file5, file6, file7});
		addPubDateIfNeeded(focus);
		focus.setLayout(postService.getLayout(layoutId));
		return super.editPost(context, user, focus, location, messages);
	}

	
	@Override
	protected void addAssociatedData(InvocationContext context, Item focus,
			Location location) {
		super.addAssociatedData(context, focus, location);
		
		List<Layout> l = postService.getLayouts();
		if(l.size() > 0) {
			location.put("layouts", l);
		}
		
		if(focus != null && focus.getLayout() != null) {
			location.put("layoutId", focus.getLayout().getId());
		}
	}

	protected void addPubDateIfNeeded(Item focus) {
		if(focus.getPubDate() == null && focus.getStatus() == PubStatus.PUBLISH) {
			focus.setPubDate(new Date());
		}
	}
	
	protected void addAttachements(Location location, Item focus, FileItem[] files) throws IOException {
		
		for(FileItem file : files) {
			if(file == null) {
				continue;
			}
			if(file.getSize() == 0) {
				continue;
			}
			focus.getAttachments().add(new Attachment(focus, writeNewAttachment(location, file)));
		}
	}

	protected String writeNewAttachment(Location location, FileItem item) throws IOException {
		StreamableResourceSink sink = findSink(location, sinkName);
		String path = getRelativeFileName(getShortFileName(item));
		sink.write(path, item.get());
		
		if(path.startsWith("/") == false) {
			path = "/" + path;
		}
		
		return path;
	}
	
	protected String getShortFileName(FileItem item) {
		String name = item.getName();
		if(StringUtils.isBlank(name) || name.contains("..") || name.startsWith("~/")) {
			name = "attachment" + System.currentTimeMillis() + ".jpg";
		}
		else {
			name = name.replace("\\", "/");
			int i = name.lastIndexOf('/');
			if(i > -1) {
				name = name.substring(i + 1);
			}
		}
		
		return name;
	}

	protected StreamableResourceSink findSink(Location location, String sinkName) {
		return (StreamableResourceSink)new WebAppLocation(location).getServiceByName(sinkName);
	}

	protected String getRelativeFileName(String shortName) {
		return attachmentPrefix + shortName;
	}

	@Override
	public List<Item> findEntitiesToShow(InvocationContext context, User user,
			HttpServletRequest request) {
		return postService.getAllPostsMostRecentFirst();
	}


	@Override
	protected List<String> determinePropertyNames() {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("slug");
		properties.add("title");
		properties.add("status");
		properties.add("pubDate");
		
		return properties;
	}

	@Path("/upload-wordpress-file")
	@GET
	@Template
	@Wrap("app_page")
	public Object uploadForm() {
		
		return "page-editor-blog/upload-wordpress-file";
	}
	
	@Path("/upload-wordpress-file")
	@POST
	public Object uploadForm(InvocationContext context, Item focus, Location location, RequestMessages messages, FileItem file) throws Exception {
		WordpressFeedImporter wfi = new WordpressFeedImporter();
		wfi.setPostService(postService);
		
		SiteSet site = wfi.parseRSS(file.getInputStream());
		
		postService.save(site);

		return new Show(getPostCreateURL(context, focus, location, messages));
	}

	public String getSinkName() {
		return sinkName;
	}

	public void setSinkName(String sinkName) {
		this.sinkName = sinkName;
	}

	public String getImagePrefix() {
		return attachmentPrefix;
	}

	public void setImagePrefix(String imagePrefix) {
		this.attachmentPrefix = imagePrefix;
	}

	
}
