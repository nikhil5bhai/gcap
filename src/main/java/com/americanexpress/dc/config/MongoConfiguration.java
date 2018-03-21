package com.americanexpress.dc.config;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.List;

import static com.mongodb.MongoCredential.createCredential;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;


public class MongoConfiguration
{

  private static CPSFileResolver cpsFileResolver;

  private static class MongoClientHolder
  {

    static final MongoClient CLIENT;

    static
    {
      final List<ServerAddress> seeds
          = asList
          (
              new ServerAddress( "lpdwd550.phx.aexp.com", 27017 ),
              new ServerAddress( "lpdwd551.phx.aexp.com", 27017 ),
              new ServerAddress( "lpdwd549.phx.aexp.com", 27017 )
          );

      System.setProperty( "spring.profiles.active", "E1_QA" );
      System.setProperty( "Config.market", "US" );
      cpsFileResolver = new CPSFileResolver();
      String user = cpsFileResolver.getCps().getProperty( "MOCK_TRUSTED_ID" );
      String password = cpsFileResolver.getCps().getProperty( "MOCK_TRUSTED_PSWD" );

      final List<MongoCredential> mongoCredentials
          = singletonList
          (
              createCredential( user, "APPX_API_MOCK_DB", password.toCharArray() )
          );

      CLIENT = new MongoClient( seeds, mongoCredentials );
    }
  }

  /**
   * Gets the MongoDB Client
   *
   * @return the MongoDB Client instance
   */
  public static MongoClient getClient()
  {
    return MongoClientHolder.CLIENT;
  }

  /**
   * Gets the MongoDB Database
   *
   * @return the MongoDB 'APPX_API_MOCK_DB' Database instance
   */
  public static MongoDatabase getDatabase()
  {
    return MongoClientHolder.CLIENT.getDatabase( "APPX_API_MOCK_DB" );
  }
}