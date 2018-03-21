package com.americanexpress.dc.bean;

public class Loyalty {
	private String legalProgramName;
	private String tierCode;
	private String programName;
	private String programLiteral;

	public String getLegalProgramName() {
		return legalProgramName;
	}

	public void setLegalProgramName(String legalProgramName) {
		this.legalProgramName = legalProgramName;
	}

	public String getTierCode() {
		return tierCode;
	}

	public void setTierCode(String tierCode) {
		this.tierCode = tierCode;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getProgramLiteral() {
		return programLiteral;
	}

	public void setProgramLiteral(String programLiteral) {
		this.programLiteral = programLiteral;
	}
}