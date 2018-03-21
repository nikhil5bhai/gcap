package com.americanexpress.dc.splitter.offers;


import com.americanexpress.dc.splitter.common.JsonResponseSplitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSortedSet;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.StreamSupport.stream;


/** @author Richard Wilson */
public class OffersResponseSplitter extends JsonResponseSplitter
{

  private static final Set<String> VALID_CODES
      = unmodifiableSortedSet( new TreeSet<>( asList( "PZN0000", "PZN0001" ) ) );
  private static final Predicate<String> CARD_DOMAIN = "CARD"::equalsIgnoreCase;
  private static final Predicate<String> NOT_CARD_DOMAIN = CARD_DOMAIN.negate();
  private static final Collection<String> ELEMENT_TO_MAP_FIELDS
      = unmodifiableCollection( asList( "treatment_identifier", "domain" ) );

  public OffersResponseSplitter()
  {
    super( HTTP_OK, HTTP_CREATED, HTTP_PARTIAL );
  }

  @Override
  protected boolean shouldProcessSplit( final JsonElement element )
  {
    return Optional
        .of( element )
        .filter( JsonElement::isJsonObject )
        .map( JsonElement::getAsJsonObject )
        .map( o -> o.get( "global_response" ) )
        .filter( JsonElement::isJsonObject )
        .map( JsonElement::getAsJsonObject )
        .map( o -> o.get( "explanation_code" ) )
        .filter( JsonElement::isJsonPrimitive )
        .map( JsonElement::getAsJsonPrimitive )
        .map( JsonPrimitive::getAsString )
        .map( String::toUpperCase )
        .map( VALID_CODES::contains )
        .orElse( false );
  }

  @Override
  protected Stream<Map<String, Object>> processStream( final Stream<JsonElement> stream )
  {
    return stream
        .flatMap( jsonValues( "treatments" ) )
        .flatMap( jsonValues( "component_treatments" ) )
        .flatMap( jsonValues( "treatment_metadata" ) )
        .filter( filterStream() )
        .map( this::elementToMap );
  }

  protected Predicate<JsonElement> filterStream()
  {
    return fieldValuePredicate( "domain", NOT_CARD_DOMAIN );
  }

  @SuppressWarnings({ "rawtypes" })
  private Function<JsonElement, Stream<JsonElement>> jsonValues( final String key )
  {
    return element ->
        ofNullable( element )
            .filter( JsonElement::isJsonObject )
            .map( JsonElement::getAsJsonObject )
            .map( e -> e.get( key ) )
            .filter( JsonElement::isJsonArray )
            .map( JsonElement::getAsJsonArray )
            .map( Iterable::spliterator )
            .map( spliterator -> stream( spliterator, false ) )
            .orElseGet( Stream::empty );
  }

  protected Predicate<JsonElement> fieldValuePredicate
      (
          final String fieldName,
          final Predicate<String> predicateMapper
      )
  {
    requireNonNull( fieldName );
    requireNonNull( predicateMapper );
    return element ->
        ofNullable( element )
            .filter( JsonElement::isJsonObject )
            .map( JsonElement::getAsJsonObject )
            .map( o -> o.get( fieldName ) )
            .map( JsonElement::getAsJsonPrimitive )
            .map( JsonPrimitive::getAsString )
            .map( predicateMapper::test )
            .orElse( false );
  }

  private Map<String, Object> elementToMap( final JsonElement element )
  {
    final JsonObject object
        = ofNullable( element )
        .filter( JsonElement::isJsonObject )
        .map( JsonElement::getAsJsonObject )
        .orElse( null );

    final Map<String, Object> map = new LinkedHashMap<>();

    ELEMENT_TO_MAP_FIELDS
        .forEach( field -> fieldStringToValue( object, field, map ) );

    return map;
  }

  private void fieldStringToValue
      (
          final JsonObject source,
          final String key,
          final Map<String, Object> destination
      )
  {
    final String value
        = ofNullable( source )
        .map( o -> o.get( key ) )
        .map( JsonElement::getAsJsonPrimitive )
        .map( JsonPrimitive::getAsString )
        .orElse( null );
    if( destination != null )
    {
      destination.put( key, value );
    }
  }
}
