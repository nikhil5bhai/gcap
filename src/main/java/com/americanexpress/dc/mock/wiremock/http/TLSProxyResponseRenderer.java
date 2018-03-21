package com.americanexpress.dc.mock.wiremock.http;


import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.google.common.collect.ImmutableList;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static com.github.tomakehurst.wiremock.http.Response.response;


/** @author Richard Wilson */
public class TLSProxyResponseRenderer extends ProxyResponseRenderer
{

  private static final int MINUTES = 1000 * 60;
  private static final String TRANSFER_ENCODING = "transfer-encoding";
  private static final String CONTENT_LENGTH = "content-length";
  private static final String HOST_HEADER = "host";

  private final HttpClient client;
  private final boolean preserveHostHeader;
  private final String hostHeaderValue;

  public TLSProxyResponseRenderer( ProxySettings proxySettings, KeyStoreSettings trustStoreSettings, boolean preserveHostHeader, String hostHeaderValue )
  {
    client = TLSHttpClientFactory.createClient( 1000, 5 * MINUTES, proxySettings, trustStoreSettings );

    this.preserveHostHeader = preserveHostHeader;
    this.hostHeaderValue = hostHeaderValue;
  }

  public TLSProxyResponseRenderer()
  {
    this( ProxySettings.NO_PROXY, KeyStoreSettings.NO_STORE, false, null );
  }

  @Override
  public Response render( ResponseDefinition responseDefinition )
  {
    HttpUriRequest httpRequest = getHttpRequestFor( responseDefinition );
    addRequestHeaders( httpRequest, responseDefinition );

    try
    {
      addBodyIfPostPutOrPatch( httpRequest, responseDefinition );
      HttpResponse httpResponse = client.execute( httpRequest );

      return response()
          .status( httpResponse.getStatusLine().getStatusCode() )
          .headers( headersFrom( httpResponse, responseDefinition ) )
          .body( getEntityAsByteArrayAndCloseStream( httpResponse ) )
          .fromProxy( true )
          .build();
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }

  private HttpHeaders headersFrom( HttpResponse httpResponse, ResponseDefinition responseDefinition )
  {
    List<HttpHeader> httpHeaders = new LinkedList<HttpHeader>();
    for( Header header : httpResponse.getAllHeaders() )
    {
      httpHeaders.add( new HttpHeader( header.getName(), header.getValue() ) );
    }

    if( responseDefinition.getHeaders() != null )
    {
      httpHeaders.addAll( responseDefinition.getHeaders().all() );
    }

    return new HttpHeaders( httpHeaders );
  }

  public static HttpUriRequest getHttpRequestFor( ResponseDefinition response )
  {
    final RequestMethod method = response.getOriginalRequest().getMethod();
    final String url = response.getProxyUrl();
    return TLSHttpClientFactory.getHttpRequestFor( method, url );
  }

  private void addRequestHeaders( HttpRequest httpRequest, ResponseDefinition response )
  {
    Request originalRequest = response.getOriginalRequest();
    for( String key : originalRequest.getAllHeaderKeys() )
    {
      if( headerShouldBeTransferred( key ) )
      {
        if( !HOST_HEADER.equalsIgnoreCase( key ) || preserveHostHeader )
        {
          List<String> values = originalRequest.header( key ).values();
          for( String value : values )
          {
            httpRequest.addHeader( key, value );
          }
        }
        else
        {
          if( hostHeaderValue != null )
          {
            httpRequest.addHeader( key, hostHeaderValue );
          }
          else if( response.getProxyBaseUrl() != null )
          {
            httpRequest.addHeader( key, URI.create( response.getProxyBaseUrl() ).getAuthority() );
          }
        }
      }
    }

    if( response.getAdditionalProxyRequestHeaders() != null )
    {
      for( String key : response.getAdditionalProxyRequestHeaders().keys() )
      {
        httpRequest.setHeader( key, response.getAdditionalProxyRequestHeaders().getHeader( key ).firstValue() );
      }
    }
  }

  private static boolean headerShouldBeTransferred( String key )
  {
    return !ImmutableList.of( CONTENT_LENGTH, TRANSFER_ENCODING, "connection" ).contains( key.toLowerCase() );
  }

  private static void addBodyIfPostPutOrPatch( HttpRequest httpRequest, ResponseDefinition response ) throws UnsupportedEncodingException
  {
    Request originalRequest = response.getOriginalRequest();
    if( originalRequest.getMethod().isOneOf( PUT, POST, PATCH ) )
    {
      HttpEntityEnclosingRequest requestWithEntity = (HttpEntityEnclosingRequest)httpRequest;
      requestWithEntity.setEntity( buildEntityFrom( originalRequest ) );
    }
  }

  private static HttpEntity buildEntityFrom( Request originalRequest )
  {
    ContentTypeHeader contentTypeHeader = originalRequest.contentTypeHeader().or( "text/plain" );
    ContentType contentType = ContentType.create( contentTypeHeader.mimeTypePart(), contentTypeHeader.encodingPart().or( "utf-8" ) );

    if( originalRequest.containsHeader( TRANSFER_ENCODING ) &&
        originalRequest.header( TRANSFER_ENCODING ).firstValue().equals( "chunked" ) )
    {
      return new InputStreamEntity( new ByteArrayInputStream( originalRequest.getBody() ), -1, contentType );
    }

    return new ByteArrayEntity( originalRequest.getBody() );
  }
}
