package com.americanexpress.dc.soap.impl;

import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.alrtinquiry._1.InquireAccountAlertsReq;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class AlertsPrefInquiryReqBuilder implements SoapConsumer {

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

		InquireAccountAlertsReq inquireAlertsReq = new InquireAccountAlertsReq();
		inquireAlertsReq.getAccountNumber().add(accountNumbers.get(0));
		inquireAlertsReq.setPartialDataAllowed(false);

		return inquireAlertsReq;

	}

	/**
	 * Method to update HeaderParamUtil with params to be inserted into request
	 * header
	 * 
	 * @param securityToken
	 */
	private void updateHeaderParams(SecurityToken securityToken) {
		HeaderParameterUtil.setSecurityToken(securityToken);
		HeaderParameterUtil.setCorrelationId(Constants.DEFAULT_CORRELATION_ID);
		HeaderParameterUtil.setAppName("GLOBAL_AMEX");
		HeaderParameterUtil.setBpToken("GLOBAL_AMEX");
	}

}
