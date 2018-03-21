package com.americanexpress.dc.util;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static com.americanexpress.dc.util.NamingUtil.screamingSnakeCase;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.HttpMethod.GET;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.apache.commons.lang3.StringUtils.*;


/**
 * The {@code ApigeeUtil} class provides common utilities for building a MAC token header value
 * required to perform Apigee API service calls.
 * <p>
 * The essence of an Apigee authenticated and authorized service call is the MAC token header value
 * in the client request.  An Apigee client has to be registered and will possess a client
 * identifier and client secret.  The client identifier will be in the header values of the client
 * request.  The client secret is private and should remain that way; the primary function of the
 * secret is to sign secure hashes/encryptions of the client request body and the metadata of the
 * client request target or endpoint of the service call, i.e. this metadata is also known as the
 * base string.
 * <p>
 * Here's the Apigee MAC token format:
 * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
 *
 * Apigee MAC token attributes:
 * <ul>
 * <li>{@code id} - the Apigee access token or client identifier
 * <li>{@code ts} - the timestamp, as epoch time in seconds
 * <li>{@code nonce} - the cryptographic nonce for one time use
 * <li>{@code bodyhash} - the signed secure hash of request body
 * <li>{@code mac} - the signed secure hash of request metadata represented by a base string.
 * </ul>
 *
 * <h4>Apigee API Service Call Types</h4>
 * <ul>
 * <li>Application to Application (A2A) or {@code HMAC} type
 * - This type of service call method is typically used for internal client to internel server.
 * It involves setting up and executing a single client request to a given resource endpoint.
 * <li>Business to Business (B2B) or {@code OAuth} type
 * - This type of service call method is typically used for external client to externally exposed
 * server.  It involves setting up and executing two client requests, one for obtaining an access
 * token and a second to the given resource endpoint.
 * </ul>
 *
 * <h5>Apigee Application to Application (A2A) or {@code HMAC} Service Call</h5>
 *
 * Example of Call API/Resource Endpoint with Client Identifier
 * to Access Resource for Apigee {@code HMAC} method:
 * <pre>
 * final String clientId = ...
 * final String clientSecret = ...
 * final String accountNumber = "371277434601006";
 *
 * final {@link URI} uri
 *     = {@link UriBuilder}
 *     .fromUri( "https://in.api.dev.app.aexp.com" )
 *     .path( "/marketing/v1/registeredcard/cardmembers/offer-savings/history" )
 *     .build();
 *
 * final String body = getRequestBody( accountNumber );
 *
 * final String authorizationToken
 *     = {@link #getAuthorizationTokenWithDecodedPath(String, String, String, URI, String) getAuthorizationTokenWithDecodedPath}
 *     (
 *         clientId,
 *         clientSecret,
 *         {@link javax.ws.rs.HttpMethod#POST POST},
 *         uri,
 *         body
 *     );
 *
 * final {@link MultivaluedMap}<String, Object> headers = new {@link MultivaluedHashMap}<>();
 * headers.add( {@link HttpHeaders#CONTENT_TYPE CONTENT_TYPE}, {@link MediaType#APPLICATION_JSON APPLICATION_JSON} );
 * headers.add( {@link HttpHeaders#AUTHORIZATION AUTHORIZATION}, authorizationToken );
 * headers.add( {@link #X_AMEX_API_KEY_HEADER}, clientId );
 *
 * final {@link Response} response = {@link WebClient#post(URI, MultivaluedMap, Entity, String...) WebClient.post}( uri, headers, {@link Entity#json(Object) json}( body ) );
 *
 * final Gson gson
 *     = new GsonBuilder()
 *     .setPrettyPrinting()
 *     .setFieldNamingStrategy( LOWER_CASE_WITH_UNDERSCORES )
 *     .create();
 *
 * if( response.getStatus() == {@link java.net.HttpURLConnection#HTTP_OK HTTP_OK} )
 * {
 *   final String content = {@link WebClient#getContentAsString(Response) WebClient.getContentAsString}( response );
 *   final JsonElement savings = gson.fromJson( content, JsonElement.class );
 *   System.out.println( gson.toJson( savings ) );
 * }
 * </pre>
 * <p>
 * Resource Output:
 * <pre>
 * {
 *   "savingsdet": {
 *     "offrSavings": []
 *   },
 *   "totalSavings": "381.4",
 *   "version": "1.0",
 *   "correlationId": "1234",
 *   "respCd": "RCSAV000",
 *   "respDesc": "Recrod retreival successful"
 * }
 * </pre>
 * <p>
 * <h5>Apigee Business to Business (B2B) or {@code OAuth} Service Call</h5>
 * <p>
 * Example of Call Access Token Endpoint to
 * Obtain Access Token for Apigee {@code OAuth} method:
 * <pre>
 * final String clientId = ...
 * final String clientSecret = ...
 * final String grant_type = {@link GrantType#CLIENT_CREDENTIALS CLIENT_CREDENTIALS}.toString();
 *
 * final {@link URI} uri
 *     = {@link UriBuilder}
 *     .fromUri( "https://in.api.dev.app.aexp.com" )
 *     .path( "/apiplatform/v2/oauth/token/mac" )
 *     .build();
 *
 * final {@link Form} form
 *     = new {@link Form}()
 *     .param( GRANT_TYPE_HEADER, grant_type )
 *     .param( SCOPE_HEADER, "" );
 *
 * final String body = {@link #getFormURLEncodedBody(Form) getFormURLEncodedBody}( form );
 *
 * final long ts = {@link #epochTimeInSeconds}();
 * final String nonce = {@link #nonce(Object...) nonce}();
 * final String authenticationToken
 *     = {@link #buildMacToken(String, String, long, String, String, String) buildMacToken}
 *     (
 *         clientId,
 *         clientSecret,
 *         ts,
 *         nonce,
 *         {@link #getBodyHash(String, String) getBodyHash}( clientSecret, body ),
 *         {@link #baseString(Object...) baseString}
 *             (
 *                 clientId,
 *                 ts,
 *                 nonce,
 *                 grant_type
 *             )
 *     );
 *
 * final {@link MultivaluedMap}<String, Object> headers = new {@link MultivaluedHashMap}<>();
 * headers.add( {@link HttpHeaders#CONTENT_TYPE CONTENT_TYPE}, {@link MediaType#APPLICATION_FORM_URLENCODED APPLICATION_FORM_URLENCODED} );
 * headers.add( {@link #AUTHENTICATION}, authenticationToken );
 * headers.add( {@link #X_AMEX_API_KEY_HEADER}, clientId );
 *
 * final {@link Response} response = {@link WebClient#post(URI, MultivaluedMap, Entity, String...) WebClient.post}( uri, headers, {@link Entity#form(Form) form}( form ) );
 *
 * final Gson gson
 *     = new GsonBuilder()
 *     .setPrettyPrinting()
 *     .setFieldNamingStrategy( LOWER_CASE_WITH_UNDERSCORES )
 *     .create();
 *
 * if( response.getStatus() == {@link java.net.HttpURLConnection#HTTP_OK HTTP_OK} )
 * {
 *   final String content = {@link WebClient#getContentAsString(Response) WebClient.getContentAsString}( response );
 *   final JsonObject credentials = gson.fromJson( content, JsonObject.class );
 *   System.out.println( gson.toJson( credentials ) );
 * }
 * </pre>
 * <p>
 * Access Token Output:
 * <pre>
 * {
 *   "access_token": "97956353-5ee9-47cc-91ce-c3c09c5120a5",
 *   "token_type": "mac",
 *   "expires_in": 7200,
 *   "refresh_token": "cf68dac5-4ca1-4f1e-b2a6-3c401d9bcaaf",
 *   "scope": "Communications_CardMemberDisclosure_A2A_default",
 *   "mac_key": "4228c867-2c84-43f9-83a0-5d3c732f2693",
 *   "mac_algorithm": "hmac-sha-256"
 * }
 * </pre>
 * <p>
 * Example of Call API/Resource Endpoint with Access
 * Token to Access Resource for Apigee {@code OAuth} method:
 * <pre>
 * final String clientId = ...
 * final JsonObject credentials = ...
 * final String accountNumber = "371277434601006";
 *
 * final {@link URI} uri
 *     = {@link UriBuilder}
 *     .fromUri( "https://in.api.dev.app.aexp.com" )
 *     .path( "/communications/v1/disclosures/metadata" )
 *     .build();
 *
 * final String authorizationToken
 *     = {@link #getAuthorizationTokenWithEncodedPath(String, String, String, URI) getAuthorizationTokenWithEncodedPath}
 *     (
 *         credentials.getAsJsonPrimitive( {@link #APIGEE_ACCESS_TOKEN} ).getAsString(),
 *         credentials.getAsJsonPrimitive( {@link #APIGEE_MAC_KEY} ).getAsString(),
 *         {@link javax.ws.rs.HttpMethod#GET GET},
 *         uri
 *     );
 *
 * final {@link MultivaluedMap}<String, Object> headers = new {@link MultivaluedHashMap}<>();
 * headers.add( {@link HttpHeaders#CONTENT_TYPE CONTENT_TYPE}, {@link MediaType#APPLICATION_FORM_URLENCODED APPLICATION_FORM_URLENCODED} );
 * headers.add( {@link HttpHeaders#AUTHORIZATION AUTHORIZATION}, authorizationToken );
 * headers.add( {@link #X_AMEX_API_KEY_HEADER}, clientId );
 * headers.add( "ACCOUNT_NO", accountNumber );
 *
 * final {@link Response} response = {@link WebClient#get(URI, MultivaluedMap, String...) WebClient.get}( uri, headers );
 *
 * final Gson gson
 *     = new GsonBuilder()
 *     .setPrettyPrinting()
 *     .setFieldNamingStrategy( LOWER_CASE_WITH_UNDERSCORES )
 *     .create();
 *
 * if( response.getStatus() == {@link java.net.HttpURLConnection#HTTP_OK HTTP_OK} )
 * {
 *   final String content = {@link WebClient#getContentAsString(Response) WebClient.getContentAsString}( response );
 *   final JsonElement documents = gson.fromJson( content, JsonElement.class );
 *   System.out.println( gson.toJson( documents ) );
 * }
 * </pre>
 * <p>
 * Resource Output:
 * <pre>
 * [
 *   {
 *     "documentKey": "1369833799417495",
 *     "accountNo": "371277434601006",
 *     "documentDate": 1454973448899,
 *     "documentName": "MR_test_document_710816.pdf",
 *     "documentType": "MR",
 *     "documentFormat": "PDF"
 *   },
 *   {
 *     "documentKey": "3200079551629102",
 *     "accountNo": "371277434601006",
 *     "documentDate": 1454109452048,
 *     "documentName": "MR_test_document_758632.pdf",
 *     "documentType": "MR",
 *     "documentFormat": "PDF"
 *   },
 *   {
 *     "documentKey": "3930440201684889",
 *     "accountNo": "371277434601006",
 *     "documentDate": 1447888647399,
 *     "documentName": "MR_test_document_389775.pdf",
 *     "documentType": "MR",
 *     "documentFormat": "PDF"
 *   },
 *   {
 *     "documentKey": "5666829352638248",
 *     "accountNo": "371277434601006",
 *     "documentDate": 1447888644827,
 *     "documentName": "MR_test_document_976418.pdf",
 *     "documentType": "MR",
 *     "documentFormat": "PDF"
 *   }
 * ]
 * </pre>
 *
 * @author Richard Wilson
 */
