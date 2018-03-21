package com.americanexpress.dc.bean.member.accounts;

import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MemberEntity {
	
	
	
	private List<AccountElement> accounts;
	private LastLoginEntity lastLogin;
	private String memberSinceDate;

	public void setAccounts(List<AccountElement> accounts) {
		this.accounts = accounts;

	}

	public List<AccountElement> getAccounts() {
		return accounts;
	}

	public void setLastLogin(LastLoginEntity lastLogin) {
		this.lastLogin = lastLogin;
	}

	public LastLoginEntity getLastLogin() {
		return lastLogin;
	}
	
	public String getMemberSinceDate() {
		return memberSinceDate;
	}

	public void setMemberSinceDate(String memberSinceDate) {
		this.memberSinceDate = memberSinceDate;
	}
	


	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		return gson.toJson(this);
	}
}