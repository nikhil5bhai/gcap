package com.americanexpress.dc.bean.member.accounts;

public class Account {
	private String displayAccountNumber;
	private String relationship;
	private String supplementaryIndex;
	
	public String getDisplayAccountNumber() {
		return displayAccountNumber;
	}

	public void setDisplayAccountNumber(String displayAccountNumber) {
		this.displayAccountNumber = displayAccountNumber;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getSupplementaryIndex() {
		return supplementaryIndex;
	}

	public void setSupplementaryIndex(String supplementaryIndex) {
		this.supplementaryIndex = supplementaryIndex;
	}
}