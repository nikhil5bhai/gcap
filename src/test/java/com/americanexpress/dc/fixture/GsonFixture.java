package com.americanexpress.dc.fixture;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Properties;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_DASHES;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;


/** @author Richard Wilson */
@SuppressWarnings("all")
public interface GsonFixture
{

  Gson GSON
      = new GsonBuilder()
      .create();
  Gson GSON_SNAKE_CASE
      = new GsonBuilder()
      .setFieldNamingPolicy( LOWER_CASE_WITH_UNDERSCORES )
      .create();
  Gson GSON_KEBAB_CASE
      = new GsonBuilder()
      .setFieldNamingPolicy( LOWER_CASE_WITH_DASHES )
      .create();
  String JSON_NULL = "null";
  JsonNull GSON_NULL = JsonNull.INSTANCE;


  default JsonElement loadJson( final String resourceName )
  {
    return loadJson( null, resourceName, JsonElement.class );
  }

  default <T> T loadJson( final String resourceName, final Class<T> classType )
  {
    return loadJson( null, resourceName, (Type)classType );
  }

  default <T> T loadJson( final String resourceName, final Type type )
  {
    return loadJson( null, resourceName, type );
  }

  default JsonElement loadJson( final Gson gson, final String resourceName )
  {
    return loadJson( null, resourceName, JsonElement.class );
  }

  default <T> T loadJson( final Gson gson, final String resourceName, final Class<T> classType )
  {
    return loadJson( gson, resourceName, (Type)classType );
  }

  default <T> T loadJson( final Gson gson, final String resourceName, final Type type )
  {
    try( final Reader reader = new InputStreamReader( getClass().getResourceAsStream( resourceName ), UTF_8 ) )
    {
      return ofNullable( gson ).orElse( GSON ).fromJson( reader, type );
    }
    catch( final NullPointerException | IOException rethrow )
    {
      throw new IllegalStateException( format( "Error - Loading '%s' Resource.", resourceName ) );
    }
  }

  default String loadResourceAsString( final String resourceName )
  {
    try( final InputStream stream = getClass().getResourceAsStream( resourceName ) )
    {
      return IOUtils.toString( stream );
    }
    catch( final NullPointerException | IOException rethrow )
    {
      throw new IllegalStateException( format( "Error - Loading '%s' Resource.", resourceName ) );
    }
  }

  default Properties loadResourceAsProperties( final String resourceName )
  {
    final Properties properties = new Properties();
    try( final InputStream stream = getClass().getResourceAsStream( resourceName ) )
    {
      properties.load( stream );
      return properties;
    }
    catch( final NullPointerException | IOException rethrow )
    {
      throw new IllegalStateException( format( "Error - Loading '%s' Resource.", resourceName ) );
    }
  }

  default JsonElement toElement( final Object value )
  {
    return toElement( null, value, JsonElement.class );
  }

  default <T> T toElement( final Object value, final Class<T> classType )
  {
    return toElement( null, value, classType );
  }

  default JsonElement toElement( final Gson gson, final Object value )
  {
    final Gson mapper = ofNullable( gson ).orElse( GSON );
    return ofNullable( value )
        .map( mapper::toJson )
        .map( json -> mapper.fromJson( json, JsonElement.class ) )
        .orElse( null );
  }

  default <T> T toElement( final Gson gson, final Object value, final Class<T> classType )
  {
    final Gson mapper = ofNullable( gson ).orElse( GSON );
    return ofNullable( value )
        .map( mapper::toJson )
        .map( json -> mapper.fromJson( json, classType ) )
        .orElse( null );
  }
}
