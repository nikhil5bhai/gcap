package com.americanexpress.dc.util;


import com.americanexpress.dc.util.ApigeeUtil.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.americanexpress.dc.util.ApigeeUtil.*;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Modifier.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/** @author Richard Wilson */
public class ApigeeUtilTest
{

  private static final String MAC_TOKEN_CAPTURE_REGEX = "MAC id=\"([^\"]+)\",ts=\"(\\d+)\",nonce=\"([^\"]+)\",bodyhash=\"([^\"]*)\",mac=\"([^\"]+)\"";
  private static final Pattern MAC_TOKEN_CAPTURE_PATTERN = compile( MAC_TOKEN_CAPTURE_REGEX );

  @Test
  public void testEpochTimeInSeconds() throws Exception
  {
    final long startTimestamp = MILLISECONDS.toSeconds( currentTimeMillis() );
    final long actualTimestamp = epochTimeInSeconds();
    final long stopTimestamp = MILLISECONDS.toSeconds( currentTimeMillis() );
    assertThat( actualTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );
  }

  @Test
  public void testNonce() throws Exception
  {
    final Set<String> uniqueSet = new HashSet<>();
    final int limit = 10;
    for( int i = limit; i > 0; i-- )
    {
      assertNonce( uniqueSet, nonce() );
    }
    assertThat( uniqueSet, hasSize( limit ) );
  }

  @Test
  public void testNonceWithSuffix() throws Exception
  {
    final String suffix = AMEX_TAG;
    final Set<String> uniqueSet = new HashSet<>();
    final int limit = 10;
    for( int i = limit; i > 0; i-- )
    {
      assertNonce( uniqueSet, suffixedNonce( suffix ), suffix );
    }
    assertThat( uniqueSet, hasSize( limit ) );
  }

  @Test
  public void testNonceFromEmptyCollection() throws Exception
  {
    final Set<String> uniqueSet = new HashSet<>();
    final int limit = 10;
    for( int i = limit; i > 0; i-- )
    {
      assertNonce( uniqueSet, nonce( emptyList() ) );
    }
    assertThat( uniqueSet, hasSize( limit ) );
  }

  private void assertNonce( final Set<String> uniqueSet, final String nonce )
  {
    assertThat( uniqueSet.add( nonce ), is( true ) );
    assertThat( uniqueSet.add( nonce ), is( false ) );
  }

  private void assertNonce( final Set<String> uniqueSet, final String nonce, final String suffix )
  {
    assertThat( nonce, endsWith( suffix ) );
    assertThat( uniqueSet.add( nonce ), is( true ) );
    assertThat( uniqueSet.add( nonce ), is( false ) );
  }

  @Test
  public void testGetPort() throws Exception
  {
    final Map<URI, String> pairs = new LinkedHashMap<>();
    pairs.put( URI.create( "http://localhost/" ), "80" );
    pairs.put( URI.create( "http://localhost:9080/" ), "9080" );
    pairs.put( URI.create( "https://localhost/" ), "443" );
    pairs.put( URI.create( "https://localhost:9443/" ), "9443" );
    pairs.put( URI.create( "ftp://localhost/" ), "21" );
    pairs.put( URI.create( "ftp://localhost:9021/" ), "9021" );
    pairs.put( URI.create( "gopher://localhost/" ), null );
    pairs.put( URI.create( "gopher://localhost:70/" ), "70" );
    pairs.put( URI.create( "gopher://localhost:9070/" ), "9070" );
    for( final URI uri : pairs.keySet() )
    {
      final String expectedPort = pairs.get( uri );
      final String actualPort = getPort( uri );
      assertThat( actualPort, is( equalTo( expectedPort ) ) );
    }
  }

