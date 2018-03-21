package com.americanexpress.dc.mock;


import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

import static com.axp.myca.common.util.Logger.logInfo;
import static com.axp.myca.common.util.constants.Constants.SERVICE_NAME;


/** @author Richard Wilson */
@SuppressWarnings("unused")
public class ClientLoggingFilter
    extends HttpExchangeLogging
    implements ClientRequestFilter, ClientResponseFilter
{

  @Override
  public void filter( final ClientRequestContext requestContext ) throws IOException
  {
    logInfo
        (
            SERVICE_NAME,
            this,
            "filter",
            getClientRequestAsString( requestContext )
        );
  }

  @Override
  public void filter
      (
          final ClientRequestContext requestContext,
          final ClientResponseContext responseContext
      )
      throws IOException
  {
    logInfo
        (
            SERVICE_NAME,
            this,
            "filter",
            getClientResponseAsString( responseContext )
        );
  }
}
