package com.emergentideas.base.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emergentideas.utils.StringUtils;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.assumptions.oak.AppLoader;
import com.emergentideas.webhandle.assumptions.oak.HandleCaller;

public class FullEnvTestCase {
	
	protected AppLoader loader;
	protected WebAppLocation webAppLocation;
	protected HandleCaller caller;
	protected ServletContext servletContext;
	
	public FullEnvTestCase() {
		
		try {
			loader = new AppLoader();
			servletContext = new TestServletContext();
			
			webAppLocation = new WebAppLocation(loader.getLocation());
			webAppLocation.setServiceByType(ServletConfig.class.getName(), mock(ServletConfig.class));
			webAppLocation.setServiceByType(ServletContext.class.getName(), servletContext);
			
			
			loader.load(getInitialConfiguration(), getProjectRoot());
			
			
			caller = (HandleCaller)webAppLocation.getServiceByName("request-handler");
			
			
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Call this to get an entity manager ready to be used for database operations.
	 * @return
	 */
	protected EntityManager setupTransaction() {
		EntityManager em = webAppLocation.getServiceByType(EntityManager.class);
		assertNotNull(em);

		em.getTransaction().begin();
		
		return em;
		
	}
	
	/** 
	 * Call to commit the work done.
	 */
	protected void commit() {
		EntityManager em = webAppLocation.getServiceByType(EntityManager.class);
		em.getTransaction().commit();
		
	}
	
	/** 
	 * Call to rollback the work done. This is probably more typical.
	 */
	protected void rollback() {
		EntityManager em = webAppLocation.getServiceByType(EntityManager.class);
		em.getTransaction().rollback();
	}
	
	/**
	 * Calls a <code>path</code> with the given <code>method</code> (GET, POST, etc) with the optional
	 * parameter map and returns the result.
	 * @param path
	 * @param method
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	protected String call(String path, String method, Map<String, String[]> parameters) throws Exception {
		if(parameters == null) {
			parameters = new HashMap<String, String[]>();
		}
		
		final ByteArrayOutputStream realOut = new ByteArrayOutputStream();
		
		HttpServletRequest request = HttpMockUtils.createRequest(path, method, parameters);
		HttpServletResponse response = HttpMockUtils.createResponse(realOut);
		
		caller.respond(servletContext, request, response);
		
		String result = new String(realOut.toByteArray());
		return result;
	}
	
	protected InputStream getInitialConfiguration() throws FileNotFoundException {
		return new FileInputStream(new File(new File("").getAbsoluteFile(), "test-plugin-app.conf"));
		
		// also, you could load something from the class path using code like:
		// return StringUtils.getStreamFromClassPathLocation("com/emergentideas/base/test/testConf1.conf");
	}
	
	protected File getProjectRoot() {
		return new File("").getAbsoluteFile();
	}

}