@SuppressWarnings("all")
public final class ApigeeUtil
{

  public static final String AMEX_TAG = "AMEX";
  public static final String NONCE_DELIMITER = ":";
  public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
  public static final String MAC_TOKEN_FORMAT
      = "MAC id=\"%s\",ts=\"%s\",nonce=\"%s\",bodyhash=\"%s\",mac=\"%s\"";
  public static final String AUTHENTICATION = "Authentication";
  public static final String AUTHORIZATION = "Authorization";
  public static final String X_AMEX_API_KEY_HEADER = "X-AMEX-API-KEY";
  public static final String GRANT_TYPE_HEADER = "grant_type";
  public static final String SCOPE_HEADER = "scope";
  public static final String APIGEE_ACCESS_TOKEN = "access_token";
  public static final String APIGEE_MAC_KEY = "mac_key";
  public static final String APIGEE_EXPIRES_IN = "expires_in";
  public static final String APIGEE_TIME_TO_LIVE = "time_to_live";
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
  private static final int UNDEFINED_PORT = -1;
  public static final String BASE_STRING_ENDLINE = "\n";

  /**
   * Apigee Grant Type enum values
   */
  public enum GrantType
  {
    CLIENT_CREDENTIALS,
    PASSWORD,
    AUTHORIZATION_CODE;

