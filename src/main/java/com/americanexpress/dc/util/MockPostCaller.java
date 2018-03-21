package com.americanexpress.dc.util;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static com.americanexpress.dc.config.MongoConfiguration.getDatabase;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;


// TODO: 12/5/16 Should this be refactored to enable inline response retrival for MongoDB and uploading into MongoDB
public class MockPostCaller
{

  public void loadSOAPMockService
      (
          final WireMockServer wireMockServer,
          final String collectionName,
          final String endpointUrl
      )
  {
    try
        (
            final MongoCursor<Document> cursor
                = getDatabase().getCollection( collectionName ).find().iterator()
        )
    {
      while( cursor.hasNext() )
      {
        final Document document = cursor.next();

        final String jsonString = document.getString( "response" );

        final String value;
        if( "accounts_mapper".equalsIgnoreCase( collectionName ) )
        {
          System.out.println( "In accounts_mapper collection" );
          value = document.getString( "account_token" );
          System.out.println( "account_token" + document.getString( "account_token" ) );
        }
        else if( "cardart".equalsIgnoreCase( collectionName ) )
        {
          System.out.println( "In cardart collection" );
          value = document.getString( "digital_asset" );
          System.out.println( "digital_asset" + value );
        }
        else
        {
          value = null;
        }
        if( value != null )
        {
          loadSoapWiremockMappingFiles( wireMockServer, jsonString, endpointUrl, value );
        }
      }
    }
  }

  /**
   * {@code Method loadSoapWiremockMappingFiles to load the Soap responses}
   *
   * @param wireMockServer
   *     the WireMock Server instance
   * @param contents
   *     the response body content to be wired
   * @param urlPattern
   *     the URL Pattern
   * @param value
   *     the value to be contained within the request body
   */

  public void loadSoapWiremockMappingFiles
  (
      final WireMockServer wireMockServer,
      final String contents,
      final String urlPattern,
      final String value
  )
  {
    wireMockServer.loadMappingsUsing
        (
            stubMappings ->
            {
              final RequestPattern requestPattern
                  = newRequestPattern( POST, urlPathEqualTo( urlPattern ) )
                  .withRequestBody( containing( value ) )
                  .build();

              final ResponseDefinition responseDef
                  = responseDefinition()
                  .withBody( contents )
                  .build();

              final StubMapping proxyBasedMapping = new StubMapping( requestPattern, responseDef );
              proxyBasedMapping.setPriority( 1 ); // Make it low priority so that existing stubs will take precedence
              stubMappings.addMapping( proxyBasedMapping );
            }
        );
  }
}
