package com.americanexpress.dc.util;


import com.americanexpress.dc.config.MongoConfiguration;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.*;


/** @author Richard Wilson */
@SuppressWarnings("all")
public final class NamingUtil
{

  public static final Function<String, String> LOWER_CASE = StringUtils::lowerCase;
  public static final Function<String, String> UPPER_CASE = StringUtils::upperCase;
  public static final Function<String, String> CAPITALIZE = StringUtils::capitalize;
  public static final Function<String, String> FULL_CAPITALIZE = LOWER_CASE.andThen( CAPITALIZE );
  public static final String ALPHANUMERIC_REGEX = "[\\W_]";

  private NamingUtil()
  {
  }

  public static final String DASH = "-";
  public static final String UNDERSCORE = "_";

  public static String trainCase( final String value )
  {
    return toCase( FULL_CAPITALIZE, DASH, value );
  }

  public static String kebabCase( final String value )
  {
    return toCase( LOWER_CASE, DASH, value );
  }

  public static String screamingKebabCase( final String value )
  {
    return toCase( StringUtils::upperCase, DASH, value );
  }

  public static String snakeCase( final String value )
  {
    return toCase( LOWER_CASE, UNDERSCORE, value );
  }

  public static String screamingSnakeCase( final String value )
  {
    return toCase( UPPER_CASE, UNDERSCORE, value );
  }

  public static String camelCase( final String value )
  {
    return uncapitalize( toCase( FULL_CAPITALIZE, EMPTY, value ) );
  }

  public static String screamingCamelCase( final String value )
  {
    return toCase( FULL_CAPITALIZE, EMPTY, value );
  }

  public static String toCase
      (
          final Function<String, String> finalCase,
          final String separator,
          final String value
      )
  {
    requireNonNull( finalCase );
    return ofNullable( value )
        .map( Stream::of )
        .map
            (
                stream ->
                    stream
                        .map( v -> replacePattern( v, ALPHANUMERIC_REGEX, SPACE ) )
                        .map( StringUtils::splitByCharacterTypeCamelCase )
                        .flatMap( Arrays::stream )
                        .filter( StringUtils::isNotBlank )
                        .map( finalCase )
                        .collect( joining( defaultString( separator ) ) )
            )
        .orElse( null );
  }
}
