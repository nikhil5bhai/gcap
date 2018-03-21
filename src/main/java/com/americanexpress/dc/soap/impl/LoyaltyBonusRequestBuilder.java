package com.americanexpress.dc.soap.impl;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.loyalty.enterprise.services.bonustrans.getqualifiedtransdetails.v1.request.*;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.ClientContext;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.CommonRequestContextType;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.RequestContext;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.ServiceContext;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.americanexpress.wss.shr.authorization.token.TokenException;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Created by rpotlapa on 2/9/2017.
 */
public class LoyaltyBonusRequestBuilder implements SoapConsumer {

  @Override
  public QualifiedTransDetailsWSRequest buildRequest(List<String> accountNumbers, String userId) {

    QualifiedTransDetailsWSRequest qualifiedTransDetailsWSRequest=null;
    try {
      SecurityToken securityToken = null;
      CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
      securityToken = collectionUploadUtil.generateClaims(userId,"");
      LoyaltyBonusRequestBuilder loyaltyBonusRequestBuilder = new LoyaltyBonusRequestBuilder();
      for(String acctNumber : accountNumbers){
         if(acctNumber != null) {
           qualifiedTransDetailsWSRequest = loyaltyBonusRequestBuilder.populateLoyaltyBonusSummaryWSRequest(securityToken,acctNumber);
           break;
         }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return qualifiedTransDetailsWSRequest;
  }

  public QualifiedTransDetailsWSRequest populateLoyaltyBonusSummaryWSRequest(SecurityToken securityToken, String accountNumber)
{

    QualifiedTransDetailsWSRequest qualifiedTransDetailsWSRequest = new QualifiedTransDetailsWSRequest();
  CommonRequestContextType commonRequestContextType = null;
  try {
    commonRequestContextType = populateCommonRequestContextType(securityToken);
  } catch (TokenException e) {
    e.printStackTrace();
  } catch (UnsupportedEncodingException e) {
    e.printStackTrace();
  }

  LoyaltyRequestParamsType loyaltyReqParmsType = new LoyaltyRequestParamsType();
    AccountInfo accountInfo = new AccountInfo();
    accountInfo.setAccountNumber(accountNumber);
    accountInfo.setAccountType("CARD_ACCOUNT_NUMBER");
    loyaltyReqParmsType.setAccountInfo(accountInfo);

    ProgramFilter progrmFilter = new ProgramFilter();
    progrmFilter.setProgramTypeCode("003");
    progrmFilter.setProgramDef("Y");
    loyaltyReqParmsType.setProgramFilter(progrmFilter);

    RequestFilter requestFilter = new RequestFilter();
    requestFilter.setResponseType("S");
    loyaltyReqParmsType.setRequestFilter(requestFilter);

    PageFilter pageFilter = new PageFilter();
    pageFilter.setMaxResults("1");
    loyaltyReqParmsType.setPageFilter(pageFilter);

    qualifiedTransDetailsWSRequest.setCommonRequestContextType(commonRequestContextType);
    qualifiedTransDetailsWSRequest.setLoyaltyRequestParamsType(loyaltyReqParmsType);

    return qualifiedTransDetailsWSRequest;
  }

  private CommonRequestContextType populateCommonRequestContextType(SecurityToken securityToken) throws TokenException, UnsupportedEncodingException {
    String sessionID = securityToken.getSessionID();
    CommonRequestContextType commonRequestContextType = new CommonRequestContextType();
    ClientContext clientContext = new ClientContext();
    clientContext.setBusinessProcess("BONUS");
    clientContext.setConversationID(sessionID);
    clientContext.setSystemID("USRelationshipSummary");
    clientContext.setChannelId("WEB");
    clientContext.setPrincipal(securityToken.asXML());
    clientContext.setPrincipalType("ASXML");
    commonRequestContextType.setClientContext(clientContext);

    ServiceContext serviceContext = new ServiceContext();
    serviceContext.setServiceMinorVersion("1.0");
    serviceContext.setServiceMajorVersion("1.0");
    commonRequestContextType.setServiceContext(serviceContext);

    RequestContext requestContext = new RequestContext();
    Calendar calendar = Calendar.getInstance();
    Date date = calendar.getTime();
    requestContext.setCorrelationID(dateToXmlDate(date).toString());
    requestContext.setTimestamp(dateToXmlDate(date));
    commonRequestContextType.setRequestContext(requestContext);

    return commonRequestContextType;
  }

  public XMLGregorianCalendar dateToXmlDate(Date date) {
    final String methodName = "dateToXmlDate";
    XMLGregorianCalendar xmlDate = null;
    if (date != null) {
      GregorianCalendar gcal = (GregorianCalendar) Calendar.getInstance();
      gcal.setTime(date);
      try {
        xmlDate = DatatypeFactory.newInstance()
          .newXMLGregorianCalendar(gcal);
      } catch (DatatypeConfigurationException e) {
         System.out.println("exp");
      }
    }
    return xmlDate;
  }
}
