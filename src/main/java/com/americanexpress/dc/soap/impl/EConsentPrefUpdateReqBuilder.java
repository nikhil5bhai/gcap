package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.acpcommon.v1.PreferenceCategoryType;
import com.americanexpress.schemas.ecomm.acpcommon.v1.PreferenceType;
import com.americanexpress.schemas.ecomm.acpupdate.v1.CorrespondencePreferenceRequestType;
import com.americanexpress.schemas.ecomm.acpupdate.v1.ObjectFactory;
import com.americanexpress.schemas.ecomm.acpupdate.v1.UpdateAccountCorrespondencePrefRequest;
import com.americanexpress.schemas.ecomm.acpupdate.v1.UpdateAcctCorrespondencePrefRequest;
import com.americanexpress.schemas.ecomm.acpupdate.v1.UpdatePreferenceRequestType;
import com.americanexpress.schemas.ecomm.acpupdate.v1.UpdatePreferenceRequestsType;
import com.americanexpress.schemas.ecomm.ecscommon.v1.EAddressType;
import com.americanexpress.schemas.ecomm.ecscommon.v1.RelationshipType;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class EConsentPrefUpdateReqBuilder implements SoapConsumer {

	private static final String CLIENT_ID = "WEB-US-PNP-UPD-EN";
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

		List<String> accountNumberList = new ArrayList<String>();
		accountNumberList.add(accountNumbers.get(0));

		ObjectFactory updObjFactory = new ObjectFactory();

		// request types object
		UpdatePreferenceRequestsType updReqTypes = updObjFactory.createUpdatePreferenceRequestsType();

		populateRequestsTypes(accountNumberList, updReqTypes);

		// pref request obj
		UpdateAcctCorrespondencePrefRequest updatePrefReq = updObjFactory.createUpdateAcctCorrespondencePrefRequest();
		updatePrefReq.setUpdatePreferenceRequests(updReqTypes);

		// the main request object
		UpdateAccountCorrespondencePrefRequest updEconPrefRequest = updObjFactory
				.createUpdateAccountCorrespondencePrefRequest();

		// setting in the main request object
		updEconPrefRequest.setUpdateAcctCorrespondencePrefRequest(updatePrefReq);

		return updEconPrefRequest;

	}

	private void populateRequestsTypes(List<String> accountNumberList, UpdatePreferenceRequestsType updReqTypes) {

		for (String entry : accountNumberList) {

			CorrespondencePreferenceRequestType correspondencePreferenceRequestType = new CorrespondencePreferenceRequestType();
			correspondencePreferenceRequestType.setCaptureSource(CLIENT_ID);
			correspondencePreferenceRequestType.setPreferenceCategory(PreferenceCategoryType.GENERIC);
			PreferenceType preferenceType = PreferenceType.PAPER_OFF;

			correspondencePreferenceRequestType.setPreference(preferenceType);

			if (preferenceType.equals(PreferenceType.PAPER_OFF)) {
				correspondencePreferenceRequestType.setDeliveryDevicePreference(EAddressType.EMAIL);
			}

			UpdatePreferenceRequestType econUpdPrefDef = new UpdatePreferenceRequestType();
			econUpdPrefDef.setAccountNumber(entry);
			econUpdPrefDef.setRelationshipType(RelationshipType.CARD_CUSTOMER);
			econUpdPrefDef.setCorrespondencePreferenceReq(correspondencePreferenceRequestType);

			updReqTypes.getUpdatePreferenceRequest().add(econUpdPrefDef);
		}

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
		HeaderParameterUtil.setAppName(APP_ID);
		HeaderParameterUtil.setBpToken(APP_ID);
	}

}