    public static GrantType fromString( final String name )
    {
      return getEnum( GrantType.class, screamingSnakeCase( name ) );
    }

    @Override
    public String toString()
    {
      return super.name().toLowerCase();
    }
  }

  private ApigeeUtil()
  {
  }

  /**
   * Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
   * <p>
   * The epoch second count is a simple incrementing count of seconds where
   * second 0 is 1970-01-01T00:00:00Z.
   *
   * @return the seconds from the epoch of 1970-01-01T00:00:00Z
   */
  public static long epochTimeInSeconds()
  {
    return Instant.now().getEpochSecond();
  }

  /**
   * Creates a nonce, that is an arbitrary value that may only be used once, for
   * cryptography.
   * <p>
   * If the {@code suffixTag} argument is {@code null} then a random UUID is generated as the nonce.
   * <p>
   * If the {@code suffixTag} argument is <i>not</i> {@code null}
   * then a randomly generated UUID and {@code suffixTag} value are concatenated
   * together using a colon ({@code :}) into a single {@link String} nonce.
   *
   * @return a single {@link String} containing the randomly generated UUID and
   * {@code suffixTag} value separated by the colon ({@code :}) character
   */
  public static String suffixedNonce( final String suffixTag )
  {
    return nonce( UUID.randomUUID().toString(), suffixTag );
  }

