

package com.americanexpress.dc.bean.member.accounts;

public class Profile {
	private String localePreference;
	private String lastName;
	private String language;
	private String country;
	private String memberSinceDate;
	private String firstName;
	private String embossedName;

	public String getLocalePreference() {
		return localePreference;
	}

	public void setLocalePreference(String localePreference) {
		this.localePreference = localePreference;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getMemberSinceDate() {
		return memberSinceDate;
	}

	public void setMemberSinceDate(String memberSinceDate) {
		this.memberSinceDate = memberSinceDate;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmbossedName() {
		return embossedName;
	}

	public void setEmbossedName(String embossedName) {
		this.embossedName = embossedName;
	}
}