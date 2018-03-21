package com.americanexpress.dc.builder.spi;


import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;


/** @author Richard Wilson */
public abstract class RequestBuilderService
{

  private static class RequestBuilderHolder
  {
    static final ServiceLoader<RequestBuilderService> loader
        = ServiceLoader.load( RequestBuilderService.class );
  }

  public static String getBody( final String key, final Map<String, Object> input )
  {
    try
    {
      for( final RequestBuilderService service : RequestBuilderHolder.loader )
      {
        final RequestBuilder builder = service.getBuilder( key );
        if( builder != null )
        {
          return builder.getBody( input );
        }
      }
    }
    catch( final ServiceConfigurationError serviceError )
    {
      serviceError.printStackTrace();
    }
    return null;
  }

  public abstract RequestBuilder getBuilder( String key );
}
