package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.eacommon._1.AuthorizedUserLocator;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddress;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressAcctReq;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressAcctReqs;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressCaptureInfo;
import com.americanexpress.schemas.ecomm.eacommon._1.EContactDetail;
import com.americanexpress.schemas.ecomm.eaupdate._1.UpdateEAddressAcctReq;
import com.americanexpress.schemas.ecomm.ecscommon._1.EAddressType;
import com.americanexpress.schemas.ecomm.ecscommon._1.PurposeCodeType;
import com.americanexpress.schemas.ecomm.ecscommon._1.RelationshipType;
import com.americanexpress.schemas.ecomm.ecscommon._1.RoleIdentifier;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class EmailUpdateRequestBuilder implements SoapConsumer {

	private static final String CLIENT_ID = "WEB-US-PROFILE-UPD-EN";
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

		List<String> accountNumberList = new ArrayList<String>();
		accountNumberList.add(accountNumbers.get(0));

		// update params to be set in request header
		updateHeaderParams(securityToken);

		UpdateEAddressAcctReq updateReq = new UpdateEAddressAcctReq();

		EAddressAcctReqs eAddressAcctReqs = new EAddressAcctReqs();

		AuthorizedUserLocator userLocator = getAuthorizedUserLocator(securityToken);

		for (String entry : accountNumberList) {
			EAddressAcctReq eAddressAcctReq = new EAddressAcctReq();
			eAddressAcctReq.setAccountNumber(entry);
			// if
			// (!EAddressType.MOBILE_EMAIL.value().equals(updateRequest.getChannel()))
			// {
			eAddressAcctReq.getAuthorizedUserLocator().add(userLocator);
			// }
			eAddressAcctReqs.getEAddressAcctReq().add(eAddressAcctReq);
		}

		updateReq.setEAddressAcctReqs(eAddressAcctReqs);
		updateReq.setRelationshipType(RelationshipType.CARD_CUSTOMER);
		updateReq.setPreferedLanguage("en");
		updateReq.setPartialDataAllowed(false);
		/*
		 * if
		 * (EAddressType.MOBILE_EMAIL.value().equals(updateRequest.getChannel())
		 * ) { updateReq.setRoleIdentifier(RoleIdentifier.MOBILE_CONTACT); }
		 */

		EContactDetail eContactDetail = new EContactDetail();
		eContactDetail.setEAddressCaptureInfo(getEAddressCaptureInfo());
		eContactDetail.setEAddress(getEAddress());

		updateReq.setEContactDetail(eContactDetail);

		return updateReq;
	}

	/**
	 * 
	 * @param securityToken
	 * @return
	 */
	private AuthorizedUserLocator getAuthorizedUserLocator(SecurityToken securityToken) {
		AuthorizedUserLocator authorizedUserLocator = new AuthorizedUserLocator();
		authorizedUserLocator.setRoleIdentifier(RoleIdentifier.CARD_HOLDER);
		authorizedUserLocator.setGuid(securityToken.getUniversalID());
		return authorizedUserLocator;
	}

	private EAddress getEAddress() {

		EAddress eAddress = new EAddress();
		eAddress.setEmailAddress("mock@aexp.com");
		eAddress.setEAddressType(EAddressType.EMAIL);
		eAddress.setPurposeCode(PurposeCodeType.E_STATEMENT);
		// eAddress.setPurposeCode(PurposeCodeType.MARKETING);
		return eAddress;
	}

	/**
	 * 
	 * @return
	 */
	private EAddressCaptureInfo getEAddressCaptureInfo() {
		EAddressCaptureInfo eAddressCaptureInfo = new EAddressCaptureInfo();
		eAddressCaptureInfo.setEAddressCaptureSource(CLIENT_ID);
		return eAddressCaptureInfo;
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
		// Modify below values being passed
		HeaderParameterUtil.setAppName(APP_ID);
		HeaderParameterUtil.setBpToken(APP_ID);
	}

}
