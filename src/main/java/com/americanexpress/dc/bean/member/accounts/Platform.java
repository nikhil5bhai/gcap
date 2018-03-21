package com.americanexpress.dc.bean.member.accounts;

public class Platform {
	private String marketName;
	private String isoAlphaCurrencyCode;
	private String amexRegion;

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getAmexRegion() {
		return amexRegion;
	}

	public void setAmexRegion(String amexRegion) {
		this.amexRegion = amexRegion;
	}

	public String getIsoAlphaCurrencyCode() {
		return isoAlphaCurrencyCode;
	}

	public void setIsoAlphaCurrencyCode(String isoAlphaCurrencyCode) {
		this.isoAlphaCurrencyCode = isoAlphaCurrencyCode;
	}

}