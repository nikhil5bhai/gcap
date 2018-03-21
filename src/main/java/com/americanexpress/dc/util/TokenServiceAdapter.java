package com.americanexpress.dc.util;


import com.americanexpress.as.cs.shr.logging.util.TraversePath;
import com.americanexpress.dc.mock.RestServiceInvoker;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


public class TokenServiceAdapter implements TokenServiceFacade {

	private static final String DETOKENIZER_ENDPOINT_URL="https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/tokenizer/v1/detokenize";
	private static final String TOKENIZER_ENDPOINT_URL = "TOKENIZER_ENDPOINT_URL";
	
	private static final String TOKENS_DECRYPTOR = "TOKENS_DECRYPTOR";
	private static final String TOKENS_ENCRYPTOR = "TOKENS_ENCRYPTOR";
	
	private static final String TOKEN_DECRYPTOR = "TOKEN_DECRYPTOR";
	private static final String TOKEN_ENCRYPTOR = "TOKEN_ENCRYPTOR";
	

	private static final String TOKENS_DECRYPTOR_REST = "TOKENS_DECRYPTOR_REST";
	private static final String TOKENS_ENCRYPTOR_REST = "TOKENS_ENCRYPTOR_REST";
	
	private static final String TOKEN_DECRYPTOR_REST = "TOKEN_DECRYPTOR_REST";
	private static final String TOKEN_ENCRYPTOR_REST = "TOKEN_ENCRYPTOR_REST";
	
	private HashMap<Integer,String> encryptedAccountNumbersSequenceMap = new HashMap<Integer,String>();
	private HashMap<Integer,String> decryptedAccountNumbersSequenceMap = new HashMap<Integer,String>();
	
	
	/**
	 * Returns a BiMap.
	 * Usage of BiMap :
	 *	Account Tokens as Key : accountNumberMap.inverse().get(encryptedAccountToken)
	 *	Account Number as Key : accountNumberMap.get(accountNumber)
	 */
	@Override
	public BiMap<String, String> getAccountNumbers(List<String> encryptedAccountNumbers,String correlationId) {
		BiMap<String, String> accountNumberMap = HashBiMap.create();
		String tPathKey = TraversePath.startComponentCall(TOKENS_DECRYPTOR);
		try {
			
					accountNumberMap = getAccountNumbersFromRestAPI(encryptedAccountNumbers,correlationId);
			
		} catch (Throwable error) {
			
		} 
		return accountNumberMap;
	}
	
	private BiMap<String, String> getAccountNumbersFromRestAPI(List<String> encryptedAccountNumbers,String correlationId) {
		
		List<TokenServiceRequest> requestList = buildRequest(encryptedAccountNumbers);
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		String request = gson.toJson(requestList);
		RestServiceInvoker invoker = new RestServiceInvoker();
		String detokenizerEndpointUrl = DETOKENIZER_ENDPOINT_URL;
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(Constants.CLIENT_ID_HEADER_FIELD, "AmexAPI");
		if(null != correlationId && !correlationId.equalsIgnoreCase("")){
			headers.put(Constants.CORRELATION_ID_HEADER_FIELD, correlationId);
		}else{
			headers.put(Constants.CORRELATION_ID_HEADER_FIELD, "test");
		}
		headers.put( CONTENT_TYPE, APPLICATION_JSON );

		
		Response response = invoker.post(detokenizerEndpointUrl, null, null, headers, request);

		String responseContent = response.readEntity(String.class);
		
		List<TokenServiceResponse> detokenizerResponse = gson.fromJson(responseContent,
				new TypeToken<List<TokenServiceResponse>>() {
				}.getType());
		BiMap<String, String> accountNumberMap = getAccountNumberFromResponse(detokenizerResponse,requestList);
		return accountNumberMap;
	}

