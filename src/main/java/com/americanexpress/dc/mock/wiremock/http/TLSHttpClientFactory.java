package com.americanexpress.dc.mock.wiremock.http;


import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.http.GenericHttpUriRequest;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.KeyStoreSettings.NO_STORE;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;



@SuppressWarnings({ "deprecation" })
public class TLSHttpClientFactory
{

  public static final int DEFAULT_MAX_CONNECTIONS = 50;

  public static CloseableHttpClient createClient
      ( int maxConnections, int timeoutMilliseconds, ProxySettings proxySettings, KeyStoreSettings trustStoreSettings )
  {

    HttpClientBuilder builder
        = HttpClientBuilder
        .create()
        .disableAuthCaching()
        .disableAutomaticRetries()
        .disableCookieManagement()
        .disableRedirectHandling()
        .disableContentCompression()
        .setMaxConnTotal( maxConnections )
        .setDefaultSocketConfig( SocketConfig.custom().setSoTimeout( timeoutMilliseconds ).build() )
        .setSSLSocketFactory( buildTLSConnectionSocketFactory() )
        .useSystemProperties()
        .setHostnameVerifier( new AllowAllHostnameVerifier() );

    if( proxySettings != NO_PROXY )
    {
      HttpHost proxyHost = new HttpHost( proxySettings.host(), proxySettings.port() );
      builder.setProxy( proxyHost );
    }

    if( trustStoreSettings != NO_STORE )
    {
      builder.setSslcontext( buildSSLContextWithTrustStore( trustStoreSettings ) );
    }
    else
    {
      builder.setSslcontext( buildAllowAnythingSSLContext() );
    }

    return builder.build();
  }

  private static SSLConnectionSocketFactory buildTLSConnectionSocketFactory()
  {
    try
    {
      return new SSLConnectionSocketFactory
          (
              buildTLSContext(),
              new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" },
              null,
              new AllowAllHostnameVerifier()
          );
    }
    catch( Exception e )
    {
      return throwUnchecked( e, SSLConnectionSocketFactory.class );
    }
  }

  private static SSLContext buildTLSContext()
  {
    try
    {
      return SSLContexts
          .custom()
          .useTLS()
          .build();
    }
    catch( Exception e )
    {
      return throwUnchecked( e, SSLContext.class );
    }
  }

  private static SSLContext buildSSLContextWithTrustStore( KeyStoreSettings trustStoreSettings )
  {
    try
    {
      KeyStore trustStore = trustStoreSettings.loadStore();
      return SSLContexts
          .custom()
          .loadTrustMaterial( null, new TrustSelfSignedStrategy() )
          .loadKeyMaterial( trustStore, trustStoreSettings.password().toCharArray() )
          .useTLS()
          .build();
    }
    catch( Exception e )
    {
      return throwUnchecked( e, SSLContext.class );
    }
  }

  private static SSLContext buildAllowAnythingSSLContext()
  {
    try
    {
      return SSLContexts.custom().loadTrustMaterial( null, new TrustStrategy()
      {
        @Override
        public boolean isTrusted( X509Certificate[] chain, String authType ) throws CertificateException
        {
          return true;
        }
      } ).build();
    }
    catch( Exception e )
    {
      return throwUnchecked( e, SSLContext.class );
    }
  }

  public static CloseableHttpClient createClient( int maxConnections, int timeoutMilliseconds )
  {
    return createClient( maxConnections, timeoutMilliseconds, NO_PROXY, NO_STORE );
  }

  public static CloseableHttpClient createClient( int timeoutMilliseconds )
  {
    return createClient( DEFAULT_MAX_CONNECTIONS, timeoutMilliseconds );
  }

  public static CloseableHttpClient createClient()
  {
    return createClient( 30000 );
  }

  public static HttpUriRequest getHttpRequestFor( RequestMethod method, String url )
  {
    notifier().info( "Proxying: " + method + " " + url );

    if( method.equals( GET ) )
    {
      return new HttpGet( url );
    }
    else if( method.equals( POST ) )
    {
      return new HttpPost( url );
    }
    else if( method.equals( PUT ) )
    {
      return new HttpPut( url );
    }
    else if( method.equals( DELETE ) )
    {
      return new HttpDelete( url );
    }
    else if( method.equals( HEAD ) )
    {
      return new HttpHead( url );
    }
    else if( method.equals( OPTIONS ) )
    {
      return new HttpOptions( url );
    }
    else if( method.equals( TRACE ) )
    {
      return new HttpTrace( url );
    }
    else if( method.equals( PATCH ) )
    {
      return new HttpPatch( url );
    }
    else
    {
      return new GenericHttpUriRequest( method.toString(), url );
    }
  }
}

