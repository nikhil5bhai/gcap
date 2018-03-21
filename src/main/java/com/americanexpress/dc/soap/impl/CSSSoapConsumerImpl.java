package com.americanexpress.dc.soap.impl;

import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.schemas.as.creditstatusservice._1.AccountNumberListDef;
import com.americanexpress.schemas.as.creditstatusservice._1.CreditStatusRequestDef;
import com.americanexpress.schemas.as.creditstatusservice._1.SegmentListDef;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;

public class CSSSoapConsumerImpl implements SoapConsumer{

		/**
		 *  buildSoapRequest- Fins request builder class
		 *  
		 *  @param String accountNumber
		 *  @param String userId
		 */
		
		public Object buildRequest(List<String> accountNumbers, String userId) {
			CreditStatusRequestDef creditStatusRequestDef = new CreditStatusRequestDef();
			
			com.americanexpress.schemas.as.globalservicedata._1.SecureCommonRequestParamDef secureCommonRequestParam = new com.americanexpress.schemas.as.globalservicedata._1.SecureCommonRequestParamDef();
			SecurityToken securityToken = null;
			
			try {
				CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
				securityToken = collectionUploadUtil.generateClaims(userId,"");
			
				secureCommonRequestParam.setSecurityTokenData(securityToken.asXML());
			
				creditStatusRequestDef.setSecureCommonRequestParam(secureCommonRequestParam);
				
				com.americanexpress.schemas.as.globalservicedata._1.CommonRequestParamDef commonRequestParamDef = new com.americanexpress.schemas.as.globalservicedata._1.CommonRequestParamDef();
				
				com.americanexpress.schemas.as.globalservicedata._1.AuditMapDef auditMapDef = new com.americanexpress.schemas.as.globalservicedata._1.AuditMapDef();
				auditMapDef.setApplicationID("AmexAPI"); // change this
				commonRequestParamDef.setAuditMap(auditMapDef);
				secureCommonRequestParam.setCommonRequestParam(commonRequestParamDef);
				
				AccountNumberListDef accountNumberList = new AccountNumberListDef();
				
				//String accNum=document.get("account_number").toString(); // change this
				for(String accountNumber: accountNumbers){
					accountNumberList.getAccountNumber().add(accountNumber);
				}
				
				creditStatusRequestDef.setAccountNumberList(accountNumberList);
				
				SegmentListDef segmentList = new SegmentListDef();
				segmentList.getSegmentName().add("LIMITS_DATA"); // change this
				segmentList.getSegmentName().add("MESSAGING_DATA"); // change this
				
				creditStatusRequestDef.setSegmentList(segmentList);
				
		    	    
				} catch (Exception e) {
					e.printStackTrace();
				}
			return creditStatusRequestDef; 

	    	  
		}
		
		
	
	
}
