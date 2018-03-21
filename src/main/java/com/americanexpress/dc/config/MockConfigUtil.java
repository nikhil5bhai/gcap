package com.americanexpress.dc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import com.americanexpress.dc.bean.DetokenizerResponse;
import com.americanexpress.dc.mock.RestServiceInvoker;
import com.americanexpress.dc.util.DetokenizerRequest;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


public class MockConfigUtil {
	
	private static final String CLIENT_ID_PARAM = "client_id";
	//private static final String CLIENT_ID_VAL = "FinancialService_E1";
	private static final String CORRELATION_ID_PARAM = "correlation_id";
	

	 public String deTokenizeAccountToken(String accountToken){
	    	String accountNumber=null;
	    	
		    	
				List<DetokenizerRequest> requestList = buildRequest(accountToken);
				Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
				String request = gson.toJson(requestList);
				System.out.println("Request ::" + request);
				RestServiceInvoker invoker = new RestServiceInvoker();
				String detokenizerEndpointUrl = "https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/tokenizer/v1/detokenize";
				HashMap<String, Object> headers = new HashMap<String, Object>();
				headers.put(CLIENT_ID_PARAM, "eStatement");
				headers.put(CORRELATION_ID_PARAM, "test");
				headers.put(CONTENT_TYPE, APPLICATION_JSON);

				System.out.println("URL::" + detokenizerEndpointUrl);
				Response response = invoker.post(detokenizerEndpointUrl, null, null, headers, request);
				
				String responseContent = response.readEntity(String.class);
				System.out.println("Response from TokenService :" + responseContent);
				List<DetokenizerResponse> detokenizerResponse = gson.fromJson(responseContent,
						new TypeToken<List<DetokenizerResponse>>() {
						}.getType());
				accountNumber = getAccountNumberFromResponse(detokenizerResponse);
				System.out.println("Acc # >>"+accountNumber);
				return accountNumber;
			
	    	    }
	    
	    public List<DetokenizerRequest> buildRequest(String encryptedAccountNumber) {
			List<DetokenizerRequest> requestList = new ArrayList<DetokenizerRequest>();
			DetokenizerRequest request = new DetokenizerRequest();
			request.setSequence(1);
			request.setType("ACCOUNT");
			request.setValue(encryptedAccountNumber);
			requestList.add(request);
			return requestList;
		}
	    
	    public String getAccountNumberFromResponse(List<DetokenizerResponse> detokenizerResponselist) {
			String accountNumber = null;
			if (null != detokenizerResponselist) {
				for (DetokenizerResponse response : detokenizerResponselist) {
					accountNumber = response.getValue();
				}
			}
			return accountNumber;
		}
	
}
