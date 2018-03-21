package com.americanexpress.dc.soap.impl;

import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.privprefcommon._1.ChannelDataReq;
import com.americanexpress.schemas.ecomm.privprefinquiry._1.InquireAccountPrivacyPrefReq;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class PrivacyPrefInquiryReqBuilder implements SoapConsumer {

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

		InquireAccountPrivacyPrefReq inquireReq = new InquireAccountPrivacyPrefReq();
		inquireReq.getAccountNumber().add(accountNumbers.get(0));

		inquireReq.getChannelDataReq().add(ChannelDataReq.valueOf("ALL"));

		inquireReq.setPartialDataAllowed(true);

		return inquireReq;

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
		HeaderParameterUtil.setAppName("GLOBAL_AMEX");
		HeaderParameterUtil.setBpToken("GLOBAL_AMEX");
	}

}
