package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.acpinquiry.v1.AccountNumbersType;
import com.americanexpress.schemas.ecomm.acpinquiry.v1.InquireAcctCorrespondencePrefRequest;
import com.americanexpress.schemas.ecomm.acpinquiry.v1.ObjectFactory;
import com.americanexpress.schemas.ecomm.acpinquiry.v1.ResponseFragmentType;
import com.americanexpress.schemas.ecomm.acpinquiry.v1.ResponseFragmentTypes;
import com.americanexpress.schemas.ecomm.ecscommon.v1.RelationshipType;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class EConsentPrefInquiryReqBuilder implements SoapConsumer {

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
		// inquire params to be set in request header
		updateHeaderParams(securityToken);

		ObjectFactory inquireObjFactory = new ObjectFactory();

		InquireAcctCorrespondencePrefRequest inquireReqTypes = inquireObjFactory
				.createInquireAcctCorrespondencePrefRequest();

		List<String> accountNumbersList = new ArrayList<String>();
		accountNumbersList.add(accountNumbers.get(0));

		List<AccountNumbersType> accnumTypeList = new ArrayList<AccountNumbersType>();

		for (String entry : accountNumbersList) {
			AccountNumbersType accnumType = new AccountNumbersType();
			accnumType.setAccountNumber(entry);
			accnumTypeList.add(accnumType);
		}
		inquireReqTypes.getAccountNumbers().addAll(accnumTypeList);
		inquireReqTypes.setRelationshipType(RelationshipType.CARD_CUSTOMER);
		List<ResponseFragmentTypes> responseFragments = new ArrayList<ResponseFragmentTypes>();
		ResponseFragmentTypes responseFragment = new ResponseFragmentTypes();
		responseFragment.setResponseFragmentType(ResponseFragmentType.CURRENT);
		responseFragments.add(responseFragment);
		inquireReqTypes.getResponseFragments().addAll(responseFragments);
		inquireReqTypes.setPartialDataAllowed(false);

		return inquireReqTypes;
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
