package com.americanexpress.dc.soap.impl;

import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.sdpinquiry._1.InquireStatementDeliveryPrefReq;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class StmtDeliveryPrefInquiryReqBuilder implements SoapConsumer {

	private static final String APP_ID = "GLOBAL_AMEX";

	@Override
	public Object buildRequest(List<String> accountNumbers, String userId) {

		SecurityToken securityToken = null;
		CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
		try {
			securityToken = collectionUploadUtil.generateClaims(userId,"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update params to be set in request header
		updateHeaderParams(securityToken);

		InquireStatementDeliveryPrefReq inquiresdpreq = new InquireStatementDeliveryPrefReq();
		inquiresdpreq.getAccountNumber().add(accountNumbers.get(0));
		inquiresdpreq.setAppName("eStatement");
		inquiresdpreq.setFetchSolInfo("false");
		inquiresdpreq.setPartialDataAllowed(true);

		return inquiresdpreq;
	}

	/**
	 * Method to update HeaderParamUtil with params to be inserted into request
	 * header
	 * 
	 * @param securityToken
	 */
	public void updateHeaderParams(SecurityToken securityToken) {
		HeaderParameterUtil.setSecurityToken(securityToken);
		HeaderParameterUtil.setCorrelationId(Constants.DEFAULT_CORRELATION_ID);
		HeaderParameterUtil.setAppName(APP_ID);
		HeaderParameterUtil.setBpToken(APP_ID);
	}

}
