package com.americanexpress.dc.bean;

import java.util.List;

public class Contacts {
	private String recentAddressChangeDate;
	private List<AddressElement> address;
	private List<PhoneElement> phone;
	private AdditionalPhoneInfo additionalPhoneInfo;

	public String getRecentAddressChangeDate() {
		return recentAddressChangeDate;
	}

	public void setRecentAddressChangeDate(String recentAddressChangeDate) {
		this.recentAddressChangeDate = recentAddressChangeDate;
	}

	public List<AddressElement> getAddress() {
		return address;
	}

	public void setAddress(List<AddressElement> address) {
		this.address = address;
	}

	public List<PhoneElement> getPhone() {
		return phone;
	}

	public void setPhone(List<PhoneElement> phone) {
		this.phone = phone;
	}
}