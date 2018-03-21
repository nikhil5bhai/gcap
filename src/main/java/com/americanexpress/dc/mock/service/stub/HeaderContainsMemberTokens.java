package com.americanexpress.dc.mock.service.stub;


import com.americanexpress.dc.bean.UserAccountsBean;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;

import java.util.List;
import java.util.Map;

import static com.americanexpress.dc.util.Constants.USER_KEY;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


/** @author Richard Wilson */
public class HeaderContainsMemberTokens extends HeaderContainsSecurityToken
{

  public static final String COMMA = ",";

  public HeaderContainsMemberTokens
      (
          final RequestMethod method,
          final UrlPathPattern urlMatcher
      )
  {
    super( method, urlMatcher );
  }

  public HeaderContainsMemberTokens
      (
          final RequestMethod method,
          final UrlPathPattern urlMatcher,
          final Map<String, StringValuePattern> queryMap
      )
  {
    super( method, urlMatcher, queryMap );
  }

  @Override
  public RequestPatternBuilder build( Map<String, Object> context )
  {
    final UserAccountsBean user = (UserAccountsBean)context.get( USER_KEY );

    final RequestPatternBuilder requestPatternBuilder = super.build( context );

    final List<String> accountTokens = user.getAccountTokens();
    if( isNotEmpty( accountTokens ) )
    {
      requestPatternBuilder.withHeader( "account_tokens", containing( COMMA ) );
    }
    else
    {
      requestPatternBuilder
          .withHeader( "account_tokens", containing( user.getAccountToken() ) );
    }

    return requestPatternBuilder;
  }
}
