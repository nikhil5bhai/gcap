package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.privprefcommon._1.Pref;
import com.americanexpress.schemas.ecomm.privprefcommon._1.PrivChoiceChannel;
import com.americanexpress.schemas.ecomm.privprefupdate._1.AccountPrivacyPref;
import com.americanexpress.schemas.ecomm.privprefupdate._1.PrivacyPref;
import com.americanexpress.schemas.ecomm.privprefupdate._1.UpdateAccountPrivacyPrefReq;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class PrivacyPrefUpdateReqBuilder implements SoapConsumer {

	private static final String CLIENT_ID = "INTLRC";
	private static final String APP_ID = "GLOBAL_AMEX";

	@Override
	public Object buildRequest(List<String> accountNumbers, String userId) {

			List<String> accountNumberList = new ArrayList<String>();
			accountNumberList.add(accountNumbers.get(0));
			
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

			UpdateAccountPrivacyPrefReq updateRequest = new UpdateAccountPrivacyPrefReq();

			for (String entry : accountNumberList) {
				AccountPrivacyPref updatePrivacyPref = new AccountPrivacyPref();
				updatePrivacyPref.setAccountNumber(entry);
				updatePrivacyPref.setUpdatedBy(CLIENT_ID);
				populatePreferences(updatePrivacyPref);
				/*if (null != entry.getValue().getBasicEditPermissionIndicator()) {
					updatePrivacyPref.setBasicEditPermissionInd(
							Pref.valueOf(entry.getValue().getBasicEditPermissionIndicator()));
				}
				populateSuppAccountPrefs(entry.getValue().getSuppPreferences(), updatePrivacyPref);*/
				updateRequest.getAccountPrivacyPref().add(updatePrivacyPref);
			}

			return updateRequest;

	}

	private void populatePreferences(AccountPrivacyPref accountPrivacyPref) {

		PrivacyPref preference = new PrivacyPref();

		preference.setPrivChoiceChannel(PrivChoiceChannel.EMAIL.value());
		preference.setCardMemberPref(Pref.OPTED_IN);
		accountPrivacyPref.getPrivChoicePref().add(preference);

	}

	/*private void populateSuppAccountPrefs(List<SuppPrivacyPreference> suppAccountPrefsReq,
			AccountPrivacyPref accountPrivacyPref) {

		if (null == suppAccountPrefsReq || suppAccountPrefsReq.isEmpty()) {
			return;
		}
		suppAccountPrefsReq.forEach(suppAccountPrefReq -> {
			SuppEditPref suppPrivacyPreference = new SuppEditPref();
			suppPrivacyPreference.setAccountNumber(suppAccountPrefReq.getAccountNumber());
			suppPrivacyPreference.setCardMemberPref(Pref.valueOf(suppAccountPrefReq.getEditPermissionIndicator()));
			accountPrivacyPref.getSuppEditPref().add(suppPrivacyPreference);
		});
	}*/

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
