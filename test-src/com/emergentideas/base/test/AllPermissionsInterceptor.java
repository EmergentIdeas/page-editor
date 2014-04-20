package com.emergentideas.base.test;

import javax.servlet.http.HttpServletRequest;

import com.emergentideas.webhandle.ParameterMarshal;
import com.emergentideas.webhandle.PreRequest;

public class AllPermissionsInterceptor {

	@PreRequest
	public void setupUserInformationSource(ParameterMarshal marshal, HttpServletRequest request) throws Exception {
		AllPermissionsRequest wrapper = new AllPermissionsRequest(request);
		marshal.getContext().setFoundParameter(HttpServletRequest.class, wrapper);
	}
}
