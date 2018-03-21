package com.americanexpress.dc.util;


import com.americanexpress.dc.soap.impl.LoyaltyBonusRequestBuilder;
import com.americanexpress.dc.soap.impl.CSSSoapConsumerImpl;
import com.americanexpress.dc.soap.impl.FinsSoapConsumerImpl;
import com.americanexpress.dc.soap.impl.IntlLoyaltyRequestBuilder;
import com.americanexpress.dc.soap.impl.LAISoapConsumerImpl;
import com.americanexpress.dc.soap.impl.PaymentServiceRequestBuilder;
import com.americanexpress.dc.soap.impl.SoapConsumer;
import com.americanexpress.dc.soap.impl.ISLDirectDebitEnrollRequestBuilder;
import com.americanexpress.dc.soap.impl.ISLDirectDebitCancelRequestBuilder;



public class SoapRequestBuilderFactory
{

  public SoapConsumer getSoapImplementation(String collectionName)
  {
    if( Constants.PAYMENTS_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
      return new FinsSoapConsumerImpl();
    }
    else if( Constants.CREDIT_LIMITS_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
      return new CSSSoapConsumerImpl();
    }
    else if( Constants.PAYMENTS_SUMMARY_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
      return new PaymentServiceRequestBuilder();
    }
    else if( Constants.US_LOYALTY_INFO_ACCOUNT_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
      return new LAISoapConsumerImpl();
    }
    else if( Constants.INTL_LOYALTY_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
    	return new IntlLoyaltyRequestBuilder();
    }
    else if( Constants.LOYALTY_BONUS_COLLECTION_NAME.equalsIgnoreCase( collectionName ))
    {
      return new LoyaltyBonusRequestBuilder();
    }
    else if( Constants.ISL_DD_ENROLL_COLLECTION_NAME.equalsIgnoreCase( collectionName ))
    {
      return new ISLDirectDebitEnrollRequestBuilder();
    }
    else if( Constants.ISL_DD_CANCEL_COLLECTION_NAME.equalsIgnoreCase( collectionName ))
    {
      return new ISLDirectDebitCancelRequestBuilder();
    }
    

    return null;
  }
}
