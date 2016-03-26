package com.emergentideas.page.editor.handles;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.emergentideas.entityclasstools.NameParser;
import com.emergentideas.page.editor.helpers.SubmitParameter;
import com.emergentideas.page.editor.service.InputToStaticTextTransformer;
import com.emergentideas.webhandle.AppLocation;
import com.emergentideas.webhandle.Location;
import com.emergentideas.webhandle.WebAppLocation;
import com.emergentideas.webhandle.assumptions.oak.ParmManipulator;
import com.emergentideas.webhandle.assumptions.oak.RequestMessages;
import com.emergentideas.webhandle.assumptions.oak.interfaces.EmailService;
import com.emergentideas.webhandle.output.SegmentedOutput;
import com.emergentideas.webhandle.output.Show;
import com.emergentideas.webhandle.output.Template;
import com.emergentideas.webhandle.output.Wrap;
import com.emergentideas.webhandle.templates.TemplateInstance;
import com.emergentideas.webhandle.templates.TemplateSource;
import com.emergentideas.webhandle.transformers.InputValuesTransformer;

public class EmailContactHandle {

	protected String successPage = "/thank-you-for-contacting-us.html";
	protected String contactFormTemplateName = "page-editor-contact/contact-form";
	protected String verificationAnswer = "25";
	protected String contactEmailTemplate = "page-editor-contact/contact-email";
	protected NameParser nameParser = new NameParser();

	protected String[] emailTo = new String[0];
	protected String fromEmail = "contact@emergentideas.com";

	protected InputValuesTransformer transformer = new InputValuesTransformer();
	protected InputToStaticTextTransformer inputElementTransformer = new InputToStaticTextTransformer();
	protected boolean transformEmail = true;
	protected boolean replaceInputs = true;

	@Resource
	protected EmailService emailService;
	
	@Path("/contact-form")
	@GET
	@Template
	@Wrap("public_page")
	public Object getContactForm() {
		return getContactFormTemplate();
	}
	
	@Path("/contact-form")
	@POST
	@Template
	@Wrap("public_page")
	public Object contactFormPost(Location location, HttpServletRequest request, RequestMessages messages, ParmManipulator manip, String vrf, String contactInfo) {
		if(StringUtils.isBlank(vrf)) {
			messages.getErrorMessages().add("Please include an answer to the stop-spambots question.");
			manip.addRequestParameters(location);
			return getContactFormTemplate();
		}
		
		if(isSecurityQuestionAnswerCorrect(location, request, vrf) == false) {
			messages.getErrorMessages().add("Wait! Looks like your answer to the stop-spambots question wasn't what we expect. Would you please try again?");
			manip.addRequestParameters(location);
			return getContactFormTemplate();
		}
		
		if(StringUtils.isNotBlank(contactInfo) && contactInfo.toLowerCase().contains("mark357177")) {
			// blacklist a problem emailer
			return new Show(successPage);
		}
		
		Location loc = addParameterObjects(request);
		setFormName(loc);
		
		TemplateSource ts = new WebAppLocation(location).getServiceByType(TemplateSource.class);
		emailForm(loc, ts, getContactEmailTemplate());
		
		return new Show(successPage);
	}
	
	protected void setFormName(Location loc) {
		loc.put("formName", "A Contact From the Web");
	}
	
	protected void transformEmail(SegmentedOutput so, Location location) {
		
		transformer.transform(so, location);
		if(replaceInputs) {
			inputElementTransformer.transform(so, location);
		}
	}
	
	protected Location addParameterObjects(HttpServletRequest request) {
		Location loc = new AppLocation();
		loc.put("date", new Date().toString());
		
		List<SubmitParameter> parms = new ArrayList<SubmitParameter>();
		loc.put("formParameters", parms);
		
		Enumeration<String> parmNames = request.getParameterNames();
		while(parmNames.hasMoreElements()) {
			String name = parmNames.nextElement();
		
			String value = request.getParameter(name);
			if(StringUtils.isBlank(value)) {
				continue;
			}
			if("vrf".equals(name)) {
				continue;
			}
			
			parms.add(new SubmitParameter(nameParser.createUserLabel(name), Jsoup.clean(value, new Whitelist())));
		}
		
		return loc;
	}
	
	protected void emailForm(Location loc, TemplateSource ts, String templateName) {
		WebAppLocation wa = new WebAppLocation(loc);
		wa.setServiceByType(TemplateSource.class.getName(), ts);
		
		SegmentedOutput so = new SegmentedOutput();
		TemplateInstance ti = ts.get(templateName);
		ti.render(so, loc, null, null);
		
		if(transformEmail) {
			transformEmail(so, loc);
		}
		
		String body = so.getStream("body").toString();
		boolean result = emailService.sendEmail(emailTo, getFromEmail(loc), null, null, (String)loc.get("formName") + " - " + (String)loc.get("date"), null, body);
		if(! result) {
			// perhaps the from address wasn't good. Let's try with another we know is good.
			emailService.sendEmail(emailTo, fromEmail, null, null, (String)loc.get("formName") + " - " + (String)loc.get("date"), null, body);
		}
	}
	
	protected String getFromEmail(Location loc) {
		// Gets the from address from the submitted parameters
		String from = (String)loc.get("email");
		if(StringUtils.isBlank(from)) {
			from = fromEmail;
		}
		else {
			if(from.contains("@") == false || from.contains(".") == false) {
				from = fromEmail;
			}
		}
		
		return from;
	}
	

	
	protected boolean isSecurityQuestionAnswerCorrect(Location location, HttpServletRequest request, String answer) {
		return answer.trim().equals(getVerificationAnswer(location, request));
	}
	
	protected String getContactFormTemplate() {
		return contactFormTemplateName;
	}
	
	protected String getVerificationAnswer(Location location, HttpServletRequest request) {
		return verificationAnswer;
	}
	
	protected String getEmailFormTemplate(Location location, HttpServletRequest request) {
		return contactEmailTemplate;
	}
	
	public String[] getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		if(emailTo == null) {
			return;
		}
		if(emailTo.contains(",")) {
			this.emailTo = emailTo.split(",");
		}
		else {
			this.emailTo = new String[] { emailTo };
		}
	}

	public String getSuccessPage() {
		return successPage;
	}

	public void setSuccessPage(String successPage) {
		this.successPage = successPage;
	}

	public String getContactFormTemplateName() {
		return contactFormTemplateName;
	}

	public void setContactFormTemplateName(String contactFormTemplateName) {
		this.contactFormTemplateName = contactFormTemplateName;
	}

	public String getVerificationAnswer() {
		return verificationAnswer;
	}

	public void setVerificationAnswer(String verificationAnswer) {
		this.verificationAnswer = verificationAnswer;
	}

	public String getContactEmailTemplate() {
		return contactEmailTemplate;
	}

	public void setContactEmailTemplate(String contactEmailTemplate) {
		this.contactEmailTemplate = contactEmailTemplate;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	
}
