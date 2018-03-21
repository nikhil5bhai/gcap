package com.americanexpress.dc.splitter.common;


import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.dc.splitter.spi.ResponseSplitter;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;


/** @author Richard Wilson */
public abstract class AbstractResponseSplitter<T> implements ResponseSplitter
{

  protected final Collection<Integer> validStatuses;

  public AbstractResponseSplitter( final Integer... validStatuses )
  {
    this.validStatuses
        = ofNullable( validStatuses )
        .map( Arrays::asList )
        .orElseGet( Collections::emptyList );
  }

  @Override
  public Collection<Map<String, Object>> split( final Response response )
  {
    return ofNullable( response )
        .filter( this::shouldProcessResponse )
        .map( CollectionUploadUtil::getContentAsString )
        .map( this::convertTo )
        .filter( this::shouldProcessSplit )
        .map( Stream::of )
        .map( this::processStream )
        .map( this::streamToCollection )
        .orElseGet( Collections::emptyList );
  }

  protected boolean shouldProcessResponse( final Response response )
  {
    return hasValidStatus( response.getStatus() );
  }

  protected boolean hasValidStatus( final Integer status )
  {
    return validStatuses.contains( status );
  }

  protected abstract T convertTo( String content );

  protected abstract boolean shouldProcessSplit( T stream );

  protected abstract Stream<Map<String, Object>> processStream( Stream<T> stream );

  protected Collection<Map<String, Object>> streamToCollection
      (
          final Stream<Map<String, Object>> stream
      )
  {
    return stream.collect( toCollection( LinkedList::new ) );
  }
}
