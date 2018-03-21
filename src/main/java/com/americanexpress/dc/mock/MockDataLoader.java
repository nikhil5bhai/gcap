package com.americanexpress.dc.mock;


import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.mock.transformer.MongoResponseLabeler;
import com.americanexpress.dc.mock.transformer.MongoResponseUploader;
import com.americanexpress.dc.mock.wiremock.TLSWireMockServer;
import com.americanexpress.dc.util.App;
import com.americanexpress.dc.util.WireMockUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.americanexpress.dc.config.MongoConfiguration.getDatabase;
import static com.americanexpress.dc.util.Constants.RESOURCE_KEY;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.nin;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.*;


public class MockDataLoader
{

  @SuppressWarnings("unchecked")
  public static void main( String[] args ) throws IOException
  {
	
    final WireMockConfiguration config = new WireMockConfiguration();

    config.extensions
        (
            MongoResponseLabeler.class,
            MongoResponseUploader.class
        );


    config
        .port( 8093 )
/*
        .httpsPort(8443)
        .keystorePath("/opt/app-root/ssl/truststore")
        .keystorePassword("Password#123")
        .needClientAuth(false)
*/
        .disableRequestJournal()
        .containerThreads( 200 )
        .jettyAcceptors( -1 )
        .jettyAcceptQueueSize( 1000 )
        .jettyHeaderBufferSize( 8192 );

    final WireMockServer wireMockServer = new TLSWireMockServer( config );
    WireMock.configureFor( config.portNumber() );

    ProxyInitializer.initialize( wireMockServer );

    // start on port 8080,
    // no HTTPS
    App.ConfigProperties.loadPropertyFile( "config_mock_member.properties" );
    App.ConfigProperties.loadQueryPropertyFile( "config_query_params.properties" );
    App.ConfigProperties.loadPathPropertyFile( "config_path_params.properties" );
    App.ConfigProperties.loadHeaderPropertyFile( "config_header.properties" );

    final Properties mockProperties = App.ConfigProperties.getPropertyKeyValue();
    for( final String serviceCallName : mockProperties.stringPropertyNames() )
    {
      final String endpointPath = mockProperties.getProperty( serviceCallName );
      if( contains( serviceCallName, "_rest") )
      {
        // Loading REST services
        loadRESTMockService( wireMockServer, serviceCallName, endpointPath );
      }
      else if( contains( serviceCallName, "_soap" ) )
      {
        // Loading Soap services
        loadSOAPMockService( wireMockServer, serviceCallName, endpointPath );
      }
    }

    wireMockServer.start();
    System.out.println( "Mock started on http port#" + wireMockServer.port()/*+" and https port # "+wireMockServer.httpsPort()*/ );
  }

  private static void loadSOAPMockService
      (
          final WireMockServer wireMockServer,
          final String serviceCallName,
          final String endpointUrl
      )
  {
    final String collectionName = substringBeforeLast( serviceCallName, "_" );

    final Bson filter
        = and
        (
            nin( "userId", null, "" ),
            nin( "account_token", null, "" ),
            nin( "account_number", null, "" ),
            nin( "publicGuid", null, "" ),
            nin( "privateGuid", null, "" )
        );

    try
        (
            final MongoCursor<Document> cursor
                = getDatabase()
                .getCollection( collectionName )
                .find( filter )
                .iterator()
        )
    {
      while( cursor.hasNext() )
      {
        final Document document = cursor.next();

        final ObjectId documentId = document.getObjectId( "_id" );

        final Document resource = document.get( RESOURCE_KEY, Document.class );

        final String responseId
            = ofNullable( documentId )
            .map( ObjectId::toHexString )
            .orElse( null );

        final UserAccountsBean user = createUserAccountsBean( document );

        final WireMockUtil mockUtil = new WireMockUtil();

        mockUtil.wireSOAPMockData
            (
                wireMockServer,
                document,
                endpointUrl,
                user,
                serviceCallName,
                responseId,
                resource
            );
      }
    }
  }

