package com.americanexpress.dc.mock.service.spi;


import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static java.lang.String.format;


/** @author Richard Wilson */
public abstract class RequestStubBuilderService
{

  private static class RequestStubBuilderHolder
  {

    static final ServiceLoader<RequestStubBuilderService> loader
        = ServiceLoader.load( RequestStubBuilderService.class );
  }

  public static RequestPatternBuilder getRequestPattern
      (
          final String key,
          final Map<String, Object> context
      )
  {
    try
    {
      for( final RequestStubBuilderService service : RequestStubBuilderHolder.loader )
      {
        final RequestStubBuilder builder = service.getBuilder( key );
        if( builder != null )
        {
          return builder.build( context );
        }
      }
    }
    catch( final ServiceConfigurationError serviceError )
    {
      serviceError.printStackTrace();
    }
    throw new UnsupportedOperationException
        (
            format
                (
                    "Please add an implementation of the %s interface for the '%s' endpoint key",
                    RequestStubBuilder.class.getName(),
                    key
                )
        );
  }

  protected abstract RequestStubBuilder getBuilder( String key );
}
