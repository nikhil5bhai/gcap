package com.americanexpress.dc.mock.service;


import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilderService;
import com.americanexpress.dc.mock.service.stub.HeaderContainsMemberTokens;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;



public class FinancialsRequestStubBuilderService extends RequestStubBuilderService
{

  private static final Map<String, StringValuePattern> PAGINATION_QUERY_MAP;
  static
  {
    final Map<String, StringValuePattern> initMap = new LinkedHashMap<>();
    initMap.put( "offset", containing( "0" ) );
    initMap.put( "limit", containing( "1000" ) );
    PAGINATION_QUERY_MAP = unmodifiableMap( initMap );
  }

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public FinancialsRequestStubBuilderService()
  {
    final Map<String, StringValuePattern> pendingQueryMap = new LinkedHashMap<>( PAGINATION_QUERY_MAP );
    pendingQueryMap.put( "status", containing( "pending" ) );
    map.put( "transactions_pending", getUrlPath( "/financials/v1/transactions", pendingQueryMap ) );

    final Map<String, StringValuePattern> postedQueryMap = new LinkedHashMap<>( PAGINATION_QUERY_MAP );
    postedQueryMap.put( "status", containing( "posted" ) );
    map.put( "transactions_posted", getUrlPath( "/financials/v1/transactions", postedQueryMap ) );

    map.put( "balances", getUrlPath( "/financials/v1/balances" ) );
    map.put( "statement_periods", getUrlPath( "/financials/v1/statement_periods" ) );
    map.put( "interest_rates", getUrlPath( "/financials/v1/interest_rates" ) );
    map.put( "supplementary_account_summary", getUrlPath( "/financials/v1/supplementary_account_summary" ) );
    map.put( "transaction_summary", getUrlPath( "/financials/v1/transaction_summary", singletonMap( "type", containing( "split_by_cardmember" ) ) ) );
  }

  private HeaderContainsMemberTokens getUrlPath( final String urlPath )
  {
    return new HeaderContainsMemberTokens( GET, urlPathEqualTo( urlPath ) );
  }

  private HeaderContainsMemberTokens getUrlPath( final String urlPath, final Map<String, StringValuePattern> queryMap )
  {
    return new HeaderContainsMemberTokens( GET, urlPathEqualTo( urlPath ), queryMap );
  }

  @Override
  public RequestStubBuilder getBuilder( final String key )
  {
    return map.get( key );
  }
}