  /**
   * Creates or Builds a nonce, that is an arbitrary value that may only be used once, for
   * cryptography.
   * <p>
   * If <b>no</b> arguments are passed to this method then a random UUID is generated as the nonce.
   * <p>
   * If one or more arguments are passed to this method then all arguments are concatenated
   * together using a colon ({@code :}) into a single {@link String} nonce.
   *
   * @return a single {@link String} nonce containing the provided
   * parts separated by the colon ({@code :}) character
   */
  public static String nonce( final Object... parts )
  {
    if( ArrayUtils.isEmpty( parts ) )
    {
      return UUID.randomUUID().toString();
    }
    return nonce( asList( parts ) );
  }

  /**
   * Creates or Builds a nonce, that is an arbitrary value that may only be used once, for
   * cryptography.
   * <p>
   * If {@code parts} collection is {@code null} or empty
   * then a random UUID is generated as the nonce.
   * <p>
   * If {@code parts} collection has one or more values
   * then all values are concatenated together using a
   * colon ({@code :}) into a single {@link String} nonce.
   *
   * @return a single {@link String} nonce containing the provided
   * parts separated by the colon ({@code :}) character
   */
  public static String nonce( final Collection<Object> parts )
  {
    if( CollectionUtils.isEmpty( parts ) )
    {
      return UUID.randomUUID().toString();
    }
    return join( parts.iterator(), NONCE_DELIMITER );
  }

  /**
   * Gets the specified port number of this {@link URI},
   * or the default port number of the protocol associated
   * with the {@link java.net.URL} of this {@link URI}.
   *
   * @param uri
   *     the {@link URI} to be used
   *
   * @return the specified port number, if not specified
   * the default port number of the protocol associated,
   * or else {@code null}
   */
  public static String getPort( final URI uri )
  {
    final int portNumber = uri.getPort();
    if( portNumber > UNDEFINED_PORT )
    {
      return String.valueOf( portNumber );
    }
    else
    {
      final int defaultPort = getDefaultPort( uri );
      if( defaultPort > UNDEFINED_PORT )
      {
        return String.valueOf( defaultPort );
      }
    }
    return null;
  }

