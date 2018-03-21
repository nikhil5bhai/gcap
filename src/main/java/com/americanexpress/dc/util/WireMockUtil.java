package com.americanexpress.dc.util;


import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.mock.transformer.MongoResponseLabeler;
import com.americanexpress.dc.mock.transformer.MongoResponseUploader;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.collections4.ListUtils;
import org.bson.Document;

import java.util.*;
import java.util.Map.Entry;

import static com.americanexpress.dc.mock.CollectionUploadUtil.mapPropertiesByParent;
import static com.americanexpress.dc.mock.service.spi.RequestStubBuilderService.getRequestPattern;
import static com.americanexpress.dc.util.Constants.*;
import static com.americanexpress.dc.util.UUIDUtil.toUUID;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.forCustomMatcher;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.*;


public class WireMockUtil
{

  //private static final File FILE_SOURCE_ROOT = new File( "bin" );

  /**
   * {@code Method wireRestMockData used to get REST mock response and also updates the response}
   *
   * @param wireMockServer
   *     the WireMock Server instance
   * @param document
   *     the document containing response data to be wired
   * @param urlPattern
   *     the URL Pattern
   * @param user
   *     the User bean
   * @param serviceCallName
   *     the Service Call name
   * @param responseId
   *     the response id for MongoDB document
   * @param resource
   *     the document containing resource data to be wired
   */
  public void wireRESTMockData
  (
      final WireMockServer wireMockServer,
      final Document document,
      final String urlPattern,
      final UserAccountsBean user,
      final String serviceCallName,
      final String responseId,
      final Document resource
  )
  {
    //ensureFileSourceRootDirectory();

    final String collectionName = substringBeforeLast( serviceCallName, "_" );

    final Map<String, Object> context = new LinkedHashMap<>();
    context.put( USER_KEY, user );
    context.put( RESOURCE_KEY, resource );

    final RequestPatternBuilder requestPatternBuilder
        = getRequestPattern( collectionName, context );

    wireMockData
        (
            wireMockServer,
            serviceCallName,
            collectionName,
            responseId,
            document,
            requestPatternBuilder
        );
  }

  private static UrlPattern createWildcardUrl
      (
          final RequestPattern requestPattern,
          final String defaultUrlPattern
      )
  {
    final Optional<RequestPattern> optionalRequestPattern = ofNullable( requestPattern );

    final UrlPattern urlMatcher
        = optionalRequestPattern
        .map( RequestPattern::getUrlMatcher )
        .orElseGet( () -> urlPathMatching( appendWildcard( defaultUrlPattern ) ) );

    if( !urlMatcher.isSpecified() )
    {
      return urlMatcher;
    }

    final String url
        = optionalRequestPattern
        .map( RequestPattern::getUrl )
        .map( WireMockUtil::appendWildcard )
        .orElse( null );

    final String urlPattern
        = optionalRequestPattern
        .map( RequestPattern::getUrlPattern )
        .map( WireMockUtil::appendWildcard )
        .orElse( null );

    final String urlPath
        = optionalRequestPattern
        .map( RequestPattern::getUrlPath )
        .map( WireMockUtil::appendWildcard )
        .orElse( null );

    final String urlPathPattern
        = optionalRequestPattern
        .map( RequestPattern::getUrlPathPattern )
        .map( WireMockUtil::appendWildcard )
        .orElse( null );

    return UrlPattern.fromOneOf( url, urlPattern, urlPath, urlPathPattern );
  }

  private static String appendWildcard( final String value )
  {
    return appendIfMissing( value, ".*" );
  }

  public static UrlPattern copyUrlMatcher( final RequestPattern requestPattern )
  {
    final Optional<RequestPattern> optionalRequestPattern = ofNullable( requestPattern );

    final String url
        = optionalRequestPattern
        .map( RequestPattern::getUrl )
        .orElse( null );

    final String urlPattern
        = optionalRequestPattern
        .map( RequestPattern::getUrlPattern )
        .orElse( null );

    final String urlPath
        = optionalRequestPattern
        .map( RequestPattern::getUrlPath )
        .orElse( null );

    final String urlPathPattern
        = optionalRequestPattern
        .map( RequestPattern::getUrlPathPattern )
        .orElse( null );

    return UrlPattern.fromOneOf( url, urlPattern, urlPath, urlPathPattern );
  }

