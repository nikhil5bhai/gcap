package com.americanexpress.dc.mock;


import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.axp.myca.common.util.Logger.logError;
import static com.axp.myca.common.util.constants.Constants.SERVICE_NAME;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;


/** @author Richard Wilson */
@SuppressWarnings("all")
public class HttpExchangeLogging
{

  private static final Gson GSON
      = new GsonBuilder()
      .create();
  protected static final String REQUEST_KEY = "request";
  protected static final String RESPONSE_KEY = "response";
  protected static final String REQUEST_LINE_KEY = "requestLine";
  protected static final String STATUS_KEY = "status";
  protected static final String HEADERS_KEY = "headers";
  protected static final String BODY_KEY = "body";
  protected static final String SPACE = " ";
  protected static final String DASH = " - ";
  protected static final String END_OF_LINE_REGEX = "[\\r\\n]";

  protected String getContainerRequestAsString( final ContainerRequestContext requestContext )
  {
    final JsonObject logObject = new JsonObject();
    final JsonObject requestObject = new JsonObject();
    requestObject.addProperty( REQUEST_LINE_KEY, requestLine( requestContext ) );
    requestObject.add( HEADERS_KEY, headers( requestContext, ContainerRequestContext::getHeaders ) );
    requestObject.add( BODY_KEY, body( requestContext ) );
    logObject.add( REQUEST_KEY, requestObject );
    return logObject.toString();
  }

  public String getClientRequestAsString( final ClientRequestContext requestContext )
  {
    final JsonObject logObject = new JsonObject();
    final JsonObject requestObject = new JsonObject();
    requestObject.addProperty( REQUEST_LINE_KEY, requestLine( requestContext ) );
    requestObject.add( HEADERS_KEY, headers( requestContext, ClientRequestContext::getHeaders ) );
    requestObject.add( BODY_KEY, body( requestContext ) );
    logObject.add( REQUEST_KEY, requestObject );
    return logObject.toString();
  }

  private String requestLine( final ContainerRequestContext requestContext )
  {
    return ofNullable( requestContext )
        .map( context -> context.getMethod() + SPACE + context.getUriInfo().getRequestUri() )
        .orElse( EMPTY );
  }

  private String requestLine( final ClientRequestContext requestContext )
  {
    return ofNullable( requestContext )
        .map( context -> context.getMethod() + SPACE + context.getUri() )
        .orElse( EMPTY );
  }

  private JsonElement body( final ContainerRequestContext requestContext )
  {
    return ofNullable( requestContext )
        .filter( ContainerRequestContext::hasEntity )
        .map( ContainerRequestContext::getEntityStream )
        .map( toBody( requestContext, ContainerRequestContext::setEntityStream ) )
        .map( toBodyElement( requestContext, ContainerRequestContext::getMediaType ) )
        .orElse( JsonNull.INSTANCE );
  }

  private JsonElement body( final ClientRequestContext requestContext )
  {
    return ofNullable( requestContext )
        .filter( ClientRequestContext::hasEntity )
        .map( ClientRequestContext::getEntity )
        .map( entity -> getEntityAsString( entity ) )
        .map( toBodyElement( requestContext, ClientRequestContext::getMediaType ) )
        .orElse( JsonNull.INSTANCE );
  }

  private String getEntityAsString( final Object entityObject )
  {
    if( entityObject != null )
    {
      final Object entity = extractNestedEntity( entityObject );
      if( entity instanceof InputStream )
      {
        try
        {
          return IOUtils.toString( (InputStream)entity );
        }
        catch( final IOException rethrow )
        {
          throw new IllegalStateException( rethrow );
        }
      }
      else if( entity instanceof Form )
      {
        return Optional
            .of( (Form)entity )
            .map( Form::asMap )
            .map( Object::toString )
            .orElse( null );
      }
      else if( entity != null )
      {
        return entity.toString();
      }
    }
    return null;
  }

  private Object extractNestedEntity( final Object entity )
  {
    if( entity instanceof Entity )
    {
      return ((Entity<?>)entity).getEntity();
    }
    return entity;
  }

