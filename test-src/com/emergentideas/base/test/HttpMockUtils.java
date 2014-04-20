package com.emergentideas.base.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.session.StandardSession;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class HttpMockUtils {
	
	public static HttpServletResponse createResponse(final OutputStream out) throws Exception {
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		
		ServletOutputStream sout = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				out.write(b);
			}
		};
		
		Mockito.when(response.getOutputStream()).thenReturn(sout);
		
		return response;
	}
	
	public static HttpServletRequest createRequest(String url, String method, final Map<String, String[]> requestParameters) {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		
		Mockito.when(request.getParameterValues(Mockito.anyString())).then(new Answer<String[]>() {
			public String[] answer(InvocationOnMock invocation)
					throws Throwable {
				return requestParameters.get((String)invocation.getArguments()[0]);
			}
		});
		
		HttpSession session = new StandardSession(null) {
			protected Map<String, Object> attributes = new HashMap<String, Object>();

			@Override
			public Object getAttribute(String name) {
				return attributes.get(name);
			}

			@Override
			public void setAttribute(String name, Object value) {
				attributes.put(name, value);
			}
		};
		Mockito.when(request.getSession()).thenReturn(session);
		
		Mockito.when(request.getServletPath()).thenReturn(url);
		
		Mockito.when(request.getMethod()).thenReturn(method);

		
		return request;
	}
}
