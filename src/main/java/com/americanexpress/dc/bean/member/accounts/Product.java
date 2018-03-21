
package com.americanexpress.dc.bean.member.accounts;

import java.util.List;

public class Product {
	private String digitalAssetId;
	private String description;
	private String paymentType;
	private String largeCardArt;
	private String smallCardArt;
	private List<String> cardEligibilities;
	private List<String> accountEligibilities;
	private String lineOfBusinessType;

	public String getDigitalAssetId() {
		return digitalAssetId;
	}

	public void setDigitalAssetId(String digitalAssetId) {
		this.digitalAssetId = digitalAssetId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getLargeCardArt() {
		return largeCardArt;
	}

	public void setLargeCardArt(String largeCardArt) {
		this.largeCardArt = largeCardArt;
	}

	public String getSmallCardArt() {
		return smallCardArt;
	}

	public void setSmallCardArt(String smallCardArt) {
		this.smallCardArt = smallCardArt;
	}

	public List<String>  getCardEligibilities() {
		return cardEligibilities;
	}

	public void setCardEligibilities(List<String> cardEligibilities) {
		this.cardEligibilities = cardEligibilities;
	}

	public String getLineOfBusinessType() {
		return lineOfBusinessType;
	}

	public void setLineOfBusinessType(String lineOfBusinessType) {
		this.lineOfBusinessType = lineOfBusinessType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getAccountEligibilities() {
		return accountEligibilities;
	}

	public void setAccountEligibilities(List<String> accountEligibilities) {
		this.accountEligibilities = accountEligibilities;
	}
}