  private void addHeaderIfAbsent
      (
          final RequestPatternBuilder patternBuilder,
          final String name,
          final String value,
          final String contents
      )
  {
    final RequestPattern requestPattern;
    if
        (
        patternBuilder != null
        &&
        (requestPattern = patternBuilder.build()) != null
        &&
        emptyIfNull( requestPattern.getHeaders() ).get( name ) == null
        )
    {
      if( value.contains( "," ) )
      {
        patternBuilder.withHeader( name, containing( "," ) );
      }
      else
      {
        patternBuilder.withHeader( name, containing( value ) );
      }
    }
  }

  private void addHeaderIfAbsent
      (
          final RequestPatternBuilder patternBuilder,
          final String name,
          final List<String> values,
          final String contents
      )
  {
    final RequestPattern requestPattern;
    if
        (
        patternBuilder != null
        &&
        (requestPattern = patternBuilder.build()) != null
        &&
        emptyIfNull( requestPattern.getHeaders() ).get( name ) == null
        )
    {
      for( final String value : values )
      {
        System.out.println( "values ::" + values );
        patternBuilder.withHeader( name, containing( "," ) );
        if( !contents.contains( value ) )
        {
          break;
        }
      }
    }
  }


  /**
   * {@code Method loadTokenizerWiremockMappingFiles call to mock the token service}
   *
   * @param wireMockServer
   *     the WireMock Server instance
   * @param contents
   *     the response body content to be wired
   * @param urlPattern
   *     the URL pattern
   * @param equalToJson
   *     the json to be equal to the request body
   */
  public void loadTokenizerWiremockMappingFiles
  (
      final WireMockServer wireMockServer,
      final String contents,
      final String urlPattern,
      final String equalToJson
  )
  {
    //ensureFileSourceRootDirectory();

    wireMockServer.loadMappingsUsing
        (
            stubMappings ->
            {
              final RequestPattern requestPattern
                  = newRequestPattern( POST, urlPathMatching( urlPattern ) )
                  .withRequestBody( equalToJson( equalToJson ) )
                  .build();

              final ResponseDefinition responseDefinition
                  = responseDefinition()
                  .withBody( contents )
                  .build();

              stubMappings.addMapping( new StubMapping( requestPattern, responseDefinition ) );
            }
        );
  }

