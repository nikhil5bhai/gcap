package com.americanexpress.dc.soap.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomUtils;

import com.americanexpress.as.sfwk.shr.ServiceUtil;
import com.americanexpress.dc.bean.BackendAccountEntity;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.mock.RestServiceInvoker;
import com.americanexpress.schemas.as.globalservicedata._1.AuditMapDef;
import com.americanexpress.schemas.as.globalservicedata._1.CommonRequestParamDef;
import com.americanexpress.schemas.as.globalservicedata._1.SecureCommonRequestParamDef;
import com.americanexpress.schemas.as.intlloyalty.detailsservice.request._1.Fragment;
import com.americanexpress.schemas.as.intlloyalty.detailsservice.request._1.LoyaltyDetailsRequest;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.americanexpress.wss.shr.authorization.token.TokenException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class IntlLoyaltyRequestBuilder implements SoapConsumer{
    
    public  static String preferedlanguage = null;
    private static final String SECURITY_TOKEN_PARAM = "security_token";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String CLIENT_ID_VAL = "AmexAPI";
    private static final String CORRELATION_ID_PARAM = "correlation_id";
    
        @SuppressWarnings("deprecation")
		public LoyaltyDetailsRequest buildRequest(List<String> accountNumbers, String userId)
        {
            
            CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
            SecurityToken securityToken = collectionUploadUtil.generateClaims(userId,"");
            
            /*Invoking Member to get language preference value*/
            String accountServiceEndpointUrl = "https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/member/v1/accounts";
            RestServiceInvoker invoker = new RestServiceInvoker();
            HashMap<String, Object> headers = new HashMap<String, Object>();
            try {
                //System.out.println("publicGuid >>"+publicGuid);

                headers.put(CLIENT_ID_PARAM, CLIENT_ID_VAL);
                headers.put(SECURITY_TOKEN_PARAM, securityToken.asXML());
                headers.put(CLIENT_ID_PARAM, "AmexAPI");
                headers.put( CORRELATION_ID_PARAM, RandomUtils.nextDouble( 0.0, 1.0 ) );
                headers.put("Content-Type", "application/json");

            } catch (Exception e) {
                e.printStackTrace();
            }

            Response response = invoker.get(accountServiceEndpointUrl, null, null, headers);
            //System.out.println("THE RESPONSE STATUS IS " + response.getStatus());
            if (response.getStatus() == 200 || response.getStatus() == 206) {
                String responseContent = response.readEntity(String.class);
                //System.out.println("Response from cards ***** :" + responseContent);
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                List<BackendAccountEntity> backendAccountEntityList = gson.fromJson(responseContent,
                        new TypeToken<List<BackendAccountEntity>>() {
                        }.getType());

                //userAccountsBean.setBackendAccountEntities(backendAccountEntityList);
                preferedlanguage = backendAccountEntityList.get(0).getHolder().getLocalizationPreferences().getLanguagePreference().substring(0, 2).toUpperCase();
            }
            
            LoyaltyDetailsRequest loyaltyDetailsRequest = new LoyaltyDetailsRequest();
            					  SecureCommonRequestParamDef secureCommonRequestParamDef = new SecureCommonRequestParamDef();
            					  				CommonRequestParamDef commonRequestParamDef = new CommonRequestParamDef();
            					  							AuditMapDef auditMap = new AuditMapDef();
            					  							auditMap.setApplicationID("IntlLoyalty");
            					  				commonRequestParamDef.setAuditMap(auditMap);
            					  secureCommonRequestParamDef.setCommonRequestParam(commonRequestParamDef);
            					  try {
									secureCommonRequestParamDef.setSecurityTokenData(ServiceUtil.encodeSecurityToken(securityToken));
								} catch (TokenException | UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            			          loyaltyDetailsRequest.setSecureCommonRequestParam(secureCommonRequestParamDef);			  				
            loyaltyDetailsRequest.getCardAccount().addAll(accountNumbers);
            loyaltyDetailsRequest.getFragments().add(Fragment.MR_REWARDS_BALANCE);
            loyaltyDetailsRequest.setPreferedLanguage(preferedlanguage);
            //loyaltyDetailsRequest.setIncludeReportedCanceledCard(true);
            //commonRequestParamDef.setCorrelationID(correlationId);
            return loyaltyDetailsRequest;
        }

		
}
		
            