package com.americanexpress.dc.soap.impl;

import java.util.Arrays;
import java.util.List;

import com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.schemas.ecomm.alrtcommon._1.DayOfWeek;
import com.americanexpress.schemas.ecomm.alrtupdate._1.AlertPref;
import com.americanexpress.schemas.ecomm.alrtupdate._1.UpdateAccountAlertsReq;
import com.americanexpress.schemas.ecomm.ecscommon._1.ChannelType;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.axp.myca.common.util.ws.HeaderParameterUtil;

public class AlertsPrefUpdateReqBuilder implements SoapConsumer {

	private static final String CLIENT_ID = "WEB-USALERTS";
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

		UpdateAccountAlertsReq updatePrefReq = new UpdateAccountAlertsReq();
		updatePrefReq.getBasicAccountNumber().add(accountNumbers.get(0));

		// populate hold alerts request
		// populateHoldAlertsRequest(updateReqEntity, updatePrefReq);

		// populate Single alerts request
		populateAlertsPrefs(updatePrefReq);

		return updatePrefReq;
	}

	/**
	 * Method to populate Single Alert Preferences.
	 * 
	 * @param updateReqEntity
	 * @param updatePrefReq
	 */
	private void populateAlertsPrefs(UpdateAccountAlertsReq updatePrefReq) {

		AlertPref alertPref = new AlertPref();

		alertPref.setAlertKey("PAYMENT_REMINDER_ALERT");

		alertPref.setEnrolled(true);

		List<String> enrolledDevices = Arrays.asList(ChannelType.EMAIL.value(), ChannelType.SMS.value());
		for (String device : enrolledDevices) {
			alertPref.getEnrolledDeviceTypes().add(ChannelType.valueOf(device));
		}

		DayOfWeek dayOfWeek = DayOfWeek.WEDNESDAY;
		alertPref.setDayOfWeek(dayOfWeek);

		// set Supp Account Alerts Preference Details
		//populateSuppAccountPrefsDetails(updateAlertPref, alertPref);
		alertPref.setLastUpdatedBy(CLIENT_ID);

		updatePrefReq.getAlertPref().add(alertPref);

	}

	/**
	 * This method is used to the supp card details in the bean
	 * 
	 * @param updateAlertPref
	 * @param alertPref
	 *//*
		 * private void populateSuppAccountPrefsDetails(SingleAlertEntity
		 * updateAlertPref, AlertPref alertPref) {
		 * 
		 * if (updateAlertPref.getSupplementaryAccounts() != null) {
		 * 
		 * List<SuppAccountPrefEntity> suppAlertsPrefList =
		 * updateAlertPref.getSupplementaryAccounts(); for
		 * (SuppAccountPrefEntity suppAlertPref : suppAlertsPrefList) {
		 * SuppAccountPrefs suppAccountPrefs = new SuppAccountPrefs();
		 * suppAccountPrefs.setSuppAccountNumber(suppAlertPref.getAccountToken()
		 * ); suppAccountPrefs.setEnrolled(suppAlertPref.isEnrolled()); if
		 * (suppAlertPref.getGlosLimit() != null) {
		 * suppAccountPrefs.setThreshold(suppAlertPref.getGlosLimit().
		 * doubleValue()); }
		 * alertPref.getSuppAccountPrefs().add(suppAccountPrefs); }
		 * 
		 * } }
		 */

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

	/**
	 * Method to build update request object for Holding or Resuming Alerts
	 * 
	 * @param securityToken
	 * @param updateRequestEntityMap
	 * @return
	 */
	/*
	 * public void populateHoldAlertsRequest(AccountAlertsPreferences
	 * updateReqEntity, UpdateAccountAlertsReq updatePrefReq) {
	 * 
	 * if (null == updateReqEntity.getHold()) { return; }
	 * 
	 * AlertsPrefOnHold alertsPrefOnHold = new AlertsPrefOnHold();
	 * 
	 * alertsPrefOnHold.setOnHoldRequested(true);
	 * 
	 * Date startDate = (alertsPrefOnHold.isOnHoldRequested()) ?
	 * getFormattedDate(updateReqEntity.getHold().getStartDate(), "MM/dd/yyyy")
	 * : new Date(); Date endDate = (alertsPrefOnHold.isOnHoldRequested()) ?
	 * getFormattedDate(updateReqEntity.getHold().getEndDate(), "MM/dd/yyyy") :
	 * new Date();
	 * 
	 * alertsPrefOnHold.setHoldStartDate(CommonUtil.dateToXmlDate(startDate));
	 * 
	 * alertsPrefOnHold.setHoldEndDate(CommonUtil.dateToXmlDate(endDate));
	 * 
	 * updatePrefReq.setAlertsPrefOnHold(alertsPrefOnHold); }
	 */

}
