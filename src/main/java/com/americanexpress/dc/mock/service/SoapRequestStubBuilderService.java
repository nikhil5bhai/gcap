package com.americanexpress.dc.mock.service;


import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilderService;
import com.americanexpress.dc.mock.service.stub.BodyContainsAccountNumber;

import java.util.Map;
import java.util.TreeMap;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;


/** @author Richard Wilson */
public class SoapRequestStubBuilderService extends RequestStubBuilderService
{

  private final Map<String, RequestStubBuilder> map = new TreeMap<>();

  public SoapRequestStubBuilderService()
  {
    map.put( "EAddressInquiryService", anyUrlPath( "/ecommsvc/eaddser/EAddressInquiryService" ) );
    map.put( "IntlLoyaltyService", anyUrlPath( "/myca/mrdetails/v1/IntLoyaltyDetailsServiceWS" ) );
    map.put( "EAddressUpdateService", anyUrlPath( "/ecommsvc/eaddser/EAddressUpdateService" ) );
    map.put( "PrivacyPreferenceUpdateServicePort", anyUrlPath( "/ecommsvc/privpref/PrivacyPreferenceUpdateServicePort" ) );
    map.put( "AlertsPreferenceUpdateServicePort", anyUrlPath( "/ecommsvc/alertspref/AlertsPreferenceUpdateServicePort" ) );
    map.put( "PaymentServiceWS", anyUrlPath( "/myca/pays/v2/PaymentServiceWS" ) );
    map.put( "ACPUpdateService", anyUrlPath( "/ecommsvc/acpref/ACPUpdateService" ) );
    map.put( "IncomeCaptureServiceInquiry", anyUrlPath( "/GRAT/GRMS/CustomerRiskInformationService/V1", "</ns1:getCustomerRiskInfo>" ) );
    map.put( "IncomeCaptureServiceUpdate", anyUrlPath( "/GRAT/GRMS/CustomerRiskInformationService/V1", "</ns2:updateClientLevelLineData>" ) );
    map.put( "StatementDeliveryPreferenceInquiryService", anyUrlPath( "/myca/global/sdp/StatementDeliveryPreferenceInquiryService" ) );
    map.put( "FinancialService", anyUrlPath( "/myca/services/fins/v2/FinancialService" ) );
    map.put( "LoyaltyServiceWS", anyUrlPath( "/loyalty/services/process/loyaltyaccountinfo/v1/LoyaltyAccountInfoNonSecuredServiceWS" ) );
    map.put( "LoyaltyBonusTransNonSecuredServiceWS", anyUrlPath( "/loyalty/services/process/bonusinfo/LoyaltyBonusTransNonSecuredServiceWS" ) );
    map.put( "StatementDeliveryPreferenceUpdateService", anyUrlPath( "/myca/global/sdp/StatementDeliveryPreferenceUpdateService" ) );
    map.put( "AlertsPreferenceInquiryServicePort", anyUrlPath( "/ecommsvc/alertspref/AlertsPreferenceInquiryServicePort" ) );
    map.put( "ACPInquiryService", anyUrlPath( "/ecommsvc/acpref/ACPInquiryService" ) );
    map.put( "PrivacyPreferenceInquiryServicePort", anyUrlPath( "/ecommsvc/privpref/PrivacyPreferenceInquiryServicePort" ) );
    map.put( "CreditStatusServiceWS", anyUrlPath( "/myca/creditstatus/v1/CreditStatusServiceWS" ) );
	map.put( "AccountInfoService", anyUrlPath( "/ICS/AccountInfoService/v3.7" ) );
	map.put( "AccountUpdateService", anyUrlPath( "/ICS/AccountUpdateService/v5.2" ) );
	map.put( "CorpAccountProfileService", anyUrlPath( "/CORPORATE/CorpAccountProfileService/V2" ) );
	map.put( "ICorpAccountProfileService", anyUrlPath( "/CORPORATE/CorpAccountProfileService/V1" ) );
    map.put( "AccountUpdateService_DD_Enroll", anyUrlPath( "/ICS/AccountUpdateService/v5.2", "startDT" ) );
    map.put( "AccountUpdateService_DD_Cancel", anyUrlPath( "/ICS/AccountUpdateService/v5.2", "NoDD" ) );
    map.put( "IssuanceHistoryService", anyUrlPath( "/worldservice/IHR/IssuanceHistoryService/V3" ) );
    map.put( "AccountDetailsCompositeService", anyUrlPath( "/IntegrationService/USCSOT/AccountDetailsCompositeService/v1" ) );
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
