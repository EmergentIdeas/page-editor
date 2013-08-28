package com.emergentideas.page.editor.handles;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.emergentideas.webhandle.Constants;
import com.emergentideas.webhandle.Init;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.Name;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.Wire;
import com.emergentideas.webhandle.assumptions.oak.interfaces.AuthenticationService;
import com.emergentideas.webhandle.handlers.Handle;
import com.emergentideas.webhandle.output.DirectRespondent;
import com.emergentideas.webhandle.output.NoResponse;

import net.sf.webdav.IWebdavStore;
import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.WebDavServletBean;

@Handle("/{davPath:webdav}")
public class WebDav extends WebDavServletBean {

	protected ServletContext context;

	protected AuthenticationService authenticationService;

	/**
	 * The group a user must be in to read the content
	 */
	protected String requiredGroup = "administrators";

	/**
	 * The directory to serve
	 */
	protected String content = "static_content";

	@Init
	public void init(ServletContext context, Location location)
			throws ServletException {

		this.context = context;

		String clazzName = LocalFileSystemStore.class.getName();

		File spec = new File(content);
		File root = null;
		if (spec.isAbsolute()) {
			root = spec;
		} else {
			WebAppLocation webApp = new WebAppLocation(location);
			File rel = new File(
					(String) webApp
							.getServiceByName(Constants.APPLICATION_ON_DISK_LOCATION));
			root = new File(rel, content);
		}

		IWebdavStore webdavStore = constructStore(clazzName, root);

		super.init(webdavStore, null, null, 0, true);

	}

	@Handle("{path:.*}")
	public Object handle(HttpServletRequest request,
			HttpServletResponse response, String davPath, String path,
			@Name("Authorization") String authorization) throws IOException,
			ServletException {

		if (StringUtils.isBlank(authorization)) {
			return create401Response();
		}

		String userAndPass = new String(Base64.decodeBase64(authorization
				.substring("Basic ".length())));
		String[] parts = userAndPass.split(":");
		if (authenticationService.isAuthenticated(parts[0], parts[1]) == false) {
			return create401Response();
		}

		if (authenticationService.getUserByProfileName(parts[0]).getGroupNames().contains(requiredGroup) == false) {
			return create401Response();
		}

		if (path.contains("..") || path.startsWith("~")
				|| path.startsWith("/~")) {
			return new NoResponse();
		}
		request.setAttribute("javax.servlet.include.request_uri", path);
		request.setAttribute("javax.servlet.include.path_info", path);

		if (davPath == null) {
			davPath = "";
		}

		super.service(createRequestWithServletPath(request, "/" + davPath),
				response);

		return new NoResponse();

	}

	protected Object create401Response() {
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("WWW-Authenticate", "Basic realm=\"webdav\"");
		return new DirectRespondent(null, 401, headers);
	}

	protected HttpServletRequest createRequestWithServletPath(
			final HttpServletRequest request, final String path) {
		HttpServletRequest r = (HttpServletRequest) Proxy.newProxyInstance(
				HttpServletRequest.class.getClassLoader(),
				new Class[] { HttpServletRequest.class },
				new InvocationHandler() {

					@Override
					public Object invoke(Object target, Method method,
							Object[] args) throws Throwable {
						if (method.getName().equals("getServletPath")) {
							return path;
						}
						return method.invoke(request, args);
					}
				});
		return r;
	}

	protected IWebdavStore constructStore(String clazzName, File root) {
		IWebdavStore webdavStore;
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass(clazzName);

			Constructor<?> ctor = clazz
					.getConstructor(new Class[] { File.class });

			webdavStore = (IWebdavStore) ctor
					.newInstance(new Object[] { root });
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("some problem making store component", e);
		}
		return webdavStore;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	@Wire
	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public String getRequiredGroup() {
		return requiredGroup;
	}

	public void setRequiredGroup(String requiredGroup) {
		this.requiredGroup = requiredGroup;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