	private BiMap<String, String> getAccountNumberFromResponse(List<TokenServiceResponse> detokenizerResponselist, List<TokenServiceRequest> requestList) {
		BiMap<String, String> accountNumberMap = HashBiMap.create();
		if (null != detokenizerResponselist) {
			for (TokenServiceResponse response : detokenizerResponselist) {
				if (response.getValue() != null) {
					accountNumberMap.put(requestList.get(0).getValue(), response.getValue());
				}
			}
		}
		return accountNumberMap;
	}

	private List<TokenServiceRequest> buildRequest(List<String> encryptedAccountNumbers) {
		List<TokenServiceRequest> requestList = new ArrayList<TokenServiceRequest>();
		int sequence = 1;
		for(String encryptedAccountNumber : encryptedAccountNumbers) {
			TokenServiceRequest request = new TokenServiceRequest();
			request.setSequence(sequence);
			request.setType("ACCOUNT");
			request.setValue(encryptedAccountNumber);
			requestList.add(request);
			encryptedAccountNumbersSequenceMap.put(sequence, encryptedAccountNumber);
			sequence++;
		}
		return requestList;
	}

	

	private BiMap<String, String> getAccountTokenFromRestAPI(List<String> decryptedAccountNumber,
			String correlationId) {
		
		List<TokenServiceRequest> requestList = buildEncryptTokenRequest(decryptedAccountNumber);
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		String request = gson.toJson(requestList);
		RestServiceInvoker invoker = new RestServiceInvoker();
		String detokenizerEndpointUrl = App.ConfigProperties.getPropertyValue(TOKENIZER_ENDPOINT_URL);
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(Constants.CLIENT_ID_HEADER_FIELD, "AmexAPI");
		if(null != correlationId && !correlationId.equalsIgnoreCase("")){
			headers.put(Constants.CORRELATION_ID_HEADER_FIELD, correlationId);
		}else{
			headers.put(Constants.CORRELATION_ID_HEADER_FIELD, "test");
		}
		headers.put(CONTENT_TYPE, APPLICATION_JSON);

		
		Response response = invoker.post(detokenizerEndpointUrl, null, null, headers, request);
		
		String responseContent = response.readEntity(String.class);

		List<TokenServiceResponse> detokenizerResponse = gson.fromJson(responseContent,
				new TypeToken<List<TokenServiceResponse>>() {
				}.getType());
		BiMap<String, String> accountNumberMap = getAccountTokensFromResponse(detokenizerResponse);
		return accountNumberMap;
	}

	private BiMap<String, String> getAccountTokensFromResponse(List<TokenServiceResponse> tokenizerResponseList) {
		BiMap<String, String> accountNumberMap = HashBiMap.create();
		if (null != tokenizerResponseList) {
			for (TokenServiceResponse response : tokenizerResponseList) {
				if (response.getValue() != null) {
					accountNumberMap.put(decryptedAccountNumbersSequenceMap.get(response.getSequence()), response.getValue());
				}
			}
		}
		return accountNumberMap;
	}

	private List<TokenServiceRequest> buildEncryptTokenRequest(List<String> decryptedAccountNumbers) {
		List<TokenServiceRequest> requestList = new ArrayList<TokenServiceRequest>();
		int sequence = 1;
		for(String decryptedAccountNumber : decryptedAccountNumbers) {
			TokenServiceRequest request = new TokenServiceRequest();
			request.setSequence(sequence);
			request.setType("ACCOUNT");
			request.setValue(decryptedAccountNumber);
			requestList.add(request);
			decryptedAccountNumbersSequenceMap.put(sequence, decryptedAccountNumber);
			sequence++;
		}
		return requestList;
	}

	@Override
	public BiMap<String, String> getEncryptedAccountTokens(List<String> decryptedAccountNumber, String correlationId) {
		return null;
	}

	@Override
	public String getAccountNumber(String accountToken, String correlationId) {
		return null;
	}

	@Override
	public String getAccountToken(String accountNumber, String correlationId) {
		return null;
	}
	
	
}