  /**
   * Gets the default port number of the protocol associated
   * with the {@link java.net.URL} of this {@link URI}.
   * <p>
   * If the URL scheme or the URLStreamHandler
   * for the URL do not define a default port number,
   * then -1 is returned.
   *
   * @param uri
   *     the {@link URI} to be used
   *
   * @return the port number
   */
  private static int getDefaultPort( final URI uri )
  {
    try
    {
      return uri.toURL().getDefaultPort();
    }
    catch( final Exception ignore )
    {
    }
    return UNDEFINED_PORT;
  }

  /**
   * Builds an Apigee MAC token header value from specified input.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} is the specified timestamp value
   * <li>{@code nonce} is the specified nonce value
   * <li>{@code bodyhash} is the specified body hash value
   * <li>{@code mac} is signed hash of specified base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param ts
   *     the timestamp, as epoch time in seconds
   * @param nonce
   *     the nonce for one time use
   * @param bodyhash
   *     the body hash value, signed with the same secret as above
   * @param baseString
   *     the base string value, see
   *     {@link #resourceBaseString(long, String, String, String, String, String, String) baseStringForHMAC}
   *     {@link #baseString(Object...)}  baseString}
   *     {@link #buildBaseString(String, Object...) buildBaseString}
   *     method.
   *
   * @return the Apigee MAC token header value
   */
  public static String buildMacToken
  (
      final String id,
      final String secret,
      final long ts,
      final String nonce,
      final String bodyhash,
      final String baseString
  )
  {
    return format( MAC_TOKEN_FORMAT, id, ts, nonce, bodyhash, sign( secret, baseString ) );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based on the
   * specified input where there is <b>no</b> HTTP request body and the base
   * string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * NOTE: The base string HTTP request URI path value
   * will be {@link java.net.URLEncoder URL Encoded}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} will be an empty {@link String}
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationTokenWithEncodedPath
  (
      final String id,
      final String secret,
      final String method,
      final URI uri
  )
  {
    return getAuthorizationTokenWithEncodedPath( id, secret, method, uri, null );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based on the
   * specified input where there is a <b>forced</b> empty HTTP request body and the base
   * string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * NOTE: The base string HTTP request URI path value
   * will be {@link java.net.URLDecoder URL Decoded}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} is signed hash of an empty {@link String}
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getForcedAuthorizationTokenWithDecodedPath
  (
      final String id,
      final String secret,
      final String method,
      final URI uri
  )
  {
    final long ts = epochTimeInSeconds();
    final String nonce = nonce();
    final String bodyhash = sign( secret, EMPTY );
    return buildMacToken
        (
            id,
            secret,
            ts,
            nonce,
            bodyhash,
            resourceBaseString
                (
                    ts,
                    nonce,
                    method,
                    getResourcePath( uri, false ),
                    uri.getHost(),
                    getPort( uri ),
                    bodyhash
                )
        );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based on the
   * specified input where there is <b>no</b> HTTP request body and the base
   * string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * NOTE: The base string HTTP request URI path value
   * will be {@link java.net.URLDecoder URL Decoded}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} will be an empty {@link String}
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationTokenWithDecodedPath
  (
      final String id,
      final String secret,
      final String method,
      final URI uri
  )
  {
    return getAuthorizationTokenWithDecodedPath( id, secret, method, uri, null );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based on the
   * specified input where there is an HTTP request body and the base string
   * values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * NOTE: The base string HTTP request URI path value
   * will be {@link java.net.URLEncoder URL Encoded}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} is signed hash of specified request body value
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   * @param body
   *     the HTTP request body
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationTokenWithEncodedPath
  (
      final String id,
      final String secret,
      final String method,
      final URI uri,
      final String body
  )
  {
    return getAuthorizationToken( id, secret, method, uri, true, body );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based on the
   * specified input where there is an HTTP request body and the base string
   * values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * NOTE: The base string HTTP request URI path value
   * will be {@link java.net.URLDecoder URL Decoded}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} is signed hash of specified request body value
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   * @param body
   *     the HTTP request body
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationTokenWithDecodedPath
  (
      final String id,
      final String secret,
      final String method,
      final URI uri,
      final String body
  )
  {
    return getAuthorizationToken( id, secret, method, uri, false, body );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based
   * on the specified input for the HTTP request body and
   * the base string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} will be a generated nonce value
   * <li>{@code bodyhash} is signed hash of specified request body value
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   * @param urlEncode
   *     the URL encoding to be used for the HTTP request URI path,
   *     true meaning enable {@link java.net.URLEncoder URL Encoded}
   *     else false meaning enable {@link java.net.URLDecoder URL Decoded}
   * @param body
   *     the HTTP request body
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationToken
  (
      final String id,
      final String secret,
      final String method,
      final URI uri,
      final boolean urlEncode,
      final String body
  )
  {
    final String nonce = nonce();
    return getAuthorizationToken( id, secret, nonce, method, uri, urlEncode, body );
  }

