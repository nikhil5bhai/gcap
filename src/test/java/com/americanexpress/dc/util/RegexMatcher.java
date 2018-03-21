package com.americanexpress.dc.util;


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** @author Richard Wilson */
public class RegexMatcher extends BaseMatcher<Object>
{

  public static final Matches MATCHES = new Matches();
  public static final Finds FINDS = new Finds();

  private final String regex;
  private final int flags;
  private final Tester tester;

  public RegexMatcher( final String regex )
  {
    this( regex, 0 );
  }

  public RegexMatcher( final String regex, final int flags )
  {
    this( regex, flags, MATCHES );
  }

  public RegexMatcher( final String regex, final Tester tester )
  {
    this( regex, 0, tester );
  }

  public RegexMatcher( final String regex, final int flags, final Tester tester )
  {
    this.regex = regex;
    this.flags = flags;
    this.tester = tester;
  }

  @Override
  public boolean matches( final Object o )
  {
    if( o != null )
    {
      return tester.test( Pattern.compile( regex, flags ).matcher( String.valueOf( o ) ) );
    }
    return false;
  }

  public void describeTo( final Description description )
  {
    description.appendText( "matches regex=" ).appendText( regex );
  }

  public static RegexMatcher matches( final String regex )
  {
    return new RegexMatcher( regex );
  }

  public static RegexMatcher matchesRegex( final String regex )
  {
    return new RegexMatcher( regex );
  }

  public static RegexMatcher matchesRegex( final String regex, final int flags )
  {
    return new RegexMatcher( regex, flags );
  }

  public static RegexMatcher finds( final String regex )
  {
    return new RegexMatcher( regex, FINDS );
  }

  public static RegexMatcher findsRegex( final String regex )
  {
    return new RegexMatcher( regex, FINDS );
  }

  public static RegexMatcher findsRegex( final String regex, final int flags )
  {
    return new RegexMatcher( regex, flags, FINDS );
  }

  public static RegexMatcher test( final String regex, final Tester tester )
  {
    return new RegexMatcher( regex, tester );
  }

  public static RegexMatcher testRegex( final String regex, final Tester tester )
  {
    return new RegexMatcher( regex, tester );
  }

  public static RegexMatcher testRegex( final String regex, final int flags, final Tester tester )
  {
    return new RegexMatcher( regex, flags, tester );
  }

  public interface Tester
  {

    Boolean test( Matcher matcher );
  }

  public static class Matches implements Tester
  {

    @Override
    public Boolean test( final Matcher matcher )
    {
      return matcher.matches();
    }
  }

  public static class Finds implements Tester
  {

    @Override
    public Boolean test( final Matcher matcher )
    {
      return matcher.find();
    }
  }
}