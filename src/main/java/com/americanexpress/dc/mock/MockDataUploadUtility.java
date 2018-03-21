/**
 *
 */
package com.americanexpress.dc.mock;


import com.americanexpress.as.schemas.fsresponseservice._2.DomainFaultException;
import com.americanexpress.as.schemas.fsresponseservice._2.SystemFaultException;
import com.americanexpress.dc.bean.DetokenizerResponse;
import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.config.MockConfigUtil;
import com.americanexpress.dc.config.MongoConfiguration;
import com.americanexpress.dc.util.App;
import com.americanexpress.dc.util.Constants;
import com.americanexpress.dc.util.DetokenizerRequest;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.Document;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * {@link MockDataUploadUtility} class is used to load the mock data in central
 * mongo store.
 *
 * @author zselvad
 */
public class MockDataUploadUtility
{

  @Mock
  HttpServletRequest request;
  /**
   * @param args
   */

  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String CORRELATION_ID_PARAM = "correlation_id";
  private static String uploadFlag = "N";

  public static void main( String[] args ) throws IOException, SystemFaultException, DomainFaultException
  {

    // loading the collection names and BE endpoints to load the collections
    App.ConfigProperties.loadPropertyFile( "config_upload.properties" );
    Set<Entry<Object, Object>> entries = App.ConfigProperties.getPropertyEntrySet();
    MockDataUploadUtility mockDataUploadUtility = new MockDataUploadUtility();

    // Loading Testdata sheet
    String workbook = Constants.TEST_DATA_SHEET;
    String sheetName = Constants.TEST_DATA_SHEET_NAME;
    // Loading the member data
    List<UserAccountsBean> accountsBeans = mockDataUploadUtility.getAccounts( workbook, sheetName );

    // Loading the all other collections
    MongoDatabase db = MongoConfiguration.getDatabase();
    try
    {
      CollectionUploadUtil.uploadBackendDataCollectionwise( entries, mockDataUploadUtility, db, accountsBeans, uploadFlag );
      //mockDataUploadUtility.deTokenize(db);
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }


  public List<UserAccountsBean> getAccounts( String testDataSheet, String sheetname ) throws IOException
  {

    System.setProperty( "spring.profiles.active", "E1_QA" );
    System.setProperty( "Config.market", "US" );

    MockitoAnnotations.initMocks( this );
    List<UserAccountsBean> accountsBeans = new ArrayList<>();
    UserAccountsBean userAccountsBean = new UserAccountsBean();

    try
    {

      FileInputStream fis = new FileInputStream( testDataSheet );
      Workbook wb = new HSSFWorkbook( fis );

      org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet( sheetname );
      System.out.println( "NoOfRows >>" + sheet.getPhysicalNumberOfRows() );

      // suppose your formula is in B3
      for( int i = 0; i < sheet.getPhysicalNumberOfRows(); i++ )
      {
        Row row = sheet.getRow( i );
        if( row != null )
        {
          Cell cell = row.getCell( 0 );

          CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
          if( cell != null )
          {
            switch( cell.getCellType() )
            {
              case Cell.CELL_TYPE_BOOLEAN:
                System.out.println( cell.getBooleanCellValue() );
                break;
              case Cell.CELL_TYPE_NUMERIC:

                collectionUploadUtil.uploadAccountsData( cell, row, "numeric", accountsBeans, "Other", userAccountsBean );
                uploadFlag = "Y";
                break;
              case Cell.CELL_TYPE_STRING:

                collectionUploadUtil.uploadAccountsData( cell, row, "String", accountsBeans, "Other", userAccountsBean );
                uploadFlag = "Y";
                break;
              case Cell.CELL_TYPE_BLANK:
                break;
              case Cell.CELL_TYPE_ERROR:
                System.out.println( cell.getErrorCellValue() );
                break;

              case Cell.CELL_TYPE_FORMULA:
                break;
            }
          }
        }
        else
        {

        }
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
    return accountsBeans;
  }

  // Method to detokenize the account numbers

  public void deTokenize( final MongoDatabase db )
  {

    FindIterable<Document> iterable = db.getCollection( "member" ).find();

    iterable.forEach( new Block<Document>()
    {
      @Override
      public void apply( Document document )
      {

        MockConfigUtil configUtil = new MockConfigUtil();

        List<DetokenizerRequest> requestList = configUtil
            .buildRequest( document.get( "account_token" ).toString() );
        Gson gson = new GsonBuilder().setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES )
                                     .create();
        String request = gson.toJson( requestList );
        // System.out.println("Request ::" + request);
        RestServiceInvoker invoker = new RestServiceInvoker();
        String detokenizerEndpointUrl = "https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/tokenizer/v1/detokenize";
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put( CLIENT_ID_PARAM, "eStatement" );
        headers.put( CORRELATION_ID_PARAM, "test" );
        headers.put( CONTENT_TYPE, APPLICATION_JSON );

        System.out.println( "URL::" + detokenizerEndpointUrl );
        Response response = invoker.post( detokenizerEndpointUrl, null, null, headers, request );

        String responseContent = response.readEntity( String.class );
        System.out.println( "Response from TokenService :" + responseContent );
        List<DetokenizerResponse> detokenizerResponse = gson.fromJson( responseContent,
                                                                       new TypeToken<List<DetokenizerResponse>>()
                                                                       {
                                                                       }.getType() );

        String accountNumber = configUtil.getAccountNumberFromResponse( detokenizerResponse );
        // System.out.println("Acc # >>"+accountNumber);

        Document document1 = new Document();
        document1.put( "account_number", accountNumber );

        if( null != document.get( "userId" ).toString() )
        {
          document1.put( "userId", document.get( "userId" ).toString() );
        }
        if( null != document.get( "privateGuid" ).toString() )
        {
          document1.put( "privateGuid", document.get( "privateGuid" ).toString() );
        }
        document1.put( "response", responseContent );

        if( null != document.get( "account_token" ).toString() )
        {
          document1.put( "account_token", document.get( "account_token" ).toString() );
        }
        db.getCollection( "accounts_mapper" ).insertOne( document1 );
      }
    } );
  }

  public void loadInit() throws IOException
  {
    System.setProperty( "spring.profiles.active", "E1_QA" );
    System.setProperty( "Config.market", "US" );
    // App.ConfigProperties.loadPropertyFile("config_MOCK.properties");
    MockitoAnnotations.initMocks( this );
  }
}
