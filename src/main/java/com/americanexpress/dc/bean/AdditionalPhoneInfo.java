package com.americanexpress.dc.bean;

public class AdditionalPhoneInfo {
	private Boolean isStatusModified;
	private Integer retentionPeriod;
	private String phoneType;

	public Boolean getIsStatusModified() {
		return isStatusModified;
	}

	public void setIsStatusModified(Boolean isStatusModified) {
		this.isStatusModified = isStatusModified;
	}

	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}

	public void setRetentionPeriod(Integer retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	public String getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(String phoneType) {
		this.phoneType = phoneType;
	}
}
