package com.americanexpress.dc.soap.impl;


import com.americanexpress.as.schemas.fsresponseservice._2.*;
import com.americanexpress.as.sfwk.shr.ServiceUtil;
import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;

import java.util.ArrayList;
import java.util.List;


public class FinsSoapConsumerImpl implements SoapConsumer
{

  private static final Integer CYCLE_INDEX = 0;

  // Build Fins request object
  public FinancialServiceRequest buildRequest( List<String> accountNumbers, String userId )
  {
    FinancialServiceRequest financialServiceRequest = new FinancialServiceRequest();
    try
    {
      // Call to financial soap service starts
      CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
      SecurityToken securityToken = collectionUploadUtil.generateClaims( userId,"E2_PROD" );

      financialServiceRequest.setApplicationName( Constants.DEFAULT_CLIENT_ID );
      financialServiceRequest.setSecurityToken( ServiceUtil.encodeSecurityToken( securityToken ) );

      CardFinancialsRequest cardFinancialsRequest = new CardFinancialsRequest();
      PeriodCriteria periodCriteria = new PeriodCriteria();
      periodCriteria.setCycleIndex( CYCLE_INDEX );
      cardFinancialsRequest.setPeriod( periodCriteria );

      cardFinancialsRequest.setCyclesRequired( false );
      cardFinancialsRequest.setCyclesMetaDataRequired( false );
      cardFinancialsRequest.setTransactionsRequired( false );
      cardFinancialsRequest.setBalancesRequired( true );

      DataSources dataSources = new DataSources();
      DataSourceConfig dataSourceConfig = new DataSourceConfig();
      dataSourceConfig.setUse( true );
      dataSourceConfig.setRefreshCache( false );
      dataSourceConfig.setFailIfUnavailable( true );
      dataSources.setUseStatementCache( dataSourceConfig );
      cardFinancialsRequest.setDataSources( dataSources );

      cardFinancialsRequest.setRealTimeARDataRequired( false );
      cardFinancialsRequest.setMultiCurrencyDataRequired( false );

      AccountRequest accountRequest = new AccountRequest();
      accountRequest.setAccountNumber( accountNumbers.get(0) );
            
      cardFinancialsRequest.setAccount( accountRequest );

      List<CardFinancialsRequest> cardFinancialsRequestList = new ArrayList<CardFinancialsRequest>();
      cardFinancialsRequestList.add( cardFinancialsRequest );
      financialServiceRequest.getCardRequests().addAll( cardFinancialsRequestList );
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
    return financialServiceRequest;
  }
}
