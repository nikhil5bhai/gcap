package com.americanexpress.dc.splitter.offers;


import com.americanexpress.dc.splitter.common.IdentifierCollectionResponseSplitter;
import com.americanexpress.dc.splitter.spi.ResponseSplitter;
import com.americanexpress.dc.splitter.spi.ResponseSplitterService;

import java.util.Map;
import java.util.TreeMap;


/** @author Richard Wilson */
public class OffersResponseSplitterService extends ResponseSplitterService
{

  private final Map<String, ResponseSplitter> map = new TreeMap<>();

  public OffersResponseSplitterService()
  {
    map.put( "offer_content", new OffersResponseSplitter() );
    map.put( "offer_eligible_accounts", new IdentifierCollectionResponseSplitter() );
    map.put( "offer_group", new IdentifierCollectionResponseSplitter() );
    map.put( "offer_enrollment", new IdentifierCollectionResponseSplitter( "treatment_identifier" ) );
  }

  @Override
  protected ResponseSplitter getSplitter( final String key )
  {
    return map.get( key );
  }
}
