/*
* -------------------------------------------------------------------------
*
* (C) Copyright / American Express, Inc. All rights reserved.
* The contents of this file represent American Express trade secrets and
* are confidential. Use outside of American Express is prohibited and in
* violation of copyright law.
*
* -------------------------------------------------------------------------
*/
package com.americanexpress.dc.mock;


import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * @author Utility class that can be used to invoke any REST service endpoint in a generic fashion
 */
public class RestServiceInvoker
{

  /**
   * Invoke a get on a REST endpoint after setting headers and parameters
   *
   * @param url
   *     - Rest endpoint
   * @param pathParams
   * @param queryParams
   *     - Query parameters to be passed
   * @param headers
   *     - Header parameters to be passed
   *
   * @return - Response
   */
  public Response get( String url, Map<String, String> pathParams, Map<String, String> queryParams, Map<String, Object> headers )
  {
    ResteasyClient client;
    ResteasyWebTarget target = null;
    client = new ResteasyClientBuilder()
        .register( ClientLoggingFilter.class )
        .build();
    MultivaluedMap<String, Object> multivaluedMap = null;

    url = substitutePathParamsInUrl( url, pathParams );

    //Adding the Query params if exists.
    if( queryParams != null && queryParams.size() > 0 )
    {
      multivaluedMap = new MultivaluedHashMap<>();
      multivaluedMap = addQueryParamsToTarget( queryParams, multivaluedMap );
      target = client.target( url ).queryParams( multivaluedMap );
    }
    else
    {
      target = client.target( url );
    }


    Builder requestBuilder = target.request( APPLICATION_JSON );
    if( headers != null )
    {
      addHeadersToBuilder( headers, requestBuilder );
    }

    long currTime = currentTimeMillis();
    Response response = requestBuilder.get();
    System.out.println( "Time taken to execute Rest Backend call (): >> " + (currentTimeMillis() - currTime) );
    return response;
  }

  private void addHeadersToBuilder( Map<String, Object> headers, Builder requestBuilder )
  {
    for( Map.Entry<String, Object> entry : headers.entrySet() )
    {
      System.out.println( "adding header Key : " + entry.getKey() + " Value : " + entry.getValue() );
      requestBuilder.header( entry.getKey(), entry.getValue() );
    }
  }

  private MultivaluedMap<String, Object> addQueryParamsToTarget( Map<String, String> queryParams, MultivaluedMap<String, Object> multivaluedMap )
  {
    for( Map.Entry<String, String> entry : queryParams.entrySet() )
    {
      System.out.println( "adding query param Key : " + entry.getKey() + " Value : " + entry.getValue() );
      //target.queryParam(entry.getKey(), entry.getValue());
      multivaluedMap.putSingle( entry.getKey(), entry.getValue() );
    }
    return multivaluedMap;
  }

  private String substitutePathParamsInUrl( String urlTemplate, Map<String, String> pathParams )
  {
    String substitutedUrl = urlTemplate;
    if( pathParams == null )
    {
      return urlTemplate;
    }

    System.out.println( "substitutedUrl:" + substitutedUrl );
    for( Map.Entry<String, String> entry : pathParams.entrySet() )
    {
      System.out.println( entry.getKey() + "|" + entry.getValue() );
      if( entry.getKey().equalsIgnoreCase( "account_token" ) )
      {
        substitutedUrl = substitutedUrl.replace( "{" + entry.getKey() + "}", entry.getValue() );
      }
      else
      {
        substitutedUrl = substitutedUrl.concat( "/" + entry.getValue() );
      }
    }

    System.out.println( "final url:" + substitutedUrl );
    return substitutedUrl;
  }

  /**
   * Invoke a get on a REST endpoint after setting headers and parameters
   *
   * @param url
   *     - Rest endpoint
   * @param pathParams
   * @param queryParams
   *     - Query parameters to be passed
   * @param headers
   *     - Header parameters to be passed
   *
   * @return - Response
   */
  public Response post( String url, Map<String, String> pathParams, Map<String, String> queryParams, Map<String, Object> headers, String request )
  {
    ResteasyClient client = new ResteasyClientBuilder()
        .register( ClientLoggingFilter.class )
        .build();
    ResteasyWebTarget target = client.target( url );
    Builder requestBuilder = target.request( APPLICATION_JSON );
    if( headers != null )
    {
      addHeadersToBuilder( headers, requestBuilder );
    }
    Response response = requestBuilder.post( Entity.json( request ) );
    return response;
  }

  /**
   * Invoke an HTTP POST on a REST endpoint after setting headers and body entity
   *
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param bodyEntity
   *     the HTTP request body entity
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response post
  (
      final URI uri,
      final Map<String, String> headers,
      Entity<T> bodyEntity
  )
  {
    return getInvocationBuilder( uri, headers ).post( bodyEntity );
  }

  /**
   * Invoke an HTTP PUT on a REST endpoint after setting headers and body entity
   *
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param bodyEntity
   *     the HTTP request body entity
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response put
  (
      final URI uri,
      final Map<String, String> headers,
      Entity<T> bodyEntity
  )
  {
    return getInvocationBuilder( uri, headers ).put( bodyEntity );
  }

  /**
   * Invoke an HTTP method on a REST end point after setting
   * headers and body entity.
   *
   * @param name
   *     the HTTP method name
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param bodyEntity
   *     the HTTP request body entity
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response method
  (
      final String name,
      final URI uri,
      final Map<String, String> headers,
      final Entity<T> bodyEntity
  )
  {
    return getInvocationBuilder( uri, headers ).method( name, bodyEntity );
  }

  /**
   * Invoke an HTTP method on a REST end point after setting
   * headers and body entity.
   *
   * @param name
   *     the HTTP method name
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response method
  (
      final String name,
      final URI uri,
      final Map<String, String> headers
  )
  {
    return getInvocationBuilder( uri, headers ).method( name );
  }

  /**
   * Invoke an HTTP DELETE on a REST endpoint after setting headers
   *
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response delete
  (
      final URI uri,
      final Map<String, String> headers
  )
  {
    return getInvocationBuilder( uri, headers ).delete();
  }

  /**
   * Invoke an HTTP GET on a REST endpoint after setting headers
   *
   * @param uri
   *     the URI endpoint
   * @param headers
   *     the HTTP headers
   * @param <T>
   *     the type of the HTTP request body entity
   *
   * @return the response of the HTTP call
   */
  public <T> Response get
  (
      final URI uri,
      final Map<String, String> headers
  )
  {
    return getInvocationBuilder( uri, headers ).get();
  }

  private Builder getInvocationBuilder
      (
          final URI uri,
          final Map<String, String> headers
      )
  {
    final ResteasyClient client = new ResteasyClientBuilder()
        .register( ClientLoggingFilter.class )
        .build();
    final ResteasyWebTarget target = client.target( uri );
    final Builder invocationBuilder = target.request( APPLICATION_JSON );
    if( headers != null )
    {
      invocationBuilder.headers( new MultivaluedHashMap<>( headers ) );
    }
    return invocationBuilder;
  }
}
