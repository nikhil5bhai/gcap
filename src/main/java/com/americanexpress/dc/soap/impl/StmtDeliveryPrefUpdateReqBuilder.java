package com.americanexpress.dc.soap.impl;

import java.util.ArrayList;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.sdpcommon._1.CardMemberDeliveryPreference;
import com.americanexpress.schemas.ecomm.sdpupdate._1.StatementDeliveryPrefUpdate;
import com.americanexpress.schemas.ecomm.sdpupdate._1.UpdateStatementDeliveryPref;
import com.americanexpress.schemas.ecomm.sdpupdate._1.UpdateStatementDeliveryPrefReq;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class StmtDeliveryPrefUpdateReqBuilder implements SoapConsumer {

	private static final String CLIENT_ID = "eStatement";
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

		UpdateStatementDeliveryPrefReq updatesdpreq = new UpdateStatementDeliveryPrefReq();

		for (String entry : accountNumberList) {
			UpdateStatementDeliveryPref updateStmtDlvryPref = new UpdateStatementDeliveryPref();
			updateStmtDlvryPref.setAccountNumber(entry);
			StatementDeliveryPrefUpdate stmtDlvryPrefUpdate = new StatementDeliveryPrefUpdate();
			stmtDlvryPrefUpdate.setUpdatingSource(CLIENT_ID);
			stmtDlvryPrefUpdate.setUpdatingUser(CLIENT_ID);

			stmtDlvryPrefUpdate.setNewDeliveryPref(CardMemberDeliveryPreference.fromValue("PAPER_OFF"));

			updateStmtDlvryPref.setStatementDeliveryPrefUpdate(stmtDlvryPrefUpdate);
			updatesdpreq.getUpdateStatementDeliveryPref().add(updateStmtDlvryPref);
		}
		return updatesdpreq;
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