  @Test
  public void testGetMacToken() throws Exception
  {
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final long ts = 1457041790;
    final String nonce = "1f22d5c9-111d-40be-9c06-06f571cd4ee6:AMEX";
    final String type = "id";
    final String baseString = buildBaseString( "\n", id, ts, nonce, type );
    final String mac = sign( secret, baseString );
    final String body = "";
    final String bodyhash = getBodyHash( secret, body );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, ts, nonce, bodyhash, mac );
    final String actualMacToken = buildMacToken( id, secret, ts, nonce, bodyhash, buildBaseString( BASE_STRING_ENDLINE, id, ts, nonce, type ) );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetForcedMacTokenWithDecodedPathWhenMethodIsHTTPGet() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.GET;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getForcedAuthorizationTokenWithDecodedPath( id, secret, method, uri );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String capturedBodyHash = matcher.group( 4 );
    assertThat( capturedBodyHash, not( isEmptyOrNullString() ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, false ),
            uri.getHost(),
            getPort( uri ),
            capturedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, capturedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithDecodedPathWhenMethodIsHTTPGet() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.GET;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithDecodedPath( id, secret, method, uri );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = EMPTY;
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, false ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithDecodedPathWhenMethodIsHTTPPostWithoutBody() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.POST;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final String body = "";

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithDecodedPath( id, secret, method, uri, body );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = EMPTY;
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, false ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithDecodedPathWhenMethodIsHTTPPostWithBody() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.POST;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final ObjectMapper mapper = new ObjectMapper();
    final ArrayNode json = mapper.createArrayNode();
    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.put( randomAlphabetic( 4 ), randomAlphabetic( 5 ) );
    json.add( objectNode );
    final String body = mapper.writeValueAsString( json );

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithDecodedPath( id, secret, method, uri, body );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = getBodyHash( secret, body );
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, false ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithEncodedPathWhenMethodIsHTTPGet() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.GET;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithEncodedPath( id, secret, method, uri );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = EMPTY;
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, true ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithEncodedPathWhenMethodIsHTTPPostWithoutBody() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement" );
    final String method = HttpMethod.POST;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final String body = "";

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithEncodedPath( id, secret, method, uri, body );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = EMPTY;
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, true ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testGetMacTokenWithEncodedPathWhenMethodIsHTTPPostWithBody() throws Exception
  {
    final URI uri = URI.create( "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload" );
    final String method = HttpMethod.POST;
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final ObjectMapper mapper = new ObjectMapper();
    final ArrayNode json = mapper.createArrayNode();
    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.put( randomAlphabetic( 4 ), randomAlphabetic( 5 ) );
    json.add( objectNode );
    final String body = mapper.writeValueAsString( json );

    final long startTimestamp = Instant.now().getEpochSecond();
    final String actualMacToken = getAuthorizationTokenWithEncodedPath( id, secret, method, uri, body );
    final long stopTimestamp = Instant.now().getEpochSecond();

    final Matcher matcher = MAC_TOKEN_CAPTURE_PATTERN.matcher( actualMacToken );
    assertThat( matcher.matches(), is( true ) );
    assertThat( matcher.groupCount(), is( 5 ) );

    final String actualId = matcher.group( 1 );
    assertThat( actualId, is( equalTo( id ) ) );

    final long capturedTimestamp = Long.valueOf( matcher.group( 2 ) );
    assertThat( capturedTimestamp, allOf( greaterThanOrEqualTo( startTimestamp ), lessThanOrEqualTo( stopTimestamp ) ) );

    final String capturedNonce = matcher.group( 3 );
    assertThat( capturedNonce, not( isEmptyOrNullString() ) );

    final String actualBodyHash = matcher.group( 4 );
    final String expectedBodyHash = getBodyHash( secret, body );
    assertThat( actualBodyHash, is( equalTo( expectedBodyHash ) ) );

    final String actualBaseStringHash = matcher.group( 5 );
    final String expectedBaseString
        = resourceBaseString
        (
            capturedTimestamp,
            capturedNonce,
            method,
            getResourcePath( uri, true ),
            uri.getHost(),
            getPort( uri ),
            expectedBodyHash
        );
    final String expectedBaseStringHash = sign( secret, expectedBaseString );
    assertThat( actualBaseStringHash, is( equalTo( expectedBaseStringHash ) ) );

    final String expectedMacToken = format( MAC_TOKEN_FORMAT, id, capturedTimestamp, capturedNonce, expectedBodyHash, expectedBaseStringHash );
    assertThat( actualMacToken, is( equalTo( expectedMacToken ) ) );
  }

  @Test
  public void testBaseStringWithNewlineSeparator() throws Exception
  {
    final Object[] values = {
        "The string ü@foo-bar",
        "https://localhost:9441/myca/statementimage/us/api/v3/getDisclosureDownload?request_type=authreg_Statement",
        ""
    };
    final String expectedBaseString = createBaseString( values );
    final String actualBaseString = buildBaseString( "\n", values );
    assertThat( actualBaseString, is( equalTo( expectedBaseString ) ) );
  }

  private String createBaseString( final Object... values )
  {
    final Formatter formatter = new Formatter();
    if( values != null )
    {
      for( final Object value : values )
      {
        formatter.format( "%s\n", value );
      }
    }
    return formatter.toString();
  }

  @Test
  public void testSignature() throws Exception
  {
    final String id = "d3acacf2-5445-4f27-9eab-bce523c3255e";
    final String secret = "b04c0e15-32a2-446d-a1b9-6bb36c9d2d06";
    final long ts = 1457041790;
    final String nonce = "1f22d5c9-111d-40be-9c06-06f571cd4ee6:AMEX";
    final String type = "id";
    final String baseString = buildBaseString( "\n", id, ts, nonce, type );

    final String expectedMacSignature = "4gDGOAnivUrIb238A0kfXw2y8efPBHBhD90ZmOKgG1s=";
    final String actualMacSignature = sign( secret, baseString );
    assertThat( actualMacSignature, is( equalTo( expectedMacSignature ) ) );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSignatureInvalidKey() throws Exception
  {
    sign( "", "" );
  }

  @Test
  public void testGrantType() throws Exception
  {
    final GrantType[] enumValues = GrantType.values();
    for( final GrantType enumValue : enumValues )
    {
      assertThat( enumValue, is( notNullValue() ) );
      switch( enumValue )
      {
        case CLIENT_CREDENTIALS:
          break;
        case PASSWORD:
          break;
        case AUTHORIZATION_CODE:
          break;
        default:
          fail( format( "Expected check for '%s' enum value.", enumValue ) );
      }
    }

    final String[] stringValues
        =
        {
            "client_credentials",
            "password",
            "authorization_code"
        };
    for( final String stringValue : stringValues )
    {
      final GrantType enumValue = GrantType.fromString( stringValue );
      assertThat( enumValue, is( notNullValue() ) );
      assertThat( enumValue.toString(), is( equalTo( stringValue ) ) );
    }
    assertThat
        (
            format( "Expected %d string to enum conversion(s).", enumValues.length ),
            stringValues.length,
            is( equalTo( enumValues.length ) )
        );
  }

  @Test
  public void testGetFormURLEncodedBodyShouldBeEmpty() throws Exception
  {
    final Form nullFormObject = null;
    final String fromNullForm = getFormURLEncodedBody( nullFormObject );
    assertThat( fromNullForm, isEmptyString() );

    final String fromNullFormWithCharset = getFormURLEncodedBody( nullFormObject, ISO_8859_1 );
    assertThat( fromNullFormWithCharset, isEmptyString() );

    final String fromNullFormWithNullCharset = getFormURLEncodedBody( nullFormObject, null );
    assertThat( fromNullFormWithNullCharset, isEmptyString() );

    final String fromEmptyForm = getFormURLEncodedBody( new Form() );
    assertThat( fromEmptyForm, isEmptyString() );

    final Form formOfNulls = new Form();
    formOfNulls.param( null, null );
    final String fromFormOfNulls = getFormURLEncodedBody( formOfNulls );
    assertThat( fromFormOfNulls, isEmptyString() );

    final MultivaluedMap<String, String> nullMapObject = null;
    final String fromNullMap = getFormURLEncodedBody( nullMapObject );
    assertThat( fromNullMap, isEmptyString() );

    final String fromNullMapWithCharset = getFormURLEncodedBody( nullMapObject, ISO_8859_1 );
    assertThat( fromNullMapWithCharset, isEmptyString() );

    final String fromNullMapWithNullCharset = getFormURLEncodedBody( nullMapObject, null );
    assertThat( fromNullMapWithNullCharset, isEmptyString() );

    final String fromEmptyMap = getFormURLEncodedBody( new MultivaluedHashMap<>() );
    assertThat( fromEmptyMap, isEmptyString() );

    final MultivaluedHashMap<String, String> mapOfNulls = new MultivaluedHashMap<>();
    mapOfNulls.put( null, null );
    final String fromMapOfNulls = getFormURLEncodedBody( mapOfNulls );
    assertThat( fromMapOfNulls, isEmptyString() );
  }

  @Test
  public void testGetFormURLEncodedBody() throws Exception
  {
    final Form formObject = new Form( "~Héy, hello", "world!" );
    assertThat( getFormURLEncodedBody( formObject ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21" ) ) );

    assertThat( getFormURLEncodedBody( formObject, ISO_8859_1 ), is( equalTo( "%7EH%E9y%2C+hello=world%21" ) ) );

    assertThat( getFormURLEncodedBody( formObject, null ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21" ) ) );

    formObject.param( "b", "1" );
    formObject.param( "b", "2" );
    assertThat( getFormURLEncodedBody( formObject ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21&b=1&b=2" ) ) );

    final MultivaluedMap<String, String> mapObject = new MultivaluedHashMap<>( singletonMap( "~Héy, hello", "world!" ) );
    assertThat( getFormURLEncodedBody( mapObject ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21" ) ) );

    assertThat( getFormURLEncodedBody( mapObject, ISO_8859_1 ), is( equalTo( "%7EH%E9y%2C+hello=world%21" ) ) );

    assertThat( getFormURLEncodedBody( mapObject, null ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21" ) ) );

    mapObject.add( "b", "1" );
    mapObject.add( "b", "2" );
    assertThat( getFormURLEncodedBody( mapObject ), is( equalTo( "%7EH%C3%A9y%2C+hello=world%21&b=1&b=2" ) ) );
  }

  @Test
  public void testUrlEncode() throws Exception
  {
    assertThat( urlEncode( "random word £500 bank $" ), is( equalTo( "random+word+%C2%A3500+bank+%24" ) ) );
  }

  @Test
  public void testUrlEncodeWhenInvalidCharset() throws Exception
  {
    try
    {
      urlEncode( "random word £500 bank $", "" );
      fail( "Expected an exception to be thrown" );
    }
    catch( final Exception actualException )
    {
      assertThat( actualException, is( instanceOf( IllegalArgumentException.class ) ) );
      assertThat( actualException.getMessage(), is( equalTo( "java.io.UnsupportedEncodingException: " ) ) );
      final Throwable actualCause = actualException.getCause();
      assertThat( actualCause, is( instanceOf( UnsupportedEncodingException.class ) ) );
      assertThat( actualCause.getMessage(), is( equalTo( "" ) ) );
    }
  }

  @Test
  public void testUrlEncodeWhenNullCharset() throws Exception
  {
    try
    {
      urlEncode( "random word £500 bank $", null );
      fail( "Expected an exception to be thrown" );
    }
    catch( final Exception actualException )
    {
      assertThat( actualException, is( instanceOf( NullPointerException.class ) ) );
      assertThat( actualException.getMessage(), is( equalTo( "charsetName" ) ) );
      assertThat( actualException.getCause(), is( nullValue() ) );
    }
  }

  @Test
  public void testApigeeUtilClass() throws Exception
  {
    assertUtilityClass( ApigeeUtil.class );
  }

  /**
   * Checks that a utility class is well formed.
   *
   * @param utilityClass
   *     utility class to check.
   */
  private void assertUtilityClass( final Class<?> utilityClass ) throws Exception
  {
    assertThat
        (
            "Utility class must be present.",
            utilityClass,
            is( notNullValue() )
        );
    assertThat
        (
            "Utility class must be final.",
            isFinal( utilityClass.getModifiers() ),
            is( true )
        );
    final Constructor<?>[] declaredConstructors = utilityClass.getDeclaredConstructors();
    assertThat
        (
            "Utility class must be only one constructor.",
            declaredConstructors,
            is( arrayWithSize( 1 ) )
        );
    final Constructor<?> constructor = declaredConstructors[ 0 ];
    if( constructor.isAccessible() || !isPrivate( constructor.getModifiers() ) )
    {
      fail( "Utility class constructor is not private." );
    }
    // WORKAROUND: Code Line Coverage
    // BEGIN - WORKAROUND
    constructor.setAccessible( true );
    final Object instance = constructor.newInstance();
    assertThat( "Utility class should instantiate.", instance, is( notNullValue() ) );
    constructor.setAccessible( false );
    // END - WORKAROUND
    for( final Method method : utilityClass.getMethods() )
    {
      if( !isStatic( method.getModifiers() ) && utilityClass.equals( method.getDeclaringClass() ) )
      {
        fail( format( "Utility class has a non-static method: %s.", method ) );
      }
    }
  }
}