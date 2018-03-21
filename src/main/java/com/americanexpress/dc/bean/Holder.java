package com.americanexpress.dc.bean;

public class Holder {
	private Link link;
	private LocalizationPreferences localizationPreferences;
	private Profile profile;
	private Contacts contacts;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public LocalizationPreferences getLocalizationPreferences() {
		return localizationPreferences;
	}

	public void setLocalizationPreferences(LocalizationPreferences localizationPreferences) {
		this.localizationPreferences = localizationPreferences;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Contacts getContacts() {
		return contacts;
	}

	public void setContacts(Contacts contacts) {
		this.contacts = contacts;
	}
}