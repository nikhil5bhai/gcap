package com.americanexpress.dc.mock;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.mockito.MockitoAnnotations;

import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.soap.impl.SoapConsumer;
import com.americanexpress.dc.util.GenerteToken;
import com.americanexpress.dc.util.ServiceInvoker;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class SoapRequestBuilder {

	SoapConsumer soapConsumer;
	
	String response=null;
	
	public SoapRequestBuilder(SoapConsumer soapConsumer) {
		this.soapConsumer = soapConsumer;
	}
	
	/**
	 * {@link callSoapService} method calls Soap service and uploads the same in Mongo.
	 * @param endpointUrl 
	 * @param collectionName 
	 * @param db  
	 * @param accountsBeans 
	 * @param uploadFlag 
	 * @throws Exception 
	 */
	
	public void callSoapService(final MongoDatabase db, final String collectionName, final String endpointUrl, final List<UserAccountsBean> accountsBeans, String uploadFlag, final String serviceName) throws Exception   {
		
        System.setProperty("spring.profiles.active", "E1_QA");
		System.setProperty("Config.market","US");
		//App.ConfigProperties.loadPropertyFile("config_MOCK.properties");
		MockitoAnnotations.initMocks(this);
		CollectionUploadUtil uploadUtil = new CollectionUploadUtil();
		SecurityToken securityToken = null;
		String privateGuid;
		
		try {
		
		if(uploadFlag != null && uploadFlag.equalsIgnoreCase("Y")){
			if(null != accountsBeans && accountsBeans.size() >0){
				for(UserAccountsBean userAccountsBean: accountsBeans){
					Object soapRequest = null;
					
					ServiceInvoker serviceInvoker = new ServiceInvoker();
					String modifedxmlResp = null;
					securityToken = uploadUtil.generateClaims(userAccountsBean.getUserId(),"");
				    privateGuid=GenerteToken.getSecurityGuid(securityToken).getPrivateGUIDs();
					
				    System.out.println("Account Number is ::" + userAccountsBean.getAccountNumber());
				    System.out.println("User ID Number is ::" + userAccountsBean.getUserId());
                    soapRequest =soapConsumer.buildRequest(userAccountsBean.getAccountNumbers(),userAccountsBean.getUserId());
					
					modifedxmlResp =serviceInvoker.soapInvoker(soapRequest,serviceName,userAccountsBean,collectionName);
					String publicGuid=GenerteToken.getSecurityGuid(securityToken).getPublicGUIDs();
				    uploadUtil.saveSoapResponse(userAccountsBean.getUserId(),userAccountsBean.getAccountNumber(),privateGuid,modifedxmlResp, securityToken, userAccountsBean.getAccountToken(),db,collectionName,publicGuid);
					
				}
			}
		}else{
			FindIterable<Document> iterable= db.getCollection("member").find(); // change this
	    	
	    	iterable.forEach(new Block<Document>() {
	    	    @Override
		    	    public void apply(Document document) {
	    	    	String accountNumber= document.get("account_number").toString().trim();
	    	    	String accountToken= document.get("account_token").toString().trim();
	    	    	String userId= document.get("userId").toString().trim();
	    	    	Object soapRequest = null;
				    String modifedxmlResp;
					List<String> accountNumbers =new ArrayList<>();
					accountNumbers.add(accountNumber);
					try{	
						soapRequest =soapConsumer.buildRequest(accountNumbers,userId); 
						
						// Calling serviceInvoker for Soap call
						ServiceInvoker serviceInvoker = new ServiceInvoker();
						modifedxmlResp = serviceInvoker.soapInvoker(soapRequest,serviceName,null, collectionName);
						
					    
					    CollectionUploadUtil uploadUtil = new CollectionUploadUtil();
					    SecurityToken securityToken;
						
							securityToken = uploadUtil.generateClaims(userId,"");
						
					    String privateGuid=GenerteToken.getSecurityGuid(securityToken).getPrivateGUIDs();
					    String publicGuid=GenerteToken.getSecurityGuid(securityToken).getPublicGUIDs();
					    uploadUtil.saveSoapResponse(userId,accountNumber,privateGuid,modifedxmlResp, securityToken, accountToken,db,collectionName,publicGuid);
						
		    	    } catch (Exception e) {
	    			
	    			e.printStackTrace();
	    		}
	    	    
	    	    }

	    	});
		}
    	
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    	
	}

	

	

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	

	
	
}