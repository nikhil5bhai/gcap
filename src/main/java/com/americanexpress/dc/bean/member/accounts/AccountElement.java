
package com.americanexpress.dc.bean.member.accounts;

import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AccountElement  {
	private Account account;
	private Status status;
	private Product product;
	private Platform platform;
	private Profile profile;
	private List<SupplementaryAccountElement> supplementaryAccounts;
	private String accountToken;
	private Integer accountWeight;
	private Integer sortedIndex;

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Profile getProfile() {
		return profile;
	}
	
	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
	
	public Platform getPlatform() {
		return this.platform;
	}

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}
	
	public Integer getAccountWeight() {
		return accountWeight;
	}

	public void setAccountWeight(Integer accountWeight) {
		this.accountWeight = accountWeight;
	}
	
	public List<SupplementaryAccountElement> getSupplementaryAccounts() {
		return supplementaryAccounts;
	}

	public void setSupplementaryAccounts(List<SupplementaryAccountElement> supplementaryAccounts) {
		this.supplementaryAccounts = supplementaryAccounts;
	}

	public Integer getSortedIndex() {
		return sortedIndex;
	}

	public void setSortedIndex(Integer sortedIndex) {
		this.sortedIndex = sortedIndex;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		return gson.toJson(this);
	}
}