  /**
   * {@code Method call to build or update the request pattern with configuration data.}
   *
   * @param requestPattern
   *     the WireMock request pattern
   * @param urlPattern
   *     the URL pattern
   * @param user
   *     the User bean
   * @param collectionName
   *     the collection name for MongoDB collection
   * @param contents
   */
  private RequestPatternBuilder buildRequestPattern
  (
      final RequestPattern requestPattern,
      final String urlPattern,
      final UserAccountsBean user,
      final String collectionName,
      final String contents
  )
  {
    final RequestPatternBuilder requestPatternBuilder
        = copyRequestPatternBuilder( requestPattern, urlPattern );

    final String publicGuid = user.getPublicGuid();
    final String accountToken = user.getAccountToken();
    final List<String> accountTokens = user.getAccountTokens();

    final String parentPrefix = collectionName + ".";

    final Properties queryProperties = App.ConfigProperties.getQueryProperties();
    final Map<String, String> queryParams1
        = mapPropertiesByParent( parentPrefix, queryProperties );
    for( final String queryName : queryParams1.keySet() )
    {
      if( queryName.equalsIgnoreCase( "status" ) )
      {
        final String type = substringAfter( collectionName, "_" );
        requestPatternBuilder.withQueryParam( "status", containing( type ) );
        requestPatternBuilder.withQueryParam( "offset", containing( "0" ) );
        requestPatternBuilder.withQueryParam( "limit", containing( "1000" ) );
      }
      else
      {
        final String queryValue = queryParams1.get( queryName );
        requestPatternBuilder.withQueryParam( queryName, containing( queryValue ) );
      }
    }

    final Properties headerProperties = App.ConfigProperties.getHeaderProperties();
    final Map<String, String> headerParams1
        = mapPropertiesByParent( parentPrefix, headerProperties );
    for( final String headerName : headerParams1.keySet() )
    {
      final String headerValue = headerParams1.get( headerName );
      requestPatternBuilder.withHeader( headerName, containing( headerValue ) );
    }

   /* if( !"payments".equalsIgnoreCase( collectionName ) && !"accounts_mapper".equalsIgnoreCase( collectionName ) && !"LoyaltyServiceWS_PendingPoints".equalsIgnoreCase( collectionName ) )*/
    if( !"payments".equalsIgnoreCase( collectionName ) && !"accounts_mapper".equalsIgnoreCase( collectionName ) )
    {
      if( !"LoyaltyServiceWS_PendingPoints".equalsIgnoreCase( collectionName ) && !"check_spending_power".equalsIgnoreCase( collectionName ) )
      {
        addHeaderIfAbsent( requestPatternBuilder, "security_token", publicGuid, contents );
      }
      else if( "LoyaltyServiceWS_PendingPoints".equalsIgnoreCase( collectionName ) )
      {
        addHeaderIfAbsent( requestPatternBuilder, "principal", publicGuid, contents );
      }
      if( !"member".equalsIgnoreCase( collectionName ) )
      {
        if( !"LoyaltyServiceWS_PendingPoints".equalsIgnoreCase( collectionName ) && !"check_spending_power".equalsIgnoreCase( collectionName ) )
        {
          if( null != accountTokens && accountTokens.size() > 0 )
          {
            addHeaderIfAbsent( requestPatternBuilder, "account_tokens", accountTokens, contents );
          }
          else
          {
            addHeaderIfAbsent( requestPatternBuilder, "account_tokens", accountToken, contents );
          }
        }
      }
    }
    if( !"member".equalsIgnoreCase( collectionName ) )
    {
      if( !"CreditStatusServiceWS".equalsIgnoreCase( collectionName ) )
      {
        if( null != accountTokens && accountTokens.size() > 0 )
        {
          addHeaderIfAbsent( requestPatternBuilder, "account_tokens", accountTokens, contents );
        }
        else
        {
          addHeaderIfAbsent( requestPatternBuilder, "account_tokens", accountToken, contents );
        }
      }
    }

    return requestPatternBuilder;
  }

  private RequestPatternBuilder copyRequestPatternBuilder
      (
          final RequestPattern requestPattern,
          final String urlPattern
      )
  {
    RequestPatternBuilder requestPatternBuilder;
    final CustomMatcherDefinition customMatcher;
    if( requestPattern.hasCustomMatcher() )
    {
      requestPatternBuilder = forCustomMatcher( requestPattern::match );
    }
    else if( (customMatcher = requestPattern.getCustomMatcher()) != null )
    {
      requestPatternBuilder
          = forCustomMatcher
          (
              customMatcher.getName(),
              customMatcher.getParameters()
          );
    }
    else
    {
      final RequestMethod requestMethod = requestPattern.getMethod();
      final UrlPattern urlMatcher
          = ofNullable( requestPattern.getUrlMatcher() )
          .orElseGet( () -> urlPathMatching( urlPattern ) );
      requestPatternBuilder = newRequestPattern( requestMethod, urlMatcher );

      final Map<String, MultiValuePattern> queryMap
          = emptyIfNull( requestPattern.getQueryParameters() );
      for( final Entry<String, MultiValuePattern> entry : queryMap.entrySet() )
      {
        final StringValuePattern valuePattern
            = ofNullable( entry.getValue() )
            .map( MultiValuePattern::getValuePattern )
            .orElse( null );
        requestPatternBuilder.withQueryParam( entry.getKey(), valuePattern );
      }

      final Map<String, MultiValuePattern> headersMap = emptyIfNull( requestPattern.getHeaders() );
      for( final Entry<String, MultiValuePattern> entry : headersMap.entrySet() )
      {
        final StringValuePattern valuePattern
            = ofNullable( entry.getValue() )
            .map( MultiValuePattern::getValuePattern )
            .orElse( null );
        requestPatternBuilder.withHeader( entry.getKey(), valuePattern );
      }

      final Map<String, StringValuePattern> cookieMap
          = emptyIfNull( requestPattern.getCookies() );
      for( final Entry<String, StringValuePattern> entry : cookieMap.entrySet() )
      {
        requestPatternBuilder.withCookie( entry.getKey(), entry.getValue() );
      }

      final BasicCredentials credentials = requestPattern.getBasicAuthCredentials();
      if( credentials != null )
      {
        requestPatternBuilder.withBasicAuth( credentials );
      }

      final List<StringValuePattern> bodyPatterns
          = ListUtils.emptyIfNull( requestPattern.getBodyPatterns() );
      for( final StringValuePattern bodyPattern : bodyPatterns )
      {
        requestPatternBuilder.withRequestBody( bodyPattern );
      }
    }
    return requestPatternBuilder;
  }