  /**
   * Gets an Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based
   * on the specified input for the nonce, the HTTP request body and
   * the base string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} will be a generated timestamp value
   * <li>{@code nonce} is the specified nonce value
   * <li>{@code bodyhash} is signed hash of specified request body value
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param nonce
   *     the nonce for one time use
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   * @param urlEncode
   *     the URL encoding to be used for the HTTP request URI path,
   *     true meaning enable {@link java.net.URLEncoder URL Encoded}
   *     else false meaning enable {@link java.net.URLDecoder URL Decoded}
   * @param body
   *     the HTTP request body
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationToken
  (
      final String id,
      final String secret,
      final String nonce,
      final String method,
      final URI uri,
      final boolean urlEncode,
      final String body
  )
  {
    final long ts = epochTimeInSeconds();
    return getAuthorizationToken( id, secret, ts, nonce, method, uri, urlEncode, body );
  }

  /**
   * Gets an Apigee MAC token for
   * the {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value based
   * on the specified input for the timestamp, the nonce, the HTTP request body
   * and the base string values are built based on the specified {@link URI},
   * the {@link javax.ws.rs.HttpMethod HTTP request method}.
   * <p>
   * Apigee MAC token format:
   * <pre>MAC id="...",ts="...",nonce="...",bodyhash="...",mac="..."</pre>
   * <ul>
   * <li>{@code id} is the specified identifier value
   * <li>{@code ts} is the specified timestamp value
   * <li>{@code nonce} is the specified nonce value
   * <li>{@code bodyhash} is signed hash of specified request body value
   * <li>{@code mac} is signed hash of built base string value
   * </ul>
   *
   * @param id
   *     the Apigee access token or client identifier
   * @param secret
   *     the Apigee mac key or client secret
   * @param ts
   *     the timestamp, as epoch time in seconds
   * @param nonce
   *     the nonce for one time use
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param uri
   *     the {@link URI} to be used
   * @param urlEncode
   *     the URL encoding to be used for the HTTP request URI path,
   *     true meaning enable {@link java.net.URLEncoder URL Encoded}
   *     else false meaning enable {@link java.net.URLDecoder URL Decoded}
   * @param body
   *     the HTTP request body
   *
   * @return the Apigee MAC token for the
   * {@link javax.ws.rs.core.HttpHeaders#AUTHORIZATION Authorization} header value
   */
  public static String getAuthorizationToken
  (
      final String id,
      final String secret,
      final long ts,
      final String nonce,
      final String method,
      final URI uri,
      final boolean urlEncode,
      final String body
  )
  {
    final String bodyhash = getBodyHash( secret, GET.equals( method ) ? null : body );
    return buildMacToken
        (
            id,
            secret,
            ts,
            nonce,
            bodyhash,
            resourceBaseString
                (
                    ts,
                    nonce,
                    method,
                    getResourcePath( uri, urlEncode ),
                    uri.getHost(),
                    getPort( uri ),
                    bodyhash
                )
        );
  }

