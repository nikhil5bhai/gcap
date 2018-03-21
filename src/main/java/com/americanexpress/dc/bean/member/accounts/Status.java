
package com.americanexpress.dc.bean.member.accounts;

import java.util.List;

public class Status {
	private List<String> cardStatus;
	private List<String> accountStatus;
	private Integer daysPastDue;
	private String accountSetupDate;

	public List<String> getCardStatus() {
		return cardStatus;
	}

	public void setCardStatus(List<String> cardStatus) {
		this.cardStatus = cardStatus;
	}

	public Integer getDaysPastDue() {
		return daysPastDue;
	}

	public void setDaysPastDue(Integer daysPastDue) {
		this.daysPastDue = daysPastDue;
	}

	public String getAccountSetupDate() {
		return accountSetupDate;
	}

	public void setAccountSetupDate(String accountSetupDate) {
		this.accountSetupDate = accountSetupDate;
	}

	public List<String> getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(List<String> accountStatus) {
		this.accountStatus = accountStatus;
	}
}