  public static RequestPatternBuilder createRequestPatternBuilder
      (
          final RequestPattern requestPattern,
          final UrlPattern urlMatcher
      )
  {
    RequestPatternBuilder requestPatternBuilder;
    final RequestMethod requestMethod = requestPattern.getMethod();
    requestPatternBuilder = newRequestPattern( requestMethod, urlMatcher );

    final Map<String, MultiValuePattern> queryMap
        = emptyIfNull( requestPattern.getQueryParameters() );
    for( final Entry<String, MultiValuePattern> entry : queryMap.entrySet() )
    {
      final StringValuePattern valuePattern
          = ofNullable( entry.getValue() )
          .map( MultiValuePattern::getValuePattern )
          .orElse( null );
      requestPatternBuilder.withQueryParam( entry.getKey(), valuePattern );
    }

    final Map<String, MultiValuePattern> headersMap = emptyIfNull( requestPattern.getHeaders() );
    for( final Entry<String, MultiValuePattern> entry : headersMap.entrySet() )
    {
      final StringValuePattern valuePattern
          = ofNullable( entry.getValue() )
          .map( MultiValuePattern::getValuePattern )
          .orElse( null );
      requestPatternBuilder.withHeader( entry.getKey(), valuePattern );
    }

    final Map<String, StringValuePattern> cookieMap
        = emptyIfNull( requestPattern.getCookies() );
    for( final Entry<String, StringValuePattern> entry : cookieMap.entrySet() )
    {
      requestPatternBuilder.withCookie( entry.getKey(), entry.getValue() );
    }

    final BasicCredentials credentials = requestPattern.getBasicAuthCredentials();
    if( credentials != null )
    {
      requestPatternBuilder.withBasicAuth( credentials );
    }

    final List<StringValuePattern> bodyPatterns
        = ListUtils.emptyIfNull( requestPattern.getBodyPatterns() );
    for( final StringValuePattern bodyPattern : bodyPatterns )
    {
      requestPatternBuilder.withRequestBody( bodyPattern );
    }
    return requestPatternBuilder;
  }

  /**
   * {@code Method wireSoapMockData to load the Soap responses}
   *
   * @param wireMockServer
   *     the WireMock Server instance
   * @param document
   *     the document containing response data to be wired
   * @param urlPattern
   *     the URL Pattern
   * @param user
   *     the User bean
   * @param serviceCallName
   *     the Service Call name
   * @param responseId
   *     the response id for MongoDB document
   * @param resource
   *     the document containing resource data to be wired
   */
  public void wireSOAPMockData
  (
      final WireMockServer wireMockServer,
      final Document document,
      final String urlPattern,
      final UserAccountsBean user,
      final String serviceCallName,
      final String responseId,
      final Document resource
  )
  {
    //ensureFileSourceRootDirectory();

    final String collectionName = substringBeforeLast( serviceCallName, "_" );

    final Map<String, Object> context = new LinkedHashMap<>();
    context.put( USER_KEY, user );
    context.put( RESOURCE_KEY, resource );

    final RequestPatternBuilder requestPatternBuilder
        = getRequestPattern( collectionName, context );

    wireMockData
        (
            wireMockServer,
            serviceCallName,
            collectionName,
            responseId,
            document,
            requestPatternBuilder
        );
  }

