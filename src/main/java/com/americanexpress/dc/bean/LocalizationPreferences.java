package com.americanexpress.dc.bean;

public class LocalizationPreferences {
	private String currencyLocale;
	private String localizationId;
	private String dateLocale;
	private String languagePreferenceCode;
	private String homeCountryLocale;
	private String languagePreference;

	public String getCurrencyLocale() {
		return currencyLocale;
	}

	public void setCurrencyLocale(String currencyLocale) {
		this.currencyLocale = currencyLocale;
	}

	public String getLocalizationId() {
		return localizationId;
	}

	public void setLocalizationId(String localizationId) {
		this.localizationId = localizationId;
	}

	public String getDateLocale() {
		return dateLocale;
	}

	public void setDateLocale(String dateLocale) {
		this.dateLocale = dateLocale;
	}

	public String getLanguagePreferenceCode() {
		return languagePreferenceCode;
	}

	public void setLanguagePreferenceCode(String languagePreferenceCode) {
		this.languagePreferenceCode = languagePreferenceCode;
	}

	public String getHomeCountryLocale() {
		return homeCountryLocale;
	}

	public void setHomeCountryLocale(String homeCountryLocale) {
		this.homeCountryLocale = homeCountryLocale;
	}

	public String getLanguagePreference() {
		return languagePreference;
	}

	public void setLanguagePreference(String languagePreference) {
		this.languagePreference = languagePreference;
	}
}