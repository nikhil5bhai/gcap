package com.americanexpress.dc.splitter.common;


import com.americanexpress.dc.splitter.spi.ResponseSplitter;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;


/** @author Richard Wilson */
public class IdentifierCollectionResponseSplitter implements ResponseSplitter
{

  protected static final String ID = "id";

  private final String identifier;

  public IdentifierCollectionResponseSplitter()
  {
    this( ID );
  }

  public IdentifierCollectionResponseSplitter( final String identifier )
  {
    this.identifier = requireNonNull( identifier );
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Map<String, Object>> split( final Response response )
  {
    return (Collection<Map<String, Object>>)
        ofNullable( response )
            .filter( r -> r.getStatus() == 200 )
            .map( Response::getEntity )
            .filter( Collection.class::isInstance )
            .map( Collection.class::cast )
            .orElseGet( Collections::emptyList )
            .stream()
            .map( toIdMap() )
            .collect( toCollection( LinkedList::new ) );
  }

  private <T> Function<T, Map<String, Object>> toIdMap()
  {
    return t -> singletonMap( identifier, t );
  }

  private static Properties getProperties( final String resourcePath )
  {
    try( final InputStream input = getContextResourceAsStream( resourcePath ) )
    {
      final Properties properties = new Properties();
      properties.load( input );
      return properties;
    }
    catch( final IOException rethrow )
    {
      throw new IllegalStateException( rethrow );
    }
  }

  private static InputStream getContextResourceAsStream
      (
          final String resourcePath
      )
      throws IOException
  {
    return getClassLoaderResourceAsStream( currentThread().getContextClassLoader(), resourcePath );
  }

  private static InputStream getClassLoaderResourceAsStream
      (
          final ClassLoader classLoader,
          final String resourcePath
      )
      throws IOException
  {
    requireNonNull( classLoader );
    final InputStream resourceAsStream
        = classLoader
        .getResourceAsStream( resourcePath );
    return ensureResourceAsStream( resourcePath, resourceAsStream );
  }

  private static InputStream getClassResourceAsStream
      (
          final Class<?> aClass,
          final String resourcePath
      )
      throws IOException
  {
    requireNonNull( aClass );
    final InputStream resourceAsStream
        = aClass
        .getResourceAsStream( resourcePath );
    return ensureResourceAsStream( resourcePath, resourceAsStream );
  }

  private static InputStream ensureResourceAsStream
      (
          final String resourcePath,
          final InputStream stream
      )
      throws IOException
  {
    return ofNullable( stream )
        .orElseThrow
            (
                () ->
                    new FileNotFoundException
                        (
                            format( "Resource '%s' not found.", resourcePath )
                        )
            );
  }
}
