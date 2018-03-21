package com.americanexpress.dc.builder.spi;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.String.format;
import static java.util.Collections.checkedCollection;
import static java.util.Collections.checkedMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;


/** @author Richard Wilson */
public abstract class RequestBuilder
{

  /**
   * Constructs the HTTP request body based on provided input
   *
   * @param input
   *     the map containing input request objects
   *
   * @return the constructed HTTP request body
   */
  public abstract String getBody( Map<String, Object> input );

  @SuppressWarnings("all")
  protected static final BinaryOperator<JsonArray> JSON_ARRAY_ADD_ALL
      = ( left, right ) ->
  {
    // NOTE: WORKAROUND to Gradle Dependency Loading Conflicts
    // NOTE: This WORKAROUND is NOT needed, if addressed in build.gradle dependencies
    final Method addAll = getMethod( JsonArray.class, "addAll", JsonArray.class );
    if( addAll != null )
    {
      left.addAll( right );
    }
    else
    {
      for( final JsonElement element : right )
      {
        left.add( element );
      }
    }
    return left;
  };

  protected static <T> Collector<T, ?, JsonArray> toJsonArray()
  {
    final BiConsumer<JsonArray, T> JSON_ARRAY_ADD
        = ( elements, element ) ->
    {
      if( element == null || element instanceof JsonElement )
      {
        elements.add( (JsonElement)element );
      }
      else if( element instanceof String )
      {
        elements.add( (String)element );
      }
      else if( element instanceof Character )
      {
        elements.add( (Character)element );
      }
      else if( element instanceof Number )
      {
        elements.add( (Number)element );
      }
      else if( element instanceof Boolean )
      {
        elements.add( (Boolean)element );
      }
      else
      {
        try
        {
          JsonArray.class.getMethod( "add", element.getClass() );
        }
        catch( final NoSuchMethodException rethrow )
        {
          throw new NoSuchMethodError( rethrow.getMessage() );
        }
      }
    };
    return Collector.of
        (
            JsonArray::new,
            JSON_ARRAY_ADD,
            JSON_ARRAY_ADD_ALL,
            IDENTITY_FINISH
        );
  }

  @SuppressWarnings("all")
  protected static Method getMethod
      (
          final Class<?> targetClass,
          final String methodName,
          final Class<?>... methodParameters
      )
  {
    try
    {
      return targetClass.getMethod( methodName, methodParameters );
    }
    catch( final Exception ignore )
    {
    }
    return null;
  }

  protected <K, V> Map<K, V> getRequiredMap
      (
          final Map<String, Object> input,
          final String key,
          final Class<K> keyType,
          final Class<V> valueType
      )
  {
    return getMap( input, key, keyType, valueType )
        .orElseThrow( illegalArgument( "Missing '%s' input map value.", key ) );
  }

  protected <K, V> Optional<Map<K, V>> getMap
      (
          final Map<String, Object> input,
          final String key,
          final Class<K> keyType,
          final Class<V> valueType
      )
  {
    requireNonNull( keyType );
    requireNonNull( valueType );
    return ofNullable( requireNonNull( input ).get( key ) )
        .filter( Map.class::isInstance )
        .map( Map.class::cast )
        .map( toTypedMap( keyType, valueType ) );
  }

  @SuppressWarnings({"all", "rawtypes", "unchecked"})
  protected <K, V> Function<Map, Map<K, V>> toTypedMap
      (
          final Class<K> keyType,
          final Class<V> valueType
      )
  {
    return map -> checkedMap( map, keyType, valueType );
  }

  protected <T> Collection<T> getRequiredValues
      (
          final Map<String, Object> input,
          final String key,
          final Class<T> type
      )
  {
    return getValues( input, key, type )
        .orElseThrow( illegalArgument( "Missing '%s' input collection value.", key ) );
  }

  protected <T> Optional<Collection<T>> getValues
      (
          final Map<String, Object> input,
          final String key,
          final Class<T> type
      )
  {
    requireNonNull( type );
    return ofNullable( requireNonNull( input ).get( key ) )
        .filter( Collection.class::isInstance )
        .map( Collection.class::cast )
        .map( toTypedCollection( type ) );
  }

  @SuppressWarnings({"all", "rawtypes", "unchecked"})
  protected <T> Function<Collection, Collection<T>> toTypedCollection( final Class<T> type )
  {
    return collection -> checkedCollection( collection, type );
  }

  protected <T> T getRequiredValue
      (
          final Map<String, Object> input,
          final String key,
          final Class<T> type
      )
  {
    return getValue( input, key, type )
        .orElseThrow( illegalArgument( "Missing '%s' input value.", key ) );
  }

  protected <T> Optional<T> getValue
      (
          final Map<String, Object> input,
          final String key,
          final Class<T> type
      )
  {
    requireNonNull( type );
    return ofNullable( requireNonNull( input ).get( key ) )
        .filter( type::isInstance )
        .map( type::cast );
  }

  @SuppressWarnings("all")
  protected Supplier<IllegalArgumentException> illegalArgument
      (
          final String format,
          final Object... values
      )
  {
    return () -> new IllegalArgumentException( format( format, values ) );
  }
}
