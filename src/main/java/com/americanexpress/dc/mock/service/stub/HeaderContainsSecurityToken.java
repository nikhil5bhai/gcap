package com.americanexpress.dc.mock.service.stub;


import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.mock.service.spi.RequestStubBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static com.americanexpress.dc.util.Constants.USER_KEY;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;


/** @author Richard Wilson */
public class HeaderContainsSecurityToken implements RequestStubBuilder
{

  protected final RequestMethod method;
  protected final UrlPathPattern urlMatcher;
  protected final Map<String, StringValuePattern> queryMap;

  public HeaderContainsSecurityToken
      (
          final RequestMethod method,
          final UrlPathPattern urlMatcher
      )
  {
    this( method, urlMatcher, null );
  }

  public HeaderContainsSecurityToken
      (
          final RequestMethod method,
          final UrlPathPattern urlMatcher,
          final Map<String, StringValuePattern> queryMap
      )
  {
    this.method = requireNonNull( method );
    this.urlMatcher = requireNonNull( urlMatcher );
    this.queryMap
        = ofNullable( queryMap )
        .orElseGet( Collections::emptyMap )
        .entrySet()
        .stream()
        .filter( validEntry() )
        .collect( toMap( Entry::getKey, Entry::getValue ) );
  }

  private Predicate<Entry<String, StringValuePattern>> validEntry()
  {
    return entry -> entry.getKey() != null && entry.getValue() != null;
  }

  @Override
  public RequestPatternBuilder build( final Map<String, Object> context )
  {
    final UserAccountsBean user = (UserAccountsBean)context.get( USER_KEY );

    final RequestPatternBuilder requestPatternBuilder
        = newRequestPattern( method, urlMatcher );

    final String publicGuid = user.getPublicGuid();
    requestPatternBuilder.withHeader( "security_token", containing( publicGuid ) );

    for( final Entry<String, StringValuePattern> entry : queryMap.entrySet() )
    {
      requestPatternBuilder.withQueryParam( entry.getKey(), entry.getValue() );
    }

    return requestPatternBuilder;
  }
}
