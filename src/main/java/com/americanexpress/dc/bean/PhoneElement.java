package com.americanexpress.dc.bean;

public class PhoneElement {
	private String phoneNumber;
	private String type;
	private String countryCode;
	private String extension;
	private String last4Digits;
	private String callPreferenceCode;
	private String updateDate;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getLast4Digits() {
		return last4Digits;
	}

	public void setLast4Digits(String last4Digits) {
		this.last4Digits = last4Digits;
	}

	public String getCallPreferenceCode() {
		return callPreferenceCode;
	}

	public void setCallPreferenceCode(String callPreferenceCode) {
		this.callPreferenceCode = callPreferenceCode;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
}