package com.americanexpress.dc.bean;

import java.util.List;

/**
 * @author zselvad
 *
 */
public class UserAccountsBean {

	private String accountNumber;
	
	private List<String> accountNumbers;
	
	private String userId;
	
	private List<String> accountTokens;

	private String privateGuid;
	
	private String publicGuid;

  // TODO: 2/23/17 Standardize Locale for Language and Country
	private String locale;
	
	private List<UserAccountsBean> suppUserAccountsBeans;
	
	private String racfId = null;
	
	private String region = null;

  // TODO: 2/23/17 Standardize Locale for Language and Country
	private String lang;
	
	private String relationship;

	private String digitalAssetId;
	private String productCode;
	private String marketCode;

	private String accountToken;
	
	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}


	/**
	 * @return the privateGuid
	 */
	public String getPrivateGuid() {
		return privateGuid;
	}

	/**
	 * @param privateGuid the privateGuid to set
	 */
	public void setPrivateGuid(String privateGuid) {
		this.privateGuid = privateGuid;
	}
	
	/**
	 * @return the racfId
	 */
	public String getracfId() {
		return racfId;
	}

	/**
	 * @param racfId the racfId to set
	 */
	public void setracfId(String racfId) {
		this.racfId = racfId;
	}
	
	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	
	/**
	 * @return the languagePreference
	 */
  // TODO: 2/23/17 Standardize Locale for Language and Country
	public String getLanguagePreference() {
		return lang;
	}

	/**
	 * @param region the region to set
	 */
	public void setLanguagePreference(String lang) {
		this.lang = lang;
	}

	public String getPublicGuid() {
		return publicGuid;
	}

	public void setPublicGuid(String publicGuid) {
		this.publicGuid = publicGuid;
	}


  // TODO: 2/23/17 Standardize Locale for Language and Country
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public List<UserAccountsBean> getSuppUserAccountsBeans() {
		return suppUserAccountsBeans;
	}

	public void setSuppUserAccountsBeans(List<UserAccountsBean> suppUserAccountsBeans) {
		this.suppUserAccountsBeans = suppUserAccountsBeans;
	}

	public String getDigitalAssetId() {
		return digitalAssetId;
	}

	public void setDigitalAssetId(String digitalAssetId) {
		this.digitalAssetId = digitalAssetId;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}


	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public List<String> getAccountTokens() {
		return accountTokens;
	}

	public void setAccountTokens(List<String> accountTokens) {
		this.accountTokens = accountTokens;
	}

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

	public List<String> getAccountNumbers() {
		return accountNumbers;
	}

	public void setAccountNumbers(List<String> accountNumbers) {
		this.accountNumbers = accountNumbers;
	}

	public String getMarketCode() {
		return marketCode;
	}

	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}
	
}
