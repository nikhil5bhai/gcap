package com.americanexpress.dc.util;

import java.util.List;

import com.google.common.collect.BiMap;

public interface TokenServiceFacade {

	public BiMap<String, String> getAccountNumbers(List<String> encryptedAccountNumber, String correlationId);

	public BiMap<String, String> getEncryptedAccountTokens(List<String> decryptedAccountNumber, String correlationId);

	/**
	 * Method to retrieve Account Number for provide Account Token
	 * 
	 * @param accountToken
	 * @param correlationId
	 * @return
	 */
	public String getAccountNumber(String accountToken, String correlationId);

	/**
	 * Method to retrieve Account Token for provide Account Number
	 * 
	 * @param accountNumber
	 * @param correlationId
	 * @return
	 */
	public String getAccountToken(String accountNumber, String correlationId);
}
