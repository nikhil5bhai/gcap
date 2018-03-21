package com.americanexpress.dc.splitter.offers;


import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;


/** @author Richard Wilson */
public class OffersFirstEligibleResponseSplitter extends OffersResponseSplitter
{

  private static final String OFFER_STATUS_FIELD = "customer_treatment_status";
  private static final Predicate<String> IS_ELIGIBLE = "ELIGIBLE"::equalsIgnoreCase;

  @Override
  protected Predicate<JsonElement> filterStream()
  {
    final Predicate<JsonElement> superPredicate = super.filterStream();
    final Predicate<JsonElement> isEligibleOffer
        = fieldValuePredicate( OFFER_STATUS_FIELD, IS_ELIGIBLE );
    return superPredicate.and( isEligibleOffer );
  }

  @Override
  protected Collection<Map<String, Object>> streamToCollection
      (
          final Stream<Map<String, Object>> stream
      )
  {
    return super.streamToCollection( stream.limit( 1 ) );
  }
}