  /**
   * {@code Method wireMockData to wire/load the response body}
   *
   * @param wireMockServer
   *     the WireMock Server instance
   * @param serviceCallName
   *     the Service Call name
   * @param collectionName
   *     the collection name for MongoDB collection
   * @param responseId
   *     the response id for MongoDB document
   * @param document
   *     the document containing response data to be wired
   * @param requestPatternBuilder
   *     the request pattern to be wired
   */
  private void wireMockData
  (
      final WireMockServer wireMockServer,
      final String serviceCallName,
      final String collectionName,
      final String responseId,
      final Document document,
      final RequestPatternBuilder requestPatternBuilder
  )
  {
    final String contentType;
    if( endsWith( serviceCallName, "_rest" ) && serviceCallName.contains( ("LoyaltyServiceWS_PendingPoints") ) )
    {
      contentType = TEXT_XML;
    }
    else if( endsWith( serviceCallName, "_rest" ) )
    {
      contentType = APPLICATION_JSON;
    }

    else if( endsWith( serviceCallName, "_soap" ) )
    {
      contentType = TEXT_XML;
    }
    else
    {
      contentType = APPLICATION_OCTET_STREAM;
    }

    final int status = document.getInteger( "http_status", 200 );
    final String contents = document.getString( "response" );

    final ResponseDefinitionBuilder responseBuilder
        = aResponse()
        .withStatus( status )
        .withHeader( MOCK_SERVICE_CALL_NAME, serviceCallName )
        .withHeader( MOCK_COLLECTION_NAME, collectionName )
        .withHeader( MOCK_RESPONSE_ID, responseId )
        .withHeader( CONTENT_TYPE, contentType );

    final ResponseDefinition preBodyResponseDefinition = responseBuilder.build();
    if( status != 204 && contents != null )
    {
      responseBuilder.withBody( contents );
    }
    final ResponseDefinition responseDefinition = responseBuilder.build();

    // Bind WireMock Request Pattern to MongoDB Response Document
    wireMockServer.loadMappingsUsing
        (
            stubMappings ->
            {
              requestPatternBuilder.withQueryParam( COLLECTION_NAME_QUERY_PARAM, absent() );
              requestPatternBuilder.withQueryParam( RESPONSE_ID_QUERY_PARAM, absent() );


              final ResponseDefinition responseDef
                  = ResponseDefinitionBuilder
                  .like( responseDefinition )
                  .withTransformers( MongoResponseLabeler.NAME )
                  .build();

              final StubMapping stubMapping = new StubMapping( requestPatternBuilder.build(), responseDef );
              stubMapping.setId( toUUID( responseId ) );
              stubMapping.setPriority( 1 );
              stubMappings.addMapping( stubMapping );
            }
        );

    // Bind WireMock Request Pattern to Upload Response into MongoDB
    wireMockServer.loadMappingsUsing
        (
            stubMappings ->
            {
              final RequestPattern uploadRequestPattern
                  = new RequestPatternBuilder( PUT, copyUrlMatcher( requestPatternBuilder.build() ) )
                  .withQueryParam( COLLECTION_NAME_QUERY_PARAM, equalTo( collectionName ) )
                  .withQueryParam( RESPONSE_ID_QUERY_PARAM, equalTo( responseId ) )
                  .build();

              final ResponseDefinition responseDef
                  = ResponseDefinitionBuilder
                  .like( preBodyResponseDefinition )
                  .withStatus( ACCEPTED.getStatusCode() )
                  .withTransformers( MongoResponseUploader.NAME )
                  .build();

              final StubMapping stubMapping = new StubMapping( uploadRequestPattern, responseDef );
              stubMapping.setPriority( 1 );
              stubMappings.addMapping( stubMapping );
            }
        );
  }
}
