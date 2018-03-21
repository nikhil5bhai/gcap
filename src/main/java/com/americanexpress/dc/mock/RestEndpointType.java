package com.americanexpress.dc.mock;


import static com.americanexpress.dc.util.NamingUtil.screamingSnakeCase;
import static java.lang.String.format;
import static org.apache.commons.lang3.EnumUtils.getEnum;



public enum RestEndpointType
{
  RESOURCE,
  COLLECTION,
  STATIC_COLLECTION;

  public static final String ENDPOINT_TYPE_MARKER = "(?i)^_(?:resource|collection|static_collection)_";
  public static final String PREFIXED_FORMAT = "_%s_%s";

  public static RestEndpointType fromString( final String name )
  {
    return getEnum( RestEndpointType.class, screamingSnakeCase( name ) );
  }

  public String prefix( final String value )
  {
    return format( PREFIXED_FORMAT, name().toLowerCase(), value );
  }
}