  private static void loadRESTMockService
      (
          final WireMockServer wireMockServer,
          final String serviceCallName,
          final String endpointUrl
      )
  {
    final String collectionName = substringBeforeLast( serviceCallName, "_" );

    Bson filter = null;

    //System.out.println( "coll name" + collectionName );

    if( serviceCallName.contains( "rest" ) )
    {
      if( collectionName.contains( "cardart" ) )
      {
        filter
            = and
            (
                nin( "userId", null, "" ),
                nin( "account_token", null, "" ),
                //nin( "account_number", null, "" ),
                nin( "publicGuid", null, "" ),
                nin( "privateGuid", null, "" ),
                nin( "digital_asset", null, "" )
            );
      }
      else
      {
        filter
            = and
            (
                nin( "userId", null, "" ),
                nin( "account_token", null, "" ),
                //nin( "account_tokens", null, "" ),
                nin( "publicGuid", null, "" ),
                nin( "privateGuid", null, "" )
            );
      }
    }
    else
    {
      filter
          = and
          (
              nin( "userId", null, "" ),
              nin( "account_token", null, "" ),
              nin( "account_number", null, "" ),
              nin( "publicGuid", null, "" ),
              nin( "privateGuid", null, "" )
          );
    }

    /*final Bson filter
        = and
        (
            nin( "userId", null, "" ),
            nin( "account_token", null, "" ),
            nin( "account_number", null, "" ),
            nin( "publicGuid", null, "" ),
            nin( "privateGuid", null, "" )
        );*/

    try
        (
            final MongoCursor<Document> cursor
                = getDatabase()
                .getCollection( collectionName )
                .find( filter )
                .iterator()
        )
    {
      while( cursor.hasNext() )
      {
        final Document document = cursor.next();

        final ObjectId documentId = document.getObjectId( "_id" );

        final Document resource = document.get( RESOURCE_KEY, Document.class );

        final String responseId
            = ofNullable( documentId )
            .map( ObjectId::toHexString )
            .orElse( null );

        final UserAccountsBean user = createUserAccountsBean( document );

        final WireMockUtil mockUtil = new WireMockUtil();

        // Gets the data from Mock data
        mockUtil.wireRESTMockData
            (
                wireMockServer,
                document,
                endpointUrl,
                user,
                serviceCallName,
                responseId,
                resource
            );
      }
    }
  }

  private static UserAccountsBean createUserAccountsBean( final Document document )
  {
    final UserAccountsBean user = new UserAccountsBean();
    user.setPrivateGuid( getStringValue( document, "privateGuid" ) );
    user.setUserId( getStringValue( document, "userId" ) );
    //user.setAccountToken( getStringValue( document, "account_token" ) );
    List<String> accTokensLst = getStringValueForList( document, "account_token" );
    
    if(accTokensLst != null && accTokensLst.size() > 0){
    	if( accTokensLst.get(0).contains(",")){
        	user.setAccountTokens(accTokensLst);
        	System.out.println("multi");
        }else{
        	System.out.println("single"+accTokensLst.get(0).toString());
        	user.setAccountToken(accTokensLst.get(0).toString());
        }
    }
    

    user.setAccountNumber( getStringValue( document, "account_number" ) );
    user.setPublicGuid( getStringValue( document, "publicGuid" ) );
    user.setDigitalAssetId( getStringValue( document, "digital_asset" ));
    user.setLocale(getStringValue(document,"locale"));
    user.setMarketCode(getStringValue(document,"market_code"));
    return user;
  }

  private static String getStringValue( final Document document, final String key )
  {
    if( document != null )
    {
      return trimToEmpty( document.getString( key ) );
    }
    return null;
  }

  private static List<String> getStringValueForList( final Document document, final String key )
  {
    if( document != null )
    {
    	List<String> strList= null;
    	if(document.get(key)  != null){
    		strList= new ArrayList<>();
    		strList.add(document.get(key).toString() );

    	}
      return strList;
    }
    return null;
  }

}