  /**
   * Gets the resource path from the specified {@code URI} by taking the
   * path and if present the query string and then apply URL codec
   *
   * @param uri
   *     the {@link URI} to be used
   * @param urlEncode
   *     the URL encoding to be used for the HTTP request URI path,
   *     true meaning enable {@link java.net.URLEncoder URL Encoded}
   *     else false meaning enable {@link java.net.URLDecoder URL Decoded}
   *
   * @return the resource path of the URI in the requested URL codec
   */
  public static String getResourcePath( final URI uri, final boolean urlEncode )
  {
    final String path;
    final String query = uri.getQuery();
    if( isNotEmpty( query ) )
    {
      path = uri.getPath() + "?" + query;
    }
    else
    {
      path = uri.getPath();
    }
    if( urlEncode )
    {
      return urlEncode( path );
    }
    return path;
  }

  /**
   * Gets the body hash value by signed the specified request body with the secret
   *
   * @param secret
   *     the Apigee mac key or client secret
   * @param body
   *     the HTTP request body
   *
   * @return the Base64 encoded signed hash {@code String} of the HTTP request body
   */
  public static String getBodyHash( final String secret, final String body )
  {
    if( body != null && body.length() > 0 )
    {
      return sign( secret, body );
    }
    return EMPTY;
  }

  /**
   * Builds the base string value by adding a trailing character to specified
   * values before being concatenated together into a single {@link String}.
   *
   * @param endline
   *     the trailing end line character
   * @param values
   *     the base string values
   *
   * @return the base string value
   */
  static String buildBaseString( final String endline, final Object... values )
  {
    final StringBuilder baseBuilder = new StringBuilder();
    for( final Object part : asList( values ) )
    {
      baseBuilder.append( part ).append( endline );
    }
    return baseBuilder.toString();
  }

  /**
   * Builds the base string value by adding a trailing newline ({@code \n}) character
   * to specified values before being concatenated together into a single {@link String}.
   *
   * @param values
   *     the base string values
   *
   * @return the base string value
   */
  public static String baseString( final Object... values )
  {
    return buildBaseString( BASE_STRING_ENDLINE, values );
  }

  /**
   * Builds the base string value for construct of a Apigee MAC token header value
   * with making serivce endpoint calls for resources.
   * All of values of the base string will having a trailing new line
   * ({@code \n}) character added to them before being concatenated together
   * into a single {@link String}.
   * <p>
   * The base string is composed of following ordered items:
   * <ol>
   * <li>a timestamp, as epoch time in seconds
   * <li>a nonce, a one-time used value
   * <li>the HTTP request method of the target
   * <li>the HTTP request URI path of the target, could be
   * {@link java.net.URLEncoder URL Encoded} or {@link java.net.URLDecoder URL Decoded}
   * <li>the HTTP request URI host of the target
   * <li>the HTTP request URI port of the target
   * <li>the body hash value (if <b>no</b> request body then can be {@code null} or an empty
   * {@code String}), see the {@link #getBodyHash(String, String) getBodyHash} method.
   * </ol>
   * <p>
   *
   * @param ts
   *     the timestamp, as epoch time in seconds
   * @param nonce
   *     the nonce for one time use
   * @param method
   *     the {@link javax.ws.rs.HttpMethod HTTP request method} to be used
   * @param path
   *     the HTTP request URI path
   * @param host
   *     the HTTP request URI host
   * @param port
   *     the HTTP request URI port
   * @param bodyhash
   *     the body hash value, signed with the same secret as above
   *
   * @return the base string value
   */
  public static String resourceBaseString
  (
      final long ts,
      final String nonce,
      final String method,
      final String path,
      final String host,
      final String port,
      final String bodyhash
  )
  {
    return baseString( ts, nonce, method, path, host, port, bodyhash );
  }

