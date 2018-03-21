package com.americanexpress.dc.mock.service.stub;


import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.americanexpress.dc.util.Constants.USER_KEY;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;


/** @author Richard Wilson */
public class BodyContainsAccountNumber implements RequestStubBuilder
{

  private final RequestMethod method;
  private final UrlPathPattern urlMatcher;
  private final List<String> bodyParts;

  public BodyContainsAccountNumber
      (
          final RequestMethod method,
          final UrlPathPattern urlMatcher,
          final String... bodyParts
      )
  {
    this.method = requireNonNull( method );
    this.urlMatcher = requireNonNull( urlMatcher );
    this.bodyParts
        = ofNullable( bodyParts )
        .map( Arrays::stream )
        .orElseGet( Stream::empty )
        .filter( StringUtils::isNotBlank )
        .collect( collectingAndThen( toList(), Collections::unmodifiableList ) );
  }

  @Override
  public RequestPatternBuilder build( final Map<String, Object> context )
  {
    final UserAccountsBean user = (UserAccountsBean)context.get( USER_KEY );

    final RequestPatternBuilder requestPatternBuilder
        = newRequestPattern( method, urlMatcher )
        .withRequestBody( containing( user.getAccountNumber() ) );

    for( final String bodyPart : bodyParts )
    {
      requestPatternBuilder.withRequestBody( containing( bodyPart ) );
    }

    return requestPatternBuilder;
  }
}
