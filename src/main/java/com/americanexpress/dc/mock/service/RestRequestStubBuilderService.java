package com.americanexpress.dc.mock.service;


import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilderService;
import com.americanexpress.dc.mock.service.stub.BodyContainsAccountNumber;
import com.americanexpress.dc.mock.service.stub.HeaderContainsMemberTokens;
import com.americanexpress.dc.mock.service.stub.HeaderContainsSecurityToken;

import java.util.Map;
import java.util.TreeMap;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;



public class RestRequestStubBuilderService extends RequestStubBuilderService
{

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public RestRequestStubBuilderService()
  {
	  map.put
	  (
		  "lxnx",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/risk/credit/v1/institution/external_data//business_verification_results" )
		  )
	  );
	  map.put
	  (
		  "axciom",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/acquisition/digital/v1/credit_application/underwriting/inquiry_results" )
		  )
	  );
	  map.put
	  (
		  "cbis",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/security/v1/identity" )
		  )
	  );
	  map.put
	  (
		  "lem",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/loyalty/earn/event/v1/sources" )
		  )
	  );
	  map.put
	  (
		  "bdpfulfillment",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/risk/credit/v1/resources/accounts/income/update" )
		  )
	  );
	  map.put
	  (
		  "aic",
		  new BodyContainsAccountNumber
		  (
			  POST,
			  urlPathEqualTo( "/risk/credit/v1/individual/credit_decisions/offer_controls/search" )
		  )
	  );
  }

  @Override
  public RequestStubBuilder getBuilder( final String key )
  {
    return map.get( key );
  }
}
