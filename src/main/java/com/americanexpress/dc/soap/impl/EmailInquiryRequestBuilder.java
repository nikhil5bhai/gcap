package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.eacommon._1.AuthorizedUserLocator;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressAcctReq;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressAcctReqs;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressReqType;
import com.americanexpress.schemas.ecomm.eacommon._1.EAddressReqTypes;
import com.americanexpress.schemas.ecomm.eainquiry._1.InquireEAddressAcctReq;
import com.americanexpress.schemas.ecomm.ecscommon._1.EAddressType;
import com.americanexpress.schemas.ecomm.ecscommon._1.PurposeCodeType;
import com.americanexpress.schemas.ecomm.ecscommon._1.PurposeCodes;
import com.americanexpress.schemas.ecomm.ecscommon._1.RelationshipType;
import com.americanexpress.schemas.ecomm.ecscommon._1.RoleIdentifier;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class EmailInquiryRequestBuilder implements SoapConsumer {

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

		InquireEAddressAcctReq inquireEAddressAcctReq = new InquireEAddressAcctReq();
		inquireEAddressAcctReq.setEAddressAcctReqs(getEAddressAcctReqs(accountNumberList, securityToken));
		inquireEAddressAcctReq.setRelationshipType(RelationshipType.CARD_CUSTOMER);
		inquireEAddressAcctReq
				.setEAddressReqTypes(getEAddressReqTypes(Arrays.asList(EAddressType.EMAIL.value()), false));
		inquireEAddressAcctReq.setPartialDataAllowed(true);

		return inquireEAddressAcctReq;
	}

	/**
	 * 
	 * @param acctNumberList
	 * @param securityToken
	 * @return
	 */
	private EAddressAcctReqs getEAddressAcctReqs(List<String> acctNumberList, SecurityToken securityToken) {

		EAddressAcctReqs eAddressAcctReqs = new EAddressAcctReqs();
		AuthorizedUserLocator userLocator = getAuthorizedUserLocator(securityToken);
		for (String accountNumber : acctNumberList) {
			EAddressAcctReq eAddressAcctReq = new EAddressAcctReq();
			eAddressAcctReq.setAccountNumber(accountNumber);
			eAddressAcctReq.getAuthorizedUserLocator().add(userLocator);
			eAddressAcctReqs.getEAddressAcctReq().add(eAddressAcctReq);
		}
		return eAddressAcctReqs;
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

	/**
	 * 
	 * @param requestedChannels
	 * @param isEMEmailEligible
	 * @return
	 */
	private EAddressReqTypes getEAddressReqTypes(List<String> requestedChannels, Boolean isEMEmailEligible) {

		EAddressReqTypes requestTypes = new EAddressReqTypes();

		for (String requestedChannel : requestedChannels) {
			EAddressReqType eAddressReqType = new EAddressReqType();
			PurposeCodes purposeCodes = new PurposeCodes();

			// To retrieve Email address.
			if (EAddressType.EMAIL.value().equalsIgnoreCase(requestedChannel)) {
				eAddressReqType.setEAddressType(EAddressType.EMAIL);
				purposeCodes.getPurposeCodeType().add(PurposeCodeType.E_STATEMENT);
				purposeCodes.getPurposeCodeType().add(PurposeCodeType.MARKETING);
				if (null != isEMEmailEligible && isEMEmailEligible) {
					purposeCodes.getPurposeCodeType().add(PurposeCodeType.ELECTRONIC_MEANS);
				}
			} else if (EAddressType.MOBILE_EMAIL.value().equalsIgnoreCase(requestedChannel)) {
				// To retrieve mobile Email address.
				eAddressReqType.setEAddressType(EAddressType.MOBILE_EMAIL);
				purposeCodes.getPurposeCodeType().add(PurposeCodeType.SERVICING);
			}
			eAddressReqType.setPurposeCodes(purposeCodes);
			requestTypes.getEAddressReqType().add(eAddressReqType);
		}

		return requestTypes;

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
		// Modify below values being passed
		HeaderParameterUtil.setAppName(APP_ID);
		HeaderParameterUtil.setBpToken(APP_ID);
	}

}
