package com.emergentideas.base.test;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AllPermissionsRequest extends HttpServletRequestWrapper {
	
	public AllPermissionsRequest(HttpServletRequest request) {
		super(request);
	}

	
	@Override
	public Principal getUserPrincipal() {
		return new Principal() {
			
			@Override
			public String getName() {
				return "administrator";
			}
		};
	}


	@Override
	public boolean isUserInRole(String role) {
		return true;
	}

	
}
