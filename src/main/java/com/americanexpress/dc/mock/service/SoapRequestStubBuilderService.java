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



public class SoapRequestStubBuilderService extends RequestStubBuilderService
{

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public SoapRequestStubBuilderService()
  {
    map.put( "bridger", new BodyContainsAccountNumber ( POST, urlPathEqualTo ("/GSRealTimeSignedService/GlobalSanctionsScreening.svc" )));
    map.put( "truview", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/emsws/Clic2" ) ));
    map.put( "cpc", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/FraudAndPrivacy/GPCC/CustomerPreferenceService/V1" ) ));
    map.put( "cpcprecheck", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/CRPS/AccountSummaryProfileService/V2" ) ));
    map.put( "lxn01", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/XmlVpn/FNProxy.aspx" ) ));
    map.put( "lxn02", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/InstantID" ) ));
    map.put( "lxn03", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/ComprehensiveADL" ) ));
    map.put( "fst01", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/WsXformService/wsxform" ) ));
    map.put( "triumph_basic", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/SetupAccountServicev1/v1" ) ));
    map.put( "triumph_supp", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/SetupAccountServicev1/v1/supp" ) ));
    map.put( "cas400", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/CAS/GlobalLimitsService/V2" ) ));
    map.put( "cas151", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/CAS/AccountProfileMaintenanceService/V2" ) ));
    map.put( "exchangemedia", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/IPD_TestSupport/IssuePaymentDeviceService.svc" ) ));
    map.put( "grmsfulfillment", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/GRAT/GRMS/InstantAccountService/V1" ) ));
    map.put( "cas505", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/CAS/CardSecurityService/V4" ) ));
    map.put( "dcpe", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/ecssvc/eaddser/EAddressUpdateService/Email" ) ));
    map.put( "dcpm", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/ecssvc/eaddser/EAddressUpdateService/SMS" ) ));
    map.put( "appc", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/FraudAndPrivacy/GPCC/GlobalExternalNumberService/V1" ) ));
    map.put( "afm", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/AFM/ProductTransferInfoService/ProductTransferInfoService" ) ));
    map.put( "dqme", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/GetMatchedConsumer/DQME" ) ));
    map.put( "grms", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/GRAT/GRMS/CustomerRiskInformationService/SinglePayee/V1" ) ));
    map.put( "sow", new BodyContainsAccountNumber ( POST, urlPathEqualTo( "/ExperianRequest/ShareOfWallet/sow" ) ));
  }

  @Override
  public RequestStubBuilder getBuilder( final String key )
  {
    return map.get( key );
  }
}
