package com.americanexpress.dc.mock.transformer;


import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.*;
import org.apache.commons.lang3.StringUtils;

import static com.americanexpress.dc.util.Constants.*;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.UriBuilder.fromUri;


/** @author Richard Wilson */
public abstract class AbstractMongoResponseTransformer extends ResponseDefinitionTransformer
{

  @Override
  public ResponseDefinition transform
      (
          final Request request,
          final ResponseDefinition responseDefinition,
          final FileSource files,
          final Parameters parameters
      )
  {
    final ResponseDefinition existingResponseDefinition
        = ofNullable( responseDefinition )
        .orElseGet( ResponseDefinition::new );

    final HttpHeaders headers
        = ofNullable( existingResponseDefinition.getHeaders() )
        .orElseGet( HttpHeaders::new );

    final String collectionName
        = headers
        .getHeader( MOCK_COLLECTION_NAME )
        .firstValue();

    final String responseId
        = headers
        .getHeader( MOCK_RESPONSE_ID )
        .firstValue();

    final String finalCollectionName
        = ofNullable( request )
        .map( r -> r.queryParameter( "status" ) )
        .filter( MultiValue::isSingleValued )
        .map( MultiValue::firstValue )
        .filter( StringUtils::isNotBlank )
        .map( v -> "transactions_" + v )
        .orElse( collectionName );

    final String uploadResponseUri
        = fromUri( request.getAbsoluteUrl() )
        .queryParam( COLLECTION_NAME_QUERY_PARAM, finalCollectionName )
        .queryParam( RESPONSE_ID_QUERY_PARAM, responseId )
        .build()
        .toString();

    final ResponseDefinitionBuilder responseBuilder
        = ResponseDefinitionBuilder
        .like( existingResponseDefinition )
        .but()
        .withHeader( MOCK_DATA_UPLOAD_URL, uploadResponseUri );

    ensureContentTypeHeader( headers, responseBuilder );

    return updateResponse( responseId, finalCollectionName, request, responseBuilder ).build();
  }

  ResponseDefinitionBuilder ensureContentTypeHeader
      (
          final HttpHeaders existingHeaders,
          final ResponseDefinitionBuilder responseBuilder
      )
  {
    final ContentTypeHeader contentTypeHeader
        = existingHeaders
        .getContentTypeHeader();

    if( !contentTypeHeader.isPresent() )
    {
      final String contentType
          = contentTypeHeader
          .or( WILDCARD )
          .firstValue();

      responseBuilder.withHeader( CONTENT_TYPE, contentType );
    }
    return responseBuilder;
  }

  ResponseDefinitionBuilder updateResponse
      (
          final String responseId,
          final String collectionName,
          final Request request,
          final ResponseDefinitionBuilder responseBuilder
      )
  {
    return responseBuilder;
  }
}
