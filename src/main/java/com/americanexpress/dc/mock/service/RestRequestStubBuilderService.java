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


/** @author Richard Wilson */
public class RestRequestStubBuilderService extends RequestStubBuilderService
{

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public RestRequestStubBuilderService()
  {
    map.put
        (
            "loyalty",
            new HeaderContainsMemberTokens
                (
                    GET,
                    urlPathEqualTo( "/account_servicing/loyalty/v1/details" )
                )
        );
    map.put
        (
            "LoyaltyServiceWS_PendingPoints",
            new HeaderContainsMemberTokens
                (
                    GET,
                    urlPathEqualTo
                        (
                        "/loyalty/services/process/loyaltystmt/v2/rwdssummary/summary"
                        )
                )
        );
    map.put
        (
            "member",
            new HeaderContainsSecurityToken
                (
                    GET,
                    urlPathEqualTo( "/account_servicing/member/v1/accounts" )
                )
        );
	map.put
		(
			"crps_address_inquiry",
			new BodyContainsAccountNumber
					(
					POST,
					urlPathEqualTo( "/360customer/v1/consumer_account_management/accounts/inquiry_results" )
				)
		);
	map.put
		(
			"crps_address_update",
			new BodyContainsAccountNumber
				(
					PUT,
					urlPathEqualTo( "/360customer/v1/consumer_account_management/accounts/address" )
				)
		);
	map.put
		(
			"crps_address_search",
			new HeaderContainsSecurityToken
				(
					POST,
					urlPathEqualTo( "/360customer/v1/data_quality_management/addresses/standardization_results" )
				)
		);
	  map.put
		  (
			  "pin_details",
			  new BodyContainsAccountNumber
			  (
				  POST,
				  urlPathEqualTo( "/risk/fraud/v1/pin_management/pins/inquiry_results" )
			  )
		  );
	  map.put
		  (
			  "pin_update",
			  new BodyContainsAccountNumber
			  (
					  PUT,
					  urlPathEqualTo( "/risk/fraud/v1/pin_management/pins" )
			  )
		  );
	  map.put
		  (
			  "pin_create",
			  new BodyContainsAccountNumber
			  (
				  PUT,
				  urlPathEqualTo( "/risk/fraud/v1/pin_management/pins" )
			  )
		  );
	  map.put
			  (
					  "PIN_STATUS_URL",
					  new HeaderContainsMemberTokens
							  (
									  GET,
									  urlPathEqualTo( "/risk/fraud/v1/pin_management/pins/inquiry_results" )
							  )
			  );
  }

  @Override
  public RequestStubBuilder getBuilder( final String key )
  {
    return map.get( key );
  }
}

