package com.emergentideas.page.editor.init;

import com.emergentideas.webhandle.Init;
import com.emergentideas.webhandle.Wire;
import com.emergentideas.webhandle.assumptions.oak.interfaces.AuthenticationService;

public class UpdateAdminProfile {
	AuthenticationService authenticationService;

	@Init
	public void init() {
		
		if(authenticationService.getGroupNames().contains("page-editors") == false) {
			authenticationService.createGroup("page-editors");
			if(authenticationService.getUserByProfileName("administrator") != null) {
				authenticationService.addMember("page-editors", "administrator");
			}
		}
	}
	
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	@Wire
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
}

