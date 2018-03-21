package com.americanexpress.dc.bean;

public class Identifiers {
	private Link link;
	private String internalAccountNumber;
	private Boolean isBasic;
	private String basicAccountToken;
	private String displayAccountNumber;
	private Boolean isPurged;
	private Boolean isReplaced;
	private Boolean isEnrolledOnline;
	private Integer supplementaryAccountCount;
	private Integer alternateCustomerExperienceCode;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public String getInternalAccountNumber() {
		return internalAccountNumber;
	}

	public void setInternalAccountNumber(String internalAccountNumber) {
		this.internalAccountNumber = internalAccountNumber;
	}

	public Boolean getIsBasic() {
		return isBasic;
	}

	public void setIsBasic(Boolean isBasic) {
		this.isBasic = isBasic;
	}

	public String getBasicAccountToken() {
		return basicAccountToken;
	}

	public void setBasicAccountToken(String basicAccountToken) {
		this.basicAccountToken = basicAccountToken;
	}

	public String getDisplayAccountNumber() {
		return displayAccountNumber;
	}

	public void setDisplayAccountNumber(String displayAccountNumber) {
		this.displayAccountNumber = displayAccountNumber;
	}

	public Boolean getIsPurged() {
		return isPurged;
	}

	public void setIsPurged(Boolean isPurged) {
		this.isPurged = isPurged;
	}

	public Boolean getIsReplaced() {
		return isReplaced;
	}

	public void setIsReplaced(Boolean isReplaced) {
		this.isReplaced = isReplaced;
	}

	public Boolean getIsEnrolledOnline() {
		return isEnrolledOnline;
	}

	public void setIsEnrolledOnline(Boolean isEnrolledOnline) {
		this.isEnrolledOnline = isEnrolledOnline;
	}

	public Integer getSupplementaryAccountCount() {
		return supplementaryAccountCount;
	}

	public void setSupplementaryAccountCount(Integer supplementaryAccountCount) {
		this.supplementaryAccountCount = supplementaryAccountCount;
	}

	public Integer getAlternateCustomerExperienceCode() {
		return alternateCustomerExperienceCode;
	}

	public void setAlternateCustomerExperienceCode(Integer alternateCustomerExperienceCode) {
		this.alternateCustomerExperienceCode = alternateCustomerExperienceCode;
	}
}