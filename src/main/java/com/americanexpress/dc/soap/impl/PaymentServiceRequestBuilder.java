package com.americanexpress.dc.soap.impl;



import java.util.List;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.schemas.as.globalservicedata._1.AuditMapDef;
import com.americanexpress.schemas.as.globalservicedata._1.CommonRequestParamDef;
import com.americanexpress.schemas.as.globalservicedata._1.SecureCommonRequestParamDef;
import com.americanexpress.schemas.as.paymentservice._2.*;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;

/**
 * Created by rpotlapa on 8/3/2016.
 */
public class PaymentServiceRequestBuilder implements SoapConsumer{



  public InquiryRequestDef buildRequest(List<String> accountNumbers, String userId){
    InquiryRequestDef inquiryRequestDef = new InquiryRequestDef();

    try {
      SecurityToken securityToken = null;
      CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
      securityToken = collectionUploadUtil.generateClaims(userId,"");

      inquiryRequestDef.setBusinessDetails(populateBusinessDetailsDef());
      inquiryRequestDef.setSecureCommonRequestParam(populateSecureCommonRequestParamDef(
        securityToken.asXML(),"USPayByComputer","mocking112233") );
      inquiryRequestDef.setChannelID("PaymentCenter");
      inquiryRequestDef.getCardAccountNumberList().add(accountNumbers.get(0));
      inquiryRequestDef.setPaymentHistorySegmentReqDef(populatePaymentHistorySegmentReqDef());
      inquiryRequestDef.setPaybillSegmentReqDef(populatePaybillSegmentReqDef());
    }catch (Exception ex){
      ex.printStackTrace();

    }
    return inquiryRequestDef;
  }



  /**
   *
   * @return paybillSegmentReqDef
   */
  private PaybillSegmentReqDef populatePaybillSegmentReqDef(){
    PaybillSegmentReqDef paybillSegmentReqDef = new PaybillSegmentReqDef();
    paybillSegmentReqDef.setBankDataRequired(false);
    paybillSegmentReqDef.setPayBillRequired(false);
    paybillSegmentReqDef.setPaymentOptionsRequired(false);
    return  paybillSegmentReqDef;
  }

  /**
   *
   * @return paymentHistorySegmentReqDef
   */
  private PaymentHistorySegmentReqDef populatePaymentHistorySegmentReqDef(){
    PaymentHistorySegmentReqDef paymentHistorySegmentReqDef = new PaymentHistorySegmentReqDef();
    paymentHistorySegmentReqDef.setPaymentHistoryCount(1);
    paymentHistorySegmentReqDef.setPaymentHistoryRequired(true);
    paymentHistorySegmentReqDef.setSupplementaryIndicator(true);
    paymentHistorySegmentReqDef.setReturnedPaymentsRequired(true);
    paymentHistorySegmentReqDef.setStopPayEligibilityRequired(true);
    return  paymentHistorySegmentReqDef;
  }

  public BusinessDetailsDef populateBusinessDetailsDef(){
    BusinessDetailsDef businessDetailsDef = new BusinessDetailsDef();
    businessDetailsDef.setCountryCode("US");
    businessDetailsDef.setCurrencyCode("USD");
    businessDetailsDef.setCollectionsIndicator(false);
    return businessDetailsDef;
  }

  public SecureCommonRequestParamDef populateSecureCommonRequestParamDef(String securityToken,
                                                                         String appID, String correlationID) {
    SecureCommonRequestParamDef secureCommonRequestParamDef = new SecureCommonRequestParamDef();
    secureCommonRequestParamDef.setSecurityTokenData(securityToken);
    CommonRequestParamDef commonRequestParamDef = new CommonRequestParamDef();
    AuditMapDef auditMapDef = new AuditMapDef();
    auditMapDef.setApplicationID(appID);
    commonRequestParamDef.setAuditMap(auditMapDef);
    commonRequestParamDef.setCorrelationID(correlationID);
    secureCommonRequestParamDef.setCommonRequestParam(commonRequestParamDef);
    return secureCommonRequestParamDef;
  }

}
