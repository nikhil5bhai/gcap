package com.americanexpress.dc.util;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.*;


/** @author Richard Wilson */
public class UUIDUtil
{

  // UUID Canonical Segment Limits: 8-4-4-4-12
  public static final int[] SEGMENT_LIMITS = { 8, 4, 4, 4, 12 };
  public static final Pattern HEXADECIMAL_REGEX = compile( "\\p{XDigit}+" );
  public static final String ZERO = "0";
  public static final String HYPHEN = "-";

  public static UUID toUUID( final String hexString )
  {
    if( isNotBlank( hexString ) && HEXADECIMAL_REGEX.matcher( hexString ).matches() )
    {
      final List<String> segments = new ArrayList<>( 5 );
      int current = 0;
      for( final int limit : SEGMENT_LIMITS )
      {
        segments.add( defaultIfBlank( substring( hexString, current, current = current + limit ), ZERO ) );
      }

      final String uuidString = join( segments, HYPHEN );
      return UUID.fromString( uuidString );
    }

    throw new IllegalArgumentException( format( "Invalid UUID string: '%s'", hexString ) );
  }
}
