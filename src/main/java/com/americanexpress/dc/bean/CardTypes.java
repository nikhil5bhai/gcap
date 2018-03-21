package com.americanexpress.dc.bean;

import java.util.List;

public class CardTypes {
	private String productTypeCode;
	private String paymentType;
	private String lineOfBusinessType;
	private List<String> plasticTypes;
	private String coBrandType;
	private String specialPurchasingAccountType;
	private String specialPlasticType;
	private String diversionAccountType;
	private String insideRewardsCityName;

	public String getProductTypeCode() {
		return productTypeCode;
	}

	public void setProductTypeCode(String productTypeCode) {
		this.productTypeCode = productTypeCode;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getLineOfBusinessType() {
		return lineOfBusinessType;
	}

	public void setLineOfBusinessType(String lineOfBusinessType) {
		this.lineOfBusinessType = lineOfBusinessType;
	}

	public List<String> getPlasticTypes() {
		return plasticTypes;
	}

	public void setPlasticTypes(List<String> plasticTypes) {
		this.plasticTypes = plasticTypes;
	}

	public String getCoBrandType() {
		return coBrandType;
	}

	public void setCoBrandType(String coBrandType) {
		this.coBrandType = coBrandType;
	}

	public String getSpecialPurchasingAccountType() {
		return specialPurchasingAccountType;
	}

	public void setSpecialPurchasingAccountType(String specialPurchasingAccountType) {
		this.specialPurchasingAccountType = specialPurchasingAccountType;
	}

	public String getSpecialPlasticType() {
		return specialPlasticType;
	}

	public void setSpecialPlasticType(String specialPlasticType) {
		this.specialPlasticType = specialPlasticType;
	}

	public String getDiversionAccountType() {
		return diversionAccountType;
	}

	public void setDiversionAccountType(String diversionAccountType) {
		this.diversionAccountType = diversionAccountType;
	}

	public String getInsideRewardsCityName() {
		return insideRewardsCityName;
	}

	public void setInsideRewardsCityName(String insideRewardsCityName) {
		this.insideRewardsCityName = insideRewardsCityName;
	}
}