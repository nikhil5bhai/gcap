package com.americanexpress.dc.bean;

public class Platform {
	private Link link;
	private String marketName;
	private String currencyCode;
	private String conversionStatus;
	private String marketCode;
	private String systemPlatform;
	private String amexRegion;
	private String isoCountryCode;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getConversionStatus() {
		return conversionStatus;
	}

	public void setConversionStatus(String conversionStatus) {
		this.conversionStatus = conversionStatus;
	}

	public String getMarketCode() {
		return marketCode;
	}

	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}

	public String getSystemPlatform() {
		return systemPlatform;
	}

	public void setSystemPlatform(String systemPlatform) {
		this.systemPlatform = systemPlatform;
	}

	public String getAmexRegion() {
		return amexRegion;
	}

	public void setAmexRegion(String amexRegion) {
		this.amexRegion = amexRegion;
	}

	public String getIsoCountryCode() {
		return isoCountryCode;
	}

	public void setIsoCountryCode(String isoCountryCode) {
		this.isoCountryCode = isoCountryCode;
	}
}