  /**
   * Signs a message with the specified secret using {@code HmacSHA256} algorithm
   *
   * @param secret
   *     the signature secret
   * @param message
   *     the message to be signed
   *
   * @return the Base64 encoded signed hash {@code String} of the original message
   */
  public static String sign( final String secret, final String message )
  {
    try
    {
      final Mac mac = Mac.getInstance( HMAC_SHA256_ALGORITHM );
      mac.init( new SecretKeySpec( secret.getBytes( UTF_8 ), HMAC_SHA256_ALGORITHM ) );
      final byte[] signatureBytes = mac.doFinal( message.getBytes( UTF_8 ) );
      return new String( BASE64_ENCODER.encode( signatureBytes ), UTF_8 );
    }
    catch( final RuntimeException rethrow )
    {
      throw rethrow;
    }
    catch( final Exception rethrow )
    {
      throw new IllegalStateException( rethrow );
    }
  }

  /**
   * Gets the Form URL Encoded
   * ({@link MediaType#APPLICATION_FORM_URLENCODED "application/x-www-form-urlencoded"}) request
   * body of the specified form, using the {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8}
   * charset.
   *
   * @param form
   *     the {@link Form} to be used
   *
   * @return the form URL encoded body value
   */
  public static String getFormURLEncodedBody( final Form form )
  {
    return getFormURLEncodedBody( form, UTF_8 );
  }

  /**
   * Gets the Form URL Encoded
   * ({@link MediaType#APPLICATION_FORM_URLENCODED "application/x-www-form-urlencoded"}) request
   * body of the specified multivalued map, using the
   * {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8} charset.
   *
   * @param form
   *     the {@link MultivaluedMap} to be used
   *
   * @return the form URL encoded body value
   */
  public static String getFormURLEncodedBody( final MultivaluedMap<String, String> form )
  {
    return getFormURLEncodedBody( form, UTF_8 );
  }

  /**
   * Gets the Form URL Encoded
   * ({@link MediaType#APPLICATION_FORM_URLENCODED "application/x-www-form-urlencoded"}) request
   * body of the specified form, using the provided charset.
   *
   * @param form
   *     the {@link Form} to be used
   * @param charset
   *     the {@link Charset} to be used
   *
   * @return the form URL encoded body value
   */
  public static String getFormURLEncodedBody( final Form form, final Charset charset )
  {
    final MultivaluedMap<String, String> formMap
        = ofNullable( form )
        .map( Form::asMap )
        .orElse( null );
    return getFormURLEncodedBody( formMap, charset );
  }

  /**
   * Gets the Form URL Encoded
   * ({@link MediaType#APPLICATION_FORM_URLENCODED "application/x-www-form-urlencoded"}) request
   * body of the specified multivalued map, using the provided charset.
   *
   * @param form
   *     the {@link MultivaluedMap} to be used
   * @param charset
   *     the {@link Charset} to be used
   *
   * @return the form URL encoded body value
   */
  public static String getFormURLEncodedBody
  (
      final MultivaluedMap<String, String> form,
      final Charset charset
  )
  {
    final Charset useCharset
        = ofNullable( charset )
        .orElseGet( Charset::defaultCharset );
    final MultivaluedMap<String, String> formMap
        = ofNullable( form )
        .orElseGet( MultivaluedHashMap::new );
    final StringBuilder builder = new StringBuilder();
    for( final Iterator<String> nameIterator = formMap.keySet().iterator(); nameIterator.hasNext(); )
    {
      final String name = nameIterator.next();
      for( final Iterator<String> valueIterator = emptyIfNull( formMap.get( name ) ).iterator(); valueIterator.hasNext(); )
      {
        final String value = valueIterator.next();
        builder.append( urlEncode( name, useCharset.name() ) );
        if( value != null )
        {
          builder.append( '=' );
          builder.append( urlEncode( value, useCharset.name() ) );
          if( valueIterator.hasNext() )
          {
            builder.append( '&' );
          }
        }
      }
      if( nameIterator.hasNext() )
      {
        builder.append( '&' );
      }
    }
    final byte[] bytes = builder.toString().getBytes( useCharset );
    return new String( bytes, 0, bytes.length, useCharset );
  }

  public static String urlEncode( final String value )
  {
    return urlEncode( value, UTF_8.name() );
  }

  public static String urlEncode( final String value, final String charset )
  {
    try
    {
      return encode( value, charset );
    }
    catch( final UnsupportedEncodingException rethrow )
    {
      throw new IllegalArgumentException( rethrow );
    }
  }
}
