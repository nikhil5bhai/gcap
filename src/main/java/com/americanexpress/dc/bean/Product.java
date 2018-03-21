package com.americanexpress.dc.bean;

import java.util.List;

public class Product {
	private Link link;
	private CardFeatures cardFeatures;
	private CardTypes cardTypes;
	private List<String> cardEligibilities;
	private List<String> programEnrollments;
	private LineOfBusiness lineOfBusiness;
	private DigitalInfo digitalInfo;

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public CardFeatures getCardFeatures() {
		return cardFeatures;
	}

	public void setCardFeatures(CardFeatures cardFeatures) {
		this.cardFeatures = cardFeatures;
	}

	public CardTypes getCardTypes() {
		return cardTypes;
	}

	public void setCardTypes(CardTypes cardTypes) {
		this.cardTypes = cardTypes;
	}

	public List<String> getCardEligibilities() {
		return cardEligibilities;
	}

	public void setCardEligibilities(List<String> cardEligibilities) {
		this.cardEligibilities = cardEligibilities;
	}

	public List<String> getProgramEnrollments() {
		return programEnrollments;
	}

	public void setProgramEnrollments(List<String> programEnrollments) {
		this.programEnrollments = programEnrollments;
	}

	public LineOfBusiness getLineOfBusiness() {
		return lineOfBusiness;
	}

	public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}

	public DigitalInfo getDigitalInfo() {
		return digitalInfo;
	}

	public void setDigitalInfo(DigitalInfo digitalInfo) {
		this.digitalInfo = digitalInfo;
	}
}