package com.americanexpress.dc.splitter.spi;


import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static java.util.Collections.emptyList;


/** @author Richard Wilson */
public abstract class ResponseSplitterService
{

  private static class ResourceSplitterHolder
  {
    static final ServiceLoader<ResponseSplitterService> loader
        = ServiceLoader.load( ResponseSplitterService.class );
  }

  public static Collection<Map<String, Object>> getResources( final String key, final Response response )
  {
    try
    {
      for( final ResponseSplitterService service : ResourceSplitterHolder.loader )
      {
        final ResponseSplitter splitter = service.getSplitter( key );
        if( splitter != null )
        {
          return splitter.split( response );
        }
      }
    }
    catch( final ServiceConfigurationError serviceError )
    {
      serviceError.printStackTrace();
    }
    return emptyList();
  }

  protected abstract ResponseSplitter getSplitter( String key );
}
