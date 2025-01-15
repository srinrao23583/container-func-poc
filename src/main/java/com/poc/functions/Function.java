package com.poc.functions;

import java.util.Optional;

import org.jboss.aerogear.security.otp.Totp;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
	
	private static final String SITE = "https://authenticationtest.com/totpChallenge/";
	
    @FunctionName("AlertEMail")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        
    	context.getLogger().info("Java HTTP trigger processed a request.");
        
    	String flowMessage = "Started, ";    	
    	
    	// Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String input = request.getBody().orElse(query);
        
        String incidentLink = null;
        context.getLogger().info("Input Message:" + input);
        if (input != null) {
        	String urlBeginString = input.length() > 10 ? input.substring(input.indexOf("https")) : null;
        	context.getLogger().info("urlBeginString:" + urlBeginString);
        	if (urlBeginString != null) {
        		incidentLink = urlBeginString.substring(0, urlBeginString.indexOf("\""));
        		incidentLink = incidentLink.replaceAll("\\\\", "");
        		flowMessage = flowMessage + ", incidentLink:" + incidentLink + ", ";
        		context.getLogger().info("Incident Link:" + incidentLink);
        	}
        }
        
        try {
        
	        try (Playwright playwright = Playwright.create()) {
				flowMessage += "Created PR, ";
	            
	            Browser browser = playwright.chromium().launch();
	            flowMessage += "Launched chromium, ";
	            // Create a new page and navigate to a URL
	            Page page = browser.newPage();
	            page.navigate(incidentLink != null ? incidentLink : SITE);
	            Locator email = page.locator("#email");
	            email.fill("totp@authenticationtest.com");
	            flowMessage += "Filled email, ";
	            
	            Locator password = page.locator("#password");
	            password.fill("pa$$w0rd");
	            flowMessage += "Filled pwd, ";
	            
	            Locator totpmfa = page.locator("#totpmfa");
	            String otp = getMFACode();
	            totpmfa.fill(otp);
	            flowMessage += "Filled otp " + otp + ", ";
	            Locator submit = page.locator("xpath=//input[@type='submit']");
	            submit.click();
	            flowMessage += "Submitted, ";
	            Locator links = page.locator("h1");
	            String text = links.first().textContent();	            
	            flowMessage += "Result:" + text;
	            browser.close();
	        } catch(Exception e) {
	        	flowMessage += "Exception in playright, Error:" + e.getMessage();	
	        }
        } catch (Exception e) {
        	flowMessage += "Exception in function, Error:" + e.getMessage();			
		}
        
        context.getLogger().info("Response Message:" + flowMessage);

        if (input == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello from Azure Function, " + flowMessage).build();
        }
    }
    
    private String getMFACode() {
		Totp totp = new Totp("I65VU7K5ZQL7WB4E");
		String mfaCode = totp.now();
		System.out.println("getMFACode:" + mfaCode);
		return mfaCode;		
	}
}