  protected String getContainerResponseAsString( final ContainerResponseContext responseContext )
  {
    final JsonObject logObject = new JsonObject();
    final JsonObject responseObject = new JsonObject();
    responseObject.addProperty
        (
            STATUS_KEY,
            stateLine( responseContext, ContainerResponseContext::getStatusInfo )
        );
    responseObject.add
        (
            HEADERS_KEY,
            headers( responseContext, ContainerResponseContext::getHeaders )
        );
    responseObject.add( BODY_KEY, body( responseContext ) );
    logObject.add( RESPONSE_KEY, responseObject );
    return logObject.toString();
  }

  public String getClientResponseAsString( final ClientResponseContext responseContext )
  {
    final JsonObject logObject = new JsonObject();
    final JsonObject responseObject = new JsonObject();
    responseObject.addProperty
        (
            STATUS_KEY,
            stateLine( responseContext, ClientResponseContext::getStatusInfo )
        );
    responseObject.add
        (
            HEADERS_KEY,
            headers( responseContext, ClientResponseContext::getHeaders )
        );
    responseObject.add( BODY_KEY, body( responseContext ) );
    logObject.add( RESPONSE_KEY, responseObject );
    return logObject.toString();
  }

  private <T> String stateLine
      (
          final T responseContext,
          final Function<T, Response.StatusType> toStatusInfo
      )
  {
    requireNonNull( toStatusInfo );
    return ofNullable( responseContext )
        .map( toStatusInfo )
        .map( statusInfo -> statusInfo.getStatusCode() + DASH + statusInfo.getReasonPhrase() )
        .orElse( EMPTY );
  }

  private <T> JsonElement headers
      (
          final T context,
          final Function<T, MultivaluedMap<String, ?>> toHeaders
      )
  {
    return ofNullable( context )
        .map( toHeaders )
        .map( GSON::toJsonTree )
        .orElseGet( JsonObject::new );
  }

  private JsonElement body( final ContainerResponseContext responseContext )
  {
    return ofNullable( responseContext )
        .filter( ContainerResponseContext::hasEntity )
        .map( ContainerResponseContext::getEntity )
        .map( Object::toString )
        .map( toBodyElement( responseContext, ContainerResponseContext::getMediaType ) )
        .orElse( JsonNull.INSTANCE );
  }

  private JsonElement body( final ClientResponseContext responseContext )
  {
    return ofNullable( responseContext )
        .filter( ClientResponseContext::hasEntity )
        .map( ClientResponseContext::getEntityStream )
        .map( toBody( responseContext, ClientResponseContext::setEntityStream ) )
        .map( toBodyElement( responseContext, ClientResponseContext::getMediaType ) )
        .orElse( JsonNull.INSTANCE );
  }

  private <T> Function<InputStream, String> toBody
      (
          final T context,
          final BiConsumer<T, InputStream> entityStreamSetter
      )
  {
    requireNonNull( entityStreamSetter );
    return stream ->
    {
      final ByteArrayOutputStream output = new ByteArrayOutputStream();
      try( final InputStream input = stream; )
      {
        copy( input, output );
        final byte[] byteArray = output.toByteArray();
        entityStreamSetter.accept
            (
                context,
                new ByteArrayInputStream( byteArray )
            );
        return new String( byteArray );
      }
      catch( final Exception log )
      {
        logError
            (
                SERVICE_NAME,
                this,
                "toBody",
                log.getMessage(),
                log
            );
      }
      return null;
    };
  }

  private <T> Function<String, JsonElement> toBodyElement
      (
          final T context,
          final Function<T, MediaType> toMediaType
      )
  {
    requireNonNull( toMediaType );
    return body ->
        ofNullable( context )
            .map( toMediaType )
            .filter( APPLICATION_JSON_TYPE::isCompatible )
            .map( mediaType -> toJsonElement( body ) )
            .orElseGet( () -> toStringElement( body ) );
  }

  private JsonElement toJsonElement( final String body )
  {
    try
    {
      return GSON.fromJson( body, JsonElement.class );
    }
    catch( final Exception log )
    {
      logError
          (
              SERVICE_NAME,
              this,
              "toJsonElement",
              log.getMessage(),
              log
          );
    }
    return toStringElement( body );
  }

  private JsonElement toStringElement( final String body )
  {
    return new JsonPrimitive( defaultString( body, EMPTY ).replaceAll( END_OF_LINE_REGEX, SPACE ) );
  }
}
