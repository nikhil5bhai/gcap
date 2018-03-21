/*
* -------------------------------------------------------------------------
*
* (C) Copyright / American Express, Inc. All rights reserved.
* The contents of this file represent American Express trade secrets and
* are confidential. Use outside of American Express is prohibited and in
* violation of copyright law.
*
* -------------------------------------------------------------------------
*/
package com.americanexpress.dc.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;


public class App
{

  public static class ConfigProperties
  {

    static InputStream inputStream;
    static Properties prop = new Properties();
    static Properties uplaodProp = new Properties();
    static Properties queryProperty = new Properties();
    static Properties hdrProperty = new Properties();
    static Properties pathProperty = new Properties();
    static Properties serviceProperty = new Properties();
    static Properties uploadProperty = new Properties();
    static Properties mockProperty = new Properties();
    static Properties multiResourceProperty = new Properties();
    static ResourceBundle productsResourceBundle;

    public static void loadPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          prop.load( inputStream );
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }
    
    public static void loadMultiResourcePropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
        	multiResourceProperty.load( inputStream );
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }
    

    public static void loadServicePropertyFile( String propFileName ) throws IOException
    {
      try
      {
        //serviceProperty.clear();
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          serviceProperty.load( inputStream );
          System.out.println( "The KEYS ARE NOW" );
          for( Object key : serviceProperty.keySet() )
          {
            System.out.println( key.toString() );
          }
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static void loadUploadServicePropertyFile( String propFileName ) throws IOException
    {
      try
      {
        //serviceProperty.clear();
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          uplaodProp.load( inputStream );
          System.out.println( "The KEYS ARE NOW" );
          for( Object key : uplaodProp.keySet() )
          {
            System.out.println( key.toString() );
          }
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static void loadUploadPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        //serviceProperty.clear();
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          uploadProperty.load( inputStream );
          System.out.println( "The KEYS ARE NOW" );
          for( Object key : uploadProperty.keySet() )
          {
            System.out.println( key.toString() );
          }
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static void loadMockPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          mockProperty.load( inputStream );
          System.out.println( "The KEYS ARE NOW" );
          for( Object key : mockProperty.keySet() )
          {
            System.out.println( key.toString() );
          }
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static void loadQueryPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          queryProperty.load( inputStream );
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }


    public static void loadHeaderPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          hdrProperty.load( inputStream );
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static void loadPathPropertyFile( String propFileName ) throws IOException
    {
      try
      {
        System.out.println( "property file >>" + propFileName );
        inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream( propFileName );
        if( inputStream != null )
        {
          pathProperty.load( inputStream );
        }
        else
        {
          throw new FileNotFoundException( "property file '" + propFileName + "' not found " );
        }
      }
      catch( Exception e )
      {
        System.out.println( "Exception: " + e );
      }
      finally
      {
        inputStream.close();
      }
    }

    public static String getPropertyValue( String key )
    {
      return prop.getProperty( key );
    }

    public static Properties getPropertyKeyValue()
    {
      return prop;
    }
    public static Properties getMultiResourcePropertyKeyValue()
    {
      return multiResourceProperty;
    }

    public static void setPropertyValue( String key, String value )
    {
      prop.setProperty( key, value );
    }

    public static List<String> getPropertyValuesWithPrefix( String prefix )
    {

      List<String> matchingProperties = new ArrayList<String>();
      Enumeration<?> en = prop.propertyNames();
      while( en.hasMoreElements() )
      {
        String propName = (String)en.nextElement();
        String propValue = prop.getProperty( propName );

        if( propName.startsWith( prefix ) )
        {
          matchingProperties.add( propValue );
        }
      }
      return matchingProperties;
    }

    public static Set<Entry<Object, Object>> getPropertyEntrySet()
    {
      return prop.entrySet();
    }

    public static Properties getProperties()
    {
      return prop;
    }

    public static Set<Entry<Object, Object>> getConfigPropertyEntrySet()
    {
      return queryProperty.entrySet();
    }

    public static Properties getQueryProperties()
    {
      return queryProperty;
    }

    public static Set<Entry<Object, Object>> getHeaderPropertyEntrySet()
    {
      return hdrProperty.entrySet();
    }

    public static Properties getHeaderProperties()
    {
      return hdrProperty;
    }

    public static Set<Entry<Object, Object>> getPathPropertyEntrySet()
    {
      return pathProperty.entrySet();
    }

    public static Properties getPathProperties()
    {
      return pathProperty;
    }

    public static Properties getServicePropertyEntrySet()
    {
      return serviceProperty;
    }

    public static Properties getServiceProperties()
    {
      return serviceProperty;
    }

    public static Properties getSoapUploadPropertyEntrySet()
    {
      return uplaodProp;
    }

    public static Set<Entry<Object, Object>> getMockPropertyEntrySet()
    {
      return mockProperty.entrySet();
    }

    public static Properties getUploadPropertyEntrySet()
    {
      return uploadProperty;
    }

    public static Properties getUploadProperties()
    {
      return uploadProperty;
    }
  }
}