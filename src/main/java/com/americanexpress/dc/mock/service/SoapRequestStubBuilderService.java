package com.americanexpress.dc.mock.service;


import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilderService;
import com.americanexpress.dc.mock.service.stub.BodyContainsAccountNumber;

import java.util.Map;
import java.util.TreeMap;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;



public class SoapRequestStubBuilderService extends RequestStubBuilderService
{

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public SoapRequestStubBuilderService()
  {
    map.put( "bridger", anyUrlPath( "/GSRealTimeSignedService/GlobalSanctionsScreening.svc" ) );
  }

  private BodyContainsAccountNumber anyUrlPath( final String urlPath, final String... bodyParts )
  {
    return new BodyContainsAccountNumber( ANY, urlPathEqualTo( urlPath ), bodyParts );
  }

  @Override
  public RequestStubBuilder getBuilder( final String key )
  {
    return map.get( key );
  }
}
