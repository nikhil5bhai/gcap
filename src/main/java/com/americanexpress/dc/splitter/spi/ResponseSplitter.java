package com.americanexpress.dc.splitter.spi;


import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;


/** @author Richard Wilson */
public interface ResponseSplitter
{

  Collection<Map<String, Object>> split( Response response );
}
