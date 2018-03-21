package com.americanexpress.dc.bean;

public class CardFeatures {
	private Boolean isLoyaltyAccount;
	private Boolean isPlastic;
	private Loyalty loyalty;
	private String ecoaDesc;

	public Boolean getIsLoyaltyAccount() {
		return isLoyaltyAccount;
	}

	public void setIsLoyaltyAccount(Boolean isLoyaltyAccount) {
		this.isLoyaltyAccount = isLoyaltyAccount;
	}

	public Boolean getIsPlastic() {
		return isPlastic;
	}

	public void setIsPlastic(Boolean isPlastic) {
		this.isPlastic = isPlastic;
	}

	public Loyalty getLoyalty() {
		return loyalty;
	}

	public void setLoyalty(Loyalty loyalty) {
		this.loyalty = loyalty;
	}

	public String getEcoaDesc() {
		return ecoaDesc;
	}

	public void setEcoaDesc(String ecoaDesc) {
		this.ecoaDesc = ecoaDesc;
	}
}