package com.americanexpress.dc.mock;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.americanexpress.dc.util.Constants.MOCK_PROXY_URL;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.file.FileSystems.getFileSystem;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.lines;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;



public class ProxyInitializer
{

  public static final String PROXY_POLICY = "/proxy.policy";

  public static void initialize( final WireMockServer wireMockServer )
  {
    try( final Stream<String> lines = getResourceLinesStream( PROXY_POLICY ) )
    {
      lines
          .filter( StringUtils::isNotBlank )
          .map( ProxyInitializer::toURI )
          .collect( groupingBy( ProxyInitializer::toProxyBase, mapping( URI::getPath, toSet() ) ) )
          .entrySet()
          .forEach( registerProxies( wireMockServer ) );
    }
  }

  private static Stream<String> getResourceLinesStream( final String name )
  {
    return ofNullable( name )
        .map( ProxyInitializer.class::getResource )
        .map( ProxyInitializer::toPath )
        .filter( Files::exists )
        .map( ProxyInitializer::toLines )
        .orElseGet( Stream::empty );
  }

  private static Path toPath( final URL url )
  {
    final String protocol = url.getProtocol();
    if( "jar".equalsIgnoreCase( protocol ) )
    {
      final String[] jarParts = url.toString().split( "!" );
      return createFileSystem( jarParts[ 0 ] ).getPath( jarParts[ 1 ] );
    }

    return ofNullable( toURI( url ) )
        .map( Paths::get )
        .orElseThrow( () -> new IllegalArgumentException( "Failure to create Path from URL." ) );
  }

  private static FileSystem createFileSystem( final String url )
  {
    final URI uri = toURI( url );
    try
    {
      return getFileSystem( uri );
    }
    catch( final FileSystemNotFoundException rethrow )
    {
      return createFileSystem( uri );
    }
  }

  private static FileSystem createFileSystem( final URI uri )
  {
    try
    {
      return newFileSystem( uri, emptyMap() );
    }
    catch( final IOException rethrow )
    {
      throw new IllegalArgumentException( "Failure to create Jar File System from URL.", rethrow );
    }
  }

  private static URI toURI( final URL url )
  {
    try
    {
      return url.toURI();
    }
    catch( final URISyntaxException rethrow )
    {
      throw new IllegalArgumentException( "Failure to create URI from URL.", rethrow );
    }
  }

  private static URI toURI( final String uriString )
  {
    try
    {
      return new URI( uriString );
    }
    catch( final URISyntaxException rethrow )
    {
      throw new IllegalArgumentException( "Failure to create URI from string value.", rethrow );
    }
  }

  private static Stream<String> toLines( final Path path )
  {
    try
    {
      return lines( path );
    }
    catch( final IOException rethrow )
    {
      throw new UncheckedIOException( "Failure to open the file.", rethrow );
    }
  }

  private static String toProxyBase( final URI uri )
  {
    return substringBeforeLast( uri.toString(), uri.getPath() );
  }

  private static Consumer<Entry<String, Set<String>>> registerProxies
      (
          final WireMockServer wireMockServer
      )
  {
    return entry ->
    {
      final String proxyBase = entry.getKey();
      entry.getValue().forEach( regex -> addProxyWiring( wireMockServer, proxyBase, regex ) );
    };
  }

  private static void addProxyWiring
      (
          final WireMockServer wireMockServer,
          final String proxyBaseUrl,
          final String urlRegex
      )
  {

    System.out.println ("Header values"+  proxyBaseUrl + urlRegex  );

    wireMockServer
        .stubFor
            (
                any( urlMatching( urlRegex ) )
                    .atPriority( 10 )
                    .willReturn
                        (
                            aResponse()
                                .withHeader( MOCK_PROXY_URL, proxyBaseUrl + urlRegex )
                                .proxiedFrom( proxyBaseUrl )
                        )
            );
  }

/*
  public static void main( String[] args )
  {
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    final Path path = Paths.get( "/Users/rwils14/Projects/axp-myca-mock-rest/mapper.json" );

    try( final BufferedReader reader = Files.newBufferedReader( path ) )
    {
      final JsonElement jsonElement = gson.fromJson( reader, JsonElement.class );
      final JsonArray requests
          = ofNullable( jsonElement )
          .filter( JsonElement::isJsonArray )
          .map( JsonElement::getAsJsonArray )
          .map( Iterable::spliterator )
          .map( spliterator -> stream( spliterator, false ) )
          .orElseGet( Stream::empty )
          .filter( Objects::nonNull )
          .filter( JsonElement::isJsonObject )
          .map( JsonElement::getAsJsonObject )
          .map
              (
                  o ->
                  {
                    final Optional<JsonObject> mapping = Optional.of( o );
                    final JsonObject request
                        = mapping
                        .map( r -> r.get( "request" ) )
                        .filter( JsonElement::isJsonObject )
                        .map( JsonElement::getAsJsonObject )
                        .orElseGet( JsonObject::new );
                    final Optional<String> serviceInfo
                        = mapping
                        .map( r -> r.get( "response" ) )
                        .filter( JsonElement::isJsonObject )
                        .map( JsonElement::getAsJsonObject )
                        .map( r -> r.get( "headers" ) )
                        .filter( JsonElement::isJsonObject )
                        .map( JsonElement::getAsJsonObject )
                        .map( r -> r.get( "MOCK-SERVICE-CALL-NAME" ) )
                        .filter( JsonElement::isJsonPrimitive )
                        .map( JsonElement::getAsString );
                    if( serviceInfo.isPresent() )
                    {
                      final String serviceCall = serviceInfo.get();
                      request.addProperty( "serviceCall", serviceCall );
                    }
                    return request;
                  }
              )
          .filter( Objects::nonNull )
          .filter( JsonElement::isJsonObject )
          .map( JsonElement::getAsJsonObject )
          .map
              (
                  o ->
                  {
                    final boolean hasQueryParameters
                        = ofNullable( o.get( "queryParameters" ) )
                        .filter( JsonElement::isJsonObject )
                        .map( JsonElement::getAsJsonObject )
                        .map( removeElement( "collection_name" ) )
                        .map( removeElement( "response_id" ) )
                        .map( qp -> !qp.entrySet().isEmpty() )
                        .orElse( false );
                    if( !hasQueryParameters )
                    {
                      o.remove( "queryParameters" );
                    }
                    return o;
                  }
              )
          .collect( toJsonArray() );
      System.out.println( "( requests ) = " + gson.toJson( requests ) );
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
  }

  private static Function<JsonObject, JsonObject> removeElement( final String name )
  {
    return object ->
    {
      object.remove( name );
      return object;
    };
  }
*/
}
