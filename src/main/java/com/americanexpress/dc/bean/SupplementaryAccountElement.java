package com.americanexpress.dc.bean;

public class SupplementaryAccountElement {
	private String accountToken;
	private Identifiers identifiers;
	private Platform platform;
	private Product product;
	private Holder holder;
	private Status status;

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

	public Identifiers getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Identifiers identifiers) {
		this.identifiers = identifiers;
	}

	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Holder getHolder() {
		return holder;
	}

	public void setHolder(Holder holder) {
		this.holder = holder;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}