package com.americanexpress.dc.mock.transformer;


import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.collections4.ListUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.americanexpress.dc.config.MongoConfiguration.getDatabase;
import static com.americanexpress.dc.util.NamingUtil.kebabCase;
import static com.americanexpress.dc.util.UUIDUtil.toUUID;
import static com.americanexpress.dc.util.WireMockUtil.copyUrlMatcher;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;


/** @author Richard Wilson */
public class MongoResponseUploader extends AbstractMongoResponseTransformer
{

  public static final String NAME = kebabCase( MongoResponseUploader.class.getSimpleName() );

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public boolean applyGlobally()
  {
    return false;
  }

  @Override
  ResponseDefinitionBuilder updateResponse
      (
          final String responseId,
          final String collectionName,
          final Request request,
          final ResponseDefinitionBuilder responseBuilder
      )
  {
    final String requestBody = request.getBodyAsString();

    if( requestBody != null )
    {
      final Bson criteria = eq( "_id", new ObjectId( responseId ) );

      getDatabase()
          .getCollection( collectionName )
          .updateOne( criteria, set( "response", requestBody ) );

      responseBuilder.withBody( requestBody );

      final URI requestUri = fromUri( request.getAbsoluteUrl() ).build();

      final int port = requestUri.getPort();

      configureFor( port );

      final StubMapping mapping = getSingleStubMapping( toUUID( responseId ) );

      final MappingBuilder remappingBuilder = getStubWithRequestBody( mapping, requestBody );

      editStub( remappingBuilder );
    }

    return responseBuilder;
  }

  private MappingBuilder getStubWithRequestBody
      (
          final StubMapping mapping,
          final String requestBody
      )
  {
    final RequestPattern requestPattern = mapping.getRequest();

    final String requestMethod = requestPattern.getMethod().getName();
    final UrlPattern urlPattern = copyUrlMatcher( requestPattern );
    final MappingBuilder remappingBuilder = request( requestMethod, urlPattern );

    final Integer priority = mapping.getPriority();
    if( priority != null )
    {
      remappingBuilder.atPriority( priority );
    }
    final UUID id = mapping.getId();
    if( id != null )
    {
      remappingBuilder.withId( id );
    }
    final String name = mapping.getName();
    if( name != null )
    {
      remappingBuilder.withName( name );
    }
    final String scenarioName = mapping.getScenarioName();
    if( scenarioName != null )
    {
      remappingBuilder.inScenario( scenarioName );
    }
    if( TRUE.equals( mapping.isPersistent( )) )
    {
      remappingBuilder.persistent();
    }

    final Map<String, MultiValuePattern> queryMap
        = emptyIfNull( requestPattern.getQueryParameters() );
    for( final Entry<String, MultiValuePattern> entry : queryMap.entrySet() )
    {
      final StringValuePattern valuePattern
          = ofNullable( entry.getValue() )
          .map( MultiValuePattern::getValuePattern )
          .orElse( null );
      remappingBuilder.withQueryParam( entry.getKey(), valuePattern );
    }

    final Map<String, MultiValuePattern> headersMap = emptyIfNull( requestPattern.getHeaders() );
    for( final Entry<String, MultiValuePattern> entry : headersMap.entrySet() )
    {
      final StringValuePattern valuePattern
          = ofNullable( entry.getValue() )
          .map( MultiValuePattern::getValuePattern )
          .orElse( null );
      remappingBuilder.withHeader( entry.getKey(), valuePattern );
    }

    final Map<String, StringValuePattern> cookieMap
        = emptyIfNull( requestPattern.getCookies() );
    for( final Entry<String, StringValuePattern> entry : cookieMap.entrySet() )
    {
      remappingBuilder.withCookie( entry.getKey(), entry.getValue() );
    }

    final BasicCredentials credentials = requestPattern.getBasicAuthCredentials();
    if( credentials != null )
    {
      final String username = credentials.username;
      final String password = credentials.password;
      remappingBuilder.withBasicAuth( username, password );
    }

    final List<StringValuePattern> bodyPatterns
        = ListUtils.emptyIfNull( requestPattern.getBodyPatterns() );
    for( final StringValuePattern bodyPattern : bodyPatterns )
    {
      remappingBuilder.withRequestBody( bodyPattern );
    }

    final Map<String, Parameters> postServeActions = emptyIfNull( mapping.getPostServeActions() );
    for( final Entry<String, Parameters> entry : postServeActions.entrySet() )
    {
      remappingBuilder.withPostServeAction( entry.getKey(), entry.getValue() );
    }

    final ResponseDefinitionBuilder updatedDefinitionBuilder
        = ResponseDefinitionBuilder
        .like( mapping.getResponse() )
        .withBody( requestBody );
    remappingBuilder.willReturn( updatedDefinitionBuilder );

    return remappingBuilder;
  }
}
