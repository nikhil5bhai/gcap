package com.americanexpress.dc.mock;


import com.americanexpress.as.cs.sap.SecurityAccessPoint;
import com.americanexpress.as.cs.sap.exception.SAPException;
import com.americanexpress.as.cs.sap.valuebean.ApplicationMap;
import com.americanexpress.as.cs.sap.valuebean.SecurityGUIDs;
import com.americanexpress.as.cs.sap.valuebean.SecurityServices;
import com.americanexpress.as.myca.web.pl.auth.SecurityAccess;
import com.americanexpress.as.sfwk.shr.ServiceUtil;
import com.americanexpress.dc.bean.BackendAccountEntity;
import com.americanexpress.dc.bean.Identifiers;
import com.americanexpress.dc.bean.SupplementaryAccountElement;
import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.config.MockConfigUtil;
import com.americanexpress.dc.config.MongoConfiguration;
import com.americanexpress.dc.util.*;
import com.americanexpress.wss.shr.authorization.token.NoAttributeFoundException;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import com.americanexpress.wss.shr.authorization.token.TokenException;
import com.google.common.collect.BiMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.americanexpress.as.cs.shr.logging.util.CorrelationIdHolder.createCorrelationId;
import static com.americanexpress.dc.builder.spi.RequestBuilderService.getBody;
import static com.americanexpress.dc.mock.RestEndpointType.*;
import static com.americanexpress.dc.splitter.spi.ResponseSplitterService.getResources;
import static com.americanexpress.dc.util.ApigeeUtil.*;
import static com.americanexpress.dc.util.App.ConfigProperties.*;
import static com.americanexpress.dc.util.Constants.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.*;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.*;


public class CollectionUploadUtil
{

  private static final Set<String> SUPPORTED_HTTP_METHOD_SET
      = unmodifiableSet( new TreeSet<>( asList( GET, POST, DELETE, PUT ) ) );
  private static final String SUPPORTED_HTTP_METHODS_FORMAT = "(?i)_(%s)$";
  private static final String REGEX_LOGICAL_OR_OPERATOR = "|";
  private static final Pattern SUPPORTED_HTTP_METHODS_PATTERN
      = compile
      (
          format
              (
                  SUPPORTED_HTTP_METHODS_FORMAT,
                  join
                      (
                          SUPPORTED_HTTP_METHOD_SET,
                          REGEX_LOGICAL_OR_OPERATOR
                      )
              )
      );
  public static final String APIGEE_HMAC_SUFFIX = "_apigee_hmac";
  public static final String APIGEE_OAUTH_SUFFIX = "_apigee_oauth";
  private static final Set<RestEndpointType> SKIP_ENDPOINT_TYPES
      = unmodifiableSet( EnumSet.of( COLLECTION, STATIC_COLLECTION ) );

  @Mock
  HttpServletRequest request;

  public static final String SECURITY_TOKEN_PARAM = "security_token";
  public static final String ACCOUNT_TOKENS_PARAM = "account_tokens";
  public static final String CLIENT_ID_PARAM = "client_id";
  public static final String CLIENT_ID_VAL = "AmexAPI";
  public static final String CORRELATION_ID_PARAM = "correlation_id";
  public static final String ACCOUNT_NUMBER_FIELD = "AccountNumber";

  public static final String ADDRESS_REQ_AUTH_HEADER = "Authorization";
  public static final String AIM_ID = "200005750";
  public static final String APPLICATION_ID = "OneAmex";

  public static final String HEADER_SECURITY_TOKEN = "AmEx-Security-Token";
  public static final String HEADER_SYSTEM_ID = "AmEx-System-ID";
  public static final String HEADER_CLIENT_ID = "AmEx-Client-ID";
  public static final String HEADER_CORRELATION_ID = "AmEx-Correlation-ID";
  public static final String HEADER_UNIQUE_REQUEST_ID = "x-request-unique-id";

  /**
   * Method to upload the backend data to collections
   *
   * @param accountsBeans
   * @param uploadFlag
   *
   * @throws Exception
   */

  public static void uploadBackendDataCollectionwise( Set<Entry<Object, Object>> entries,
                                                      MockDataUploadUtility mockDataUploadUtility, MongoDatabase db, List<UserAccountsBean> accountsBeans,
                                                      String uploadFlag ) throws Exception
  {
    App.ConfigProperties.loadUploadPropertyFile( "config_upload.properties" );
    Properties uploadProperty = App.ConfigProperties.getUploadPropertyEntrySet();

    for( final String setElement : uploadProperty.stringPropertyNames() )
    {
      // Loading the soap services.
      if( setElement.contains( "_soap" ) )
      {
        String collectionName = setElement.replace( "_soap", "" );

        SoapRequestBuilderFactory soapRequestBuilderFactory = new SoapRequestBuilderFactory();
        SoapRequestBuilder requestBuilder = new SoapRequestBuilder( soapRequestBuilderFactory.getSoapImplementation( collectionName ) );

        requestBuilder.callSoapService( db, collectionName, uploadProperty.getProperty( setElement ), accountsBeans, uploadFlag, "" );
      }
      else
      {
        // Loading the REST Apis other than member.
        CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
        if( !"member".equalsIgnoreCase( setElement ) )
        {
          collectionUploadUtil.callRestApi( db, setElement, uploadProperty.getProperty( setElement ), accountsBeans, uploadFlag );
        }
      }
    }
  }

  /**
   * SecurityToken generator API
   *
   * @param userId
   *
   * @return
   *
   * @throws Exception
   */
  public SecurityToken generateClaims( String userId, String env )
  {
    try
    {
      System.setProperty( "spring.profiles.active", env );
      System.setProperty( "Config.market", "US" );

      MockitoAnnotations.initMocks( this );

      SecurityToken securityToken = SecurityAccessPoint.getInstance().logon( userId, "flower1", SecurityServices.MYCA, ApplicationMap.CAZM );
      SecurityGUIDs guids = SecurityAccessPoint.getInstance().getGUIDs( securityToken, SecurityServices.MYCA, ApplicationMap.CAZM );
      com.americanexpress.as.security.chkreg.shr.SecurityGUIDs guidsSecurity = new com.americanexpress.as.security.chkreg.shr.SecurityGUIDs();
      guidsSecurity.setCSRPrivateGUID( guids.getCsrPrivateGUID() );
      guidsSecurity.setCSRPublicGUID( guids.getCsrPublicGUID() );
      guidsSecurity.setPublicGUID( guids.getPublicGUIDs() );
      guidsSecurity.setPrivateGUID( guids.getPrivateGUIDs() );
      SecurityAccess.getInstance().addSecurityTokenToRequest( request, guidsSecurity, securityToken );
      return securityToken;
    }
    catch( final SAPException exc )
    {
      final String message = "Exception while creating claims " + exc.getMessage();
      System.out.println( message );
      exc.printStackTrace( System.out );
      throw new IllegalStateException( message, exc );
    }
  }

  /**
   * Method : Call account service and load member data
   *
   * @param cell
   * @param row
   * @param cellFormat
   * @param accountsBeans
   * @param serviceType
   * @param userAccountsBean
   *
   * @return
   */
  public String uploadAccountsData( Cell cell, Row row, String cellFormat, List<UserAccountsBean> accountsBeans,
                                    String serviceType, UserAccountsBean userAccountsBean )
  {

    String uploadFlag = "N";

    String racfId = null;
    String region = null;
    String userId = null;
    MongoDatabase db = MongoConfiguration.getDatabase();

    String accountServiceEndpointUrl = "https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/member/v1/accounts";
    String privateGuid = null;
    String publicGuid = null;

    RestServiceInvoker invoker = new RestServiceInvoker();
    HashMap<String, Object> headers = new HashMap<String, Object>();

    String accountNumber = null;
    if( cellFormat.equalsIgnoreCase( "numeric" ) )
    {
      accountNumber = NumberToTextConverter.toText( cell.getNumericCellValue() ).trim();
      uploadFlag = "Y";
    }
    else if( cellFormat.equalsIgnoreCase( "String" ) )
    {
      accountNumber = cell.getStringCellValue().toString().trim();
      uploadFlag = "Y";
    }

		/*UserAccountsBean userAccountsBean = new UserAccountsBean();
    userAccountsBean.setAccountNumber(accountNumber);
		cell = row.getCell(1);

		userId = cell.getStringCellValue().toString().trim();
		userAccountsBean.setUserId(userId);*/
    cell = row.getCell( 0 );
    userId = cell.getStringCellValue().toString().trim();
    // Adding the headers.
    try
    {
      SecurityToken securityToken = null;
      CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
      securityToken = collectionUploadUtil.generateClaims( userId, "E2_PROD"  );
      privateGuid = GenerteToken.getSecurityGuid( securityToken ).getPrivateGUIDs();
      publicGuid = GenerteToken.getSecurityGuid( securityToken ).getPublicGUIDs();

      System.out.println( "publicGuid >>" + publicGuid );

      headers.put( CLIENT_ID_PARAM, CLIENT_ID_VAL );
      headers.put( SECURITY_TOKEN_PARAM, securityToken.asXML() );
      headers.put( CLIENT_ID_PARAM, "AmexAPI" );
      headers.put( CORRELATION_ID_PARAM, RandomUtils.nextDouble( 0.0, 1.0 ) );
      headers.put( CONTENT_TYPE, APPLICATION_JSON );
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    Response response = invoker.get( accountServiceEndpointUrl, null, null, headers );

    System.out.println( "THE RESPONSE STATUS IS " + response.getStatus() );

    if( response.getStatus() == 200 || response.getStatus() == 206 )
    {
      String responseContent = response.readEntity( String.class );
      System.out.println( "Response from cards ***** :" + responseContent );

      Gson gson = new GsonBuilder().setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES ).create();

      List<BackendAccountEntity> backendAccountEntityList = gson.fromJson( responseContent,
                                                                           new TypeToken<List<BackendAccountEntity>>()
                                                                           {
                                                                           }.getType() );

      //userAccountsBean.setBackendAccountEntities(backendAccountEntityList);


      loadMemberdetails( backendAccountEntityList, userAccountsBean, accountsBeans, userId,
                         accountNumber, privateGuid, responseContent, db, publicGuid );
    }

    return uploadFlag;
  }

  /**
   * Method -Load member details in Mongo
   *
   * @param backendAccountEntityList
   * @param userAccountsBean
   * @param accountsBeans
   * @param userId
   * @param accountNumber
   * @param privateGuid
   * @param responseContent
   * @param db
   * @param publicGuid
   */

  private void loadMemberdetails( List<BackendAccountEntity> backendAccountEntityList,
                                  UserAccountsBean userAccountsBean, List<UserAccountsBean> accountsBeans, String userId,
                                  String accountNumber, String privateGuid, String responseContent, MongoDatabase db, String publicGuid )
  {
    TokenServiceFacade tokenServiceFacade = new TokenServiceAdapter();
    List<String> acctNbrs = new ArrayList<>();
    List<String> encryptedAccountNumber;
    List<String> accountNumbers = new ArrayList<>();

    List<UserAccountsBean> suppUserAccountsBeans = null;
    UserAccountsBean suppUserAccountsBean = null;
    userAccountsBean = new UserAccountsBean();
    for( BackendAccountEntity backendAccountEntity : backendAccountEntityList )
    {


      userAccountsBean.setUserId( userId );

      if( null != backendAccountEntity.getSupplementaryAccounts() && backendAccountEntity.getSupplementaryAccounts().size() > 0 )
      {
        suppUserAccountsBeans = new ArrayList<>();

        for( SupplementaryAccountElement supplementaryAccountElement : backendAccountEntity.getSupplementaryAccounts() )
        {
          suppUserAccountsBean = new UserAccountsBean();
          suppUserAccountsBean.setAccountToken( supplementaryAccountElement.getAccountToken() );
          suppUserAccountsBeans.add( suppUserAccountsBean );

          encryptedAccountNumber = new ArrayList<>();
          encryptedAccountNumber.add( supplementaryAccountElement.getAccountToken() );
          BiMap<String, String> accountsMap = tokenServiceFacade.getAccountNumbers( encryptedAccountNumber, "test1234" );
          suppUserAccountsBean.setAccountNumber( accountsMap.get( supplementaryAccountElement.getAccountToken() ) );
          accountNumbers.add( accountsMap.get( backendAccountEntity.getAccountToken() ) );
          //suppUserAccountsBean.setAccountNumbers(accountNumbers);
          suppUserAccountsBeans.add( suppUserAccountsBean );
        }
        userAccountsBean.setSuppUserAccountsBeans( suppUserAccountsBeans );
      }

      System.out.println( "THE ACCOUNT TOKEN IS " + backendAccountEntity.getAccountToken() );
      Document document = new Document();
      document.put( "account_number", "XXXX-XXXXX-XXXXX" );
      if( null != backendAccountEntity.getProduct() && null != backendAccountEntity.getProduct().getDigitalInfo() )
      {
        document.put( "benefit_code", backendAccountEntity.getProduct().getDigitalInfo().getPctCode() );
        userAccountsBean.setDigitalAssetId( backendAccountEntity.getProduct().getDigitalInfo().getDigitalAssetId() );
      }
      if( null != backendAccountEntity.getProduct() && null != backendAccountEntity.getProduct().getDigitalInfo() )
      {
        document.put( "product_code", backendAccountEntity.getProduct().getDigitalInfo().getProductCode() );
        userAccountsBean.setProductCode( backendAccountEntity.getProduct().getDigitalInfo().getProductCode() );
      }
      if( null != backendAccountEntity.getProduct() && null != backendAccountEntity.getProduct().getDigitalInfo() )
      {
        document.put( "product_desc", backendAccountEntity.getProduct().getDigitalInfo().getProductDesc() );
      }
      if( null != backendAccountEntity.getProduct() && null != backendAccountEntity.getProduct().getCardTypes() )
      {
        document.put( "lob", backendAccountEntity.getProduct().getCardTypes().getLineOfBusinessType() );
      }
      if
          (
          null != backendAccountEntity.getHolder()
          &&
          null != backendAccountEntity.getHolder().getLocalizationPreferences()
          &&
          null != backendAccountEntity.getHolder().getLocalizationPreferences().getHomeCountryLocale()
          )
      {
        document.put( "market",
                      backendAccountEntity.getHolder().getLocalizationPreferences().getHomeCountryLocale() );
        userAccountsBean.setLocale( backendAccountEntity.getHolder().getLocalizationPreferences().getHomeCountryLocale() );
      }
      if
          (
          null != backendAccountEntity.getHolder()
          &&
          null != backendAccountEntity.getHolder().getLocalizationPreferences()
          )
      {
        document.put( "market_locale",
                      backendAccountEntity.getHolder().getLocalizationPreferences().getCurrencyLocale() );
      }
      if( null != backendAccountEntity.getProduct() && null != backendAccountEntity.getProduct().getCardTypes() )
      {
        document.put( "payment_type", backendAccountEntity.getProduct().getCardTypes().getPaymentType() );
      }
      if( null != backendAccountEntity.getPlatform() && null != backendAccountEntity.getPlatform().getMarketCode() )
      {
        document.put( "market_code", backendAccountEntity.getPlatform().getMarketCode() );
        userAccountsBean.setMarketCode( backendAccountEntity.getPlatform().getMarketCode());
      }
      userAccountsBean.setPrivateGuid( privateGuid );
      userAccountsBean.setPublicGuid( publicGuid );
      document.put( "privateGuid", privateGuid );
      document.put( "publicGuid", publicGuid );

      document.put( "userId", userId );
      document.put( "account_token", backendAccountEntity.getAccountToken() );

      final Identifiers identifiers = backendAccountEntity.getIdentifiers();
      if( null != identifiers && identifiers.getIsBasic() == true )
      {
        userAccountsBean.setAccountToken( backendAccountEntity.getAccountToken() );

        acctNbrs.add( backendAccountEntity.getAccountToken() );

        userAccountsBean.setRelationship( "Basic" );
        encryptedAccountNumber = new ArrayList<>();
        encryptedAccountNumber.add( backendAccountEntity.getAccountToken() );
        BiMap<String, String> accountsMap = tokenServiceFacade.getAccountNumbers( encryptedAccountNumber, "test1234" );
        userAccountsBean.setAccountNumber( accountsMap.get( backendAccountEntity.getAccountToken() ) );
        accountNumbers.add( accountsMap.get( backendAccountEntity.getAccountToken() ) );
      }
      else
      {
        userAccountsBean.setAccountToken( backendAccountEntity.getAccountToken() );
        acctNbrs.add( backendAccountEntity.getAccountToken() );
        userAccountsBean.setRelationship( "Supp" );

        encryptedAccountNumber = new ArrayList<>();
        encryptedAccountNumber.add( backendAccountEntity.getAccountToken() );
        tokenServiceFacade.getAccountNumbers( encryptedAccountNumber, "test1234" );

        BiMap<String, String> accountsMap = tokenServiceFacade.getAccountNumbers( encryptedAccountNumber, "test1234" );
        userAccountsBean.setAccountNumber( accountsMap.get( backendAccountEntity.getAccountToken() ) );
        accountNumbers.add( accountsMap.get( backendAccountEntity.getAccountToken() ) );
      }

      document.put( "response", responseContent );

      if( null != backendAccountEntity.getHolder() && null != backendAccountEntity.getHolder().getLocalizationPreferences().getLanguagePreference() )
      {
        userAccountsBean.setLanguagePreference(
            backendAccountEntity.getHolder().getLocalizationPreferences().getLanguagePreference() );
        System.out.println( "The language is "
                            + backendAccountEntity.getHolder().getLocalizationPreferences().getLanguagePreference() );
      }
      userAccountsBean.setAccountTokens( acctNbrs );
      userAccountsBean.setAccountNumbers( accountNumbers );
      accountsBeans.add( userAccountsBean );

      db.getCollection( "member" ).replaceOne( new Document( "userId", userId ), document,
                                               new UpdateOptions().upsert( true ) );
    }
  }

  /**
   * Method call to load data to Mongo through REST API
   *
   * @param db
   * @param serviceCallName
   * @param endpoint
   * @param accountsBeans
   * @param uploadFlag
   *
   * @throws IOException
   */

  public void callRestApi( final MongoDatabase db, final String serviceCallName, final String endpoint,
                           List<UserAccountsBean> accountsBeans, String uploadFlag ) throws IOException
  {/*
    if( BooleanUtils.toBoolean( uploadFlag ) )
    {
      final Collection<UserAccountsBean> userAccounts = emptyIfNull( accountsBeans );
      final Map<String, List<UserAccountsBean>> accountsByUserIdMap
          = userAccounts.stream().collect( groupingBy( UserAccountsBean::getUserId ) );

      loadMultiResourcePropertyFile( "config_multiaccounts_api.properties" );
      final Properties multiResourceProperties = getMultiResourcePropertyKeyValue();

      if( BooleanUtils.toBoolean( multiResourceProperties.getProperty( serviceCallName ) ) )
      {

       // MultiAccountsUploadUtil multiAccountsUploadUtil = new MultiAccountsUploadUtil();

        for( final UserAccountsBean userAccountsBean : userAccounts )
        {
          multiAccountsUploadUtil.doRestServiceCall
              (
                  userAccountsBean,
                  accountsByUserIdMap.get( userAccountsBean.getUserId() ),
                  serviceCallName,
                  endpoint,
                  db
              );
          break;
        }
      }

      final String baseServiceCallName = removeEndpointMarker( serviceCallName );
      final RestEndpointType endpointType
          = getRestEndpointType( serviceCallName, baseServiceCallName );
      if( !SKIP_ENDPOINT_TYPES.contains( endpointType ) )
      {
        for( final UserAccountsBean accountsBean : accountsBeans )
        {
          final List<UserAccountsBean> otherAccounts
              = accountsByUserIdMap.get( accountsBean.getUserId() );
          if( endpointType == RESOURCE )
          {
            doRestServiceCallResource
                (
                    accountsBean,
                    otherAccounts,
                    baseServiceCallName,
                    db
                );
          }
          else
          {
            doRestServiceCall
                (
                    accountsBean,
                    otherAccounts,
                    serviceCallName,
                    endpoint,
                    db
                );
          }
        }
      }
    }
    else
    {

      FindIterable<Document> iterable = db.getCollection( "member" ).find();
      final MockConfigUtil dataUploadUtil = new MockConfigUtil();

      iterable.forEach
          (
              new Block<Document>()
              {
                @Override
                public void apply( Document document )
                {
                  final UserAccountsBean userAccountsBean = new UserAccountsBean();
                  userAccountsBean.setUserId( document.get( "userId" ).toString() );
                  final String accountToken1 = document.get( "account_token" ).toString();
                  userAccountsBean.setAccountToken( accountToken1 );
                  userAccountsBean.setPrivateGuid( document.get( "privateGuid" ).toString().trim() );
                  userAccountsBean.setPublicGuid( document.get( "publicGuid" ).toString().trim() );
                  userAccountsBean
                      .setAccountNumber
                          (
                              dataUploadUtil.deTokenizeAccountToken( accountToken1 )
                          );
                  final String marketLocale = trim( document.getString( "market_locale" ) );

                  try
                  {
                    callRestService
                        (
                            userAccountsBean,
                            serviceCallName,
                            endpoint,
                            db,
                            marketLocale
                        );
                  }
                  catch( IOException e )
                  {
                    e.printStackTrace();
                  }
                }
              }
          );
    }
  */}

  private void doRestServiceCallResource
      (
          final UserAccountsBean accountsBean,
          final List<UserAccountsBean> otherAccounts,
          final String baseServiceCallName,
          final MongoDatabase db
      )
      throws IOException
  {
    final Properties uploadProperty = App.ConfigProperties.getUploadPropertyEntrySet();

    final Map<String, Object> serviceProperties
        = getServicePropertiesMap( accountsBean, otherAccounts );

    final Response response;

    final String staticCollectionName
        = STATIC_COLLECTION.prefix( baseServiceCallName );
    final List<String> staticCollection
        = ofNullable( uploadProperty.getProperty( staticCollectionName ) )
        .map( StringUtils::split )
        .map( Arrays::asList )
        .orElseGet( Collections::emptyList );
    final String collectionName = getDocumentCollectionName( baseServiceCallName );
    if( staticCollection.isEmpty() )
    {
      final String prefixedCollectionName = COLLECTION.prefix( collectionName );
      final Optional<String> foundCollectionServiceCallName
          = uploadProperty
          .stringPropertyNames()
          .stream()
          .filter( name -> startsWithIgnoreCase( name, prefixedCollectionName ) )
          .findFirst();
      if( foundCollectionServiceCallName.isPresent() )
      {
        final String collectionServiceCallName = foundCollectionServiceCallName.get();
        final String collectionEndpoint = uploadProperty.getProperty( collectionServiceCallName );
        response = getRestServiceResponse( collectionServiceCallName, collectionEndpoint, serviceProperties );
      }
      else
      {
        throw new IllegalStateException( format( "Missing prefix match for '%s'", prefixedCollectionName ) );
      }
    }
    else
    {
      response = Response.ok().entity( staticCollection ).build();
    }

    final String resourceServiceCallName
        = RESOURCE.prefix( baseServiceCallName );
    final String resourceEndpoint = uploadProperty.getProperty( resourceServiceCallName );
    final Collection<Map<String, Object>> resources = getResources( collectionName, response );

    for( final Map<String, Object> resource : resources )
    {
      doRestServiceCallResource
          (
              accountsBean,
              otherAccounts,
              resourceServiceCallName,
              resourceEndpoint,
              resource,
              db
          );
    }
  }

  private RestEndpointType getRestEndpointType
      (
          final String value
      )
  {
    return getRestEndpointType( value, removeEndpointMarker( value ) );
  }

  private RestEndpointType getRestEndpointType
      (
          final String original,
          final String unmarked
      )
  {
    return ofNullable( remove( original, unmarked ) )
        .map( RestEndpointType::fromString )
        .orElse( null );
  }

  private String removeEndpointMarker( final String value )
  {
    return removePattern( value, ENDPOINT_TYPE_MARKER );
  }

  /**
   * Method call to actual REST service and load data to Mongo through REST API
   *
   * @param userAccountsBean
   * @param collectionName
   * @param endpoint
   * @param db
   * @param marketLocale
   *
   * @throws IOException
   */
  private void callRestService
  (
      final UserAccountsBean userAccountsBean,
      final String collectionName,
      final String endpoint,
      final MongoDatabase db,
      final String marketLocale
  )
      throws IOException
  {

    SecurityToken securityToken = null;
    RestServiceInvoker restInvoker = new RestServiceInvoker();
    HashMap<String, String> queryParams = new HashMap<String, String>();
    HashMap<String, String> pathParams = new HashMap<String, String>();
    HashMap<String, Object> headers = new HashMap<String, Object>();

    // Adding the query params

    loadQueryPropertyFile( "config_query_params_upload.properties" );
    Set<Entry<Object, Object>> entries = getConfigPropertyEntrySet();

    if( entries.size() > 0 )
    {
      final Iterator<Entry<Object, Object>> it = entries.iterator();
      while( it.hasNext() )
      {
        final Entry<Object, Object> pair = it.next();
        final String key = pair.getKey().toString();
        if( key.contains( collectionName ) )
        {
          queryParams.put(
              key.substring( key.indexOf( "." ) + 1 ).trim(),
              pair.getValue().toString() );
        }
      }
    }

    try
    {

      final String userId = userAccountsBean.getUserId();
      final String accountToken = userAccountsBean.getAccountToken();
      final String accountNumber = userAccountsBean.getAccountNumber();
      final String privateGuid = userAccountsBean.getPrivateGuid();
      final String publicGuid = userAccountsBean.getPublicGuid();
      if( null != userId && "transactions".equalsIgnoreCase( collectionName ) )
      {

        securityToken = generateClaims( userId, "E2_PROD" );

        headers.put( CLIENT_ID_PARAM, CLIENT_ID_VAL );

        headers.put( CONTENT_TYPE, APPLICATION_JSON );
        headers.put( CORRELATION_ID_PARAM, RandomUtils.nextLong( 0, Long.MAX_VALUE ) );

        queryParams.put( "status", "pending" );

        System.out.println( "accountToken : " + accountToken );

        headers.put( SECURITY_TOKEN_PARAM, ServiceUtil.encodeSecurityToken( securityToken ) );
        headers.put( ACCOUNT_TOKENS_PARAM, accountToken );
        Response response1 = restInvoker.get( endpoint, pathParams, queryParams, headers );

        int responseStatus = response1.getStatus();
        System.out.println( "Txn pending Response status >>" + responseStatus );
        String responseContent1 = response1.readEntity( String.class );

        System.out.println( "Fins Transaction BE response >>" + responseContent1 );

        Document document2 = new Document();
        if( responseStatus == 200 )
        {
          document2.put( "userId", userId );
          document2.put( "account_token", accountToken );
          document2.put( "privateGuid", privateGuid );
          document2.put( "publicGuid", publicGuid );

          System.out.println( "Fins Transaction BE response >>" + responseContent1 );
          document2.put( "response", responseContent1 );
          if( null != responseContent1 )
          {

            db.getCollection( collectionName + "_pending" ).replaceOne( new Document( "userId", userId ), document2,
                                                                        new UpdateOptions().upsert( true ) );
          }
        }

        // transactions for posted
        HashMap<String, String> queryParams1 = new HashMap<String, String>();
        queryParams1.put( "status", "posted" );
        Response response2 = restInvoker.get( endpoint, pathParams, queryParams1, headers );
        int responseStatus2 = response2.getStatus();
        System.out.println( "Txn posted Response status >>" + responseStatus2 );
        if( responseStatus2 == 200 )
        {
          String responseContent2 = response2.readEntity( String.class );
          document2.put( "response", responseContent2 );
          if( null != responseContent2 )
          {

            db.getCollection( collectionName + "_posted" ).replaceOne( new Document( "userId", userId ), document2,
                                                                       new UpdateOptions().upsert( true ) );
          }
        }
      }
      else
      {
        securityToken = generateClaims( userId, "E2_PROD"  );

        headers.put( CLIENT_ID_PARAM, CLIENT_ID_VAL );

        headers.put( CORRELATION_ID_PARAM, DEFAULT_CORRELATION_ID );

        if( collectionName.contains( "loyalty" ) )
        {
          headers.put( "locale", marketLocale );
        }

        System.out.println( "accountNumber >>" + accountNumber );
        FieldSecurity fieldSecurity = new FieldSecurity( securityToken );
        String accountTokenLoyalty = fieldSecurity.encrypt( ACCOUNT_NUMBER_FIELD, accountNumber );

        headers.put( SECURITY_TOKEN_PARAM, ServiceUtil.encodeSecurityToken( securityToken ) );
        if( collectionName.contains( "loyalty" ) )
        {
          System.out.println( "accountToken : " + accountToken );
          headers.put( ACCOUNT_TOKENS_PARAM, accountTokenLoyalty );
        }
        else if( collectionName.contains( "paymentsummary" ) )
        {
          headers.put( ACCOUNT_TOKENS_PARAM, accountToken );
        }
        else
        {
          headers.put( ACCOUNT_TOKENS_PARAM, accountToken );
          headers.put( CONTENT_TYPE, APPLICATION_JSON );
        }
        Response response1 = restInvoker.get( endpoint, pathParams, queryParams, headers );

        Document document2 = new Document();
        document2.put( "userId", userId );
        if( collectionName.contains( "loyalty" ) )
        {
          document2.put( "account_token", accountTokenLoyalty );
        }
        else
        {
          document2.put( "account_token", accountToken );
        }

        document2.put( "privateGuid", privateGuid );
        document2.put( "publicGuid", publicGuid );
        int responseStatus = response1.getStatus();
        if( responseStatus == 200 )
        {
          String responseContent1 = response1.readEntity( String.class );

          System.out.println( " BE response >>" + responseContent1 );
          document2.put( "response", responseContent1 );
          if( null != responseContent1 )
          {
            db.getCollection( collectionName ).replaceOne( new Document( "userId", userId ), document2,
                                                           new UpdateOptions().upsert( true ) );
          }
        }
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }

  

  /**
   * Method call to actual REST service and load collection data to Mongo through REST API
   *
   * @param userAccountsBean
   * @param otherAccounts
   * @param serviceCallName
   * @param endpoint
   * @param db
   *
   * @throws IOException
   */
  private void doRestServiceCall
  (
      final UserAccountsBean userAccountsBean,
      final Collection<UserAccountsBean> otherAccounts,
      final String serviceCallName,
      final String endpoint,
      final MongoDatabase db
  )
      throws IOException
  {
    final Map<String, Object> serviceProperties = getServicePropertiesMap( userAccountsBean, otherAccounts );

    final Response response = getRestServiceResponse( serviceCallName, endpoint, serviceProperties );

    saveRestServiceResponse( db, serviceCallName, response, serviceProperties );
  }

  /**
   * Method call to actual REST service and load resource data to Mongo through REST API
   *
   * @param userAccountsBean
   * @param otherAccounts
   * @param serviceCallName
   * @param endpoint
   * @param resource
   * @param db
   *
   * @throws IOException
   */
  private void doRestServiceCallResource
  (
      final UserAccountsBean userAccountsBean,
      final Collection<UserAccountsBean> otherAccounts,
      final String serviceCallName,
      final String endpoint,
      final Map<String, Object> resource,
      final MongoDatabase db
  )
      throws IOException
  {
    final Map<String, Object> serviceProperties = getServicePropertiesMap( userAccountsBean, otherAccounts );

    serviceProperties.put( RESOURCE_KEY, resource );

    final Response response = getRestServiceResponse( serviceCallName, endpoint, serviceProperties );

    saveRestServiceResponse( db, serviceCallName, response, serviceProperties );
  }

  private String getDocumentCollectionName( final String resourceCallName )
  {
    final String invokeName = removeEnd( resourceCallName, APIGEE_HMAC_SUFFIX );
    return removeEndIgnoreCase( invokeName, getHttpMethodParts( invokeName ).poll() );
  }

  private Map<String, Object> getServicePropertiesMap
      (
          final UserAccountsBean userAccountsBean,
          final Collection<UserAccountsBean> otherAccounts
      )
  {
    final String userId = userAccountsBean.getUserId();
    final SecurityToken securityToken = generateClaims( userId, "E2_PROD" );
    try
    {
      final Map<String, Object> serviceProperties = new LinkedHashMap<>();
      serviceProperties.put( "security_token", securityToken );
      serviceProperties.put( "security_token_xml", securityToken.asXML() );
      serviceProperties.put( "blue_box_public", securityToken.getAttributeValue( "blueboxpublic" ) );
      serviceProperties.put( "encoded_security_token", ServiceUtil.encodeSecurityToken( securityToken ) );
      serviceProperties.put( "user_id", userId );
      serviceProperties.put( "private_guid", userAccountsBean.getPrivateGuid() );
      serviceProperties.put( "public_guid", userAccountsBean.getPublicGuid() );
      serviceProperties.put( "account_number", userAccountsBean.getAccountNumber() );
      serviceProperties.put( "account_token", userAccountsBean.getAccountToken() );
      serviceProperties.put( "digital_asset_id", userAccountsBean.getDigitalAssetId() );
      serviceProperties.put( "product_code", userAccountsBean.getProductCode() );
      serviceProperties.put("market_code",userAccountsBean.getMarketCode());
      serviceProperties.put("locale",userAccountsBean.getLocale());
      final Collection<UserAccountsBean> allOtherAccounts = emptyIfNull( otherAccounts );
      final Set<String> accountNumbers
          = allOtherAccounts
          .stream()
          .map( UserAccountsBean::getAccountNumber )
          .collect( toSet() );
      serviceProperties.put( "account_numbers", accountNumbers );
      final Set<String> accountTokens
          = allOtherAccounts
          .stream()
          .map( UserAccountsBean::getAccountToken )
          .collect( toSet() );
      serviceProperties.put( "account_tokens", accountTokens );
      ofNullable( userAccountsBean.getLanguagePreference() )
          .map( Locale::forLanguageTag )
          .filter( locale -> isNotBlank( locale.getCountry() ) )
          .ifPresent( locale -> serviceProperties.put( LOCALE_KEY, locale ) );
      return serviceProperties;
    }
    catch( final TokenException | NoAttributeFoundException | UnsupportedEncodingException rethrow )
    {
      throw new IllegalStateException( rethrow );
    }
  }

  @SuppressWarnings("unchecked")
  private Response getRestServiceResponse
      (
          final String serviceCallName,
          final String endpoint,
          final Map<String, Object> serviceProperties
      )
      throws IOException
  {
    final String invokeName = removeEnd( serviceCallName, APIGEE_HMAC_SUFFIX );
    final Deque<String> httpMethodParts = getHttpMethodParts( invokeName );
    final String collectionName = removeEndIgnoreCase( invokeName, httpMethodParts.poll() );
    final String httpMethod = defaultString( httpMethodParts.poll(), GET );
    final String parentName = collectionName + ".";

    final Map<String, Object> resource
        = (Map<String, Object>)serviceProperties.get( RESOURCE_KEY );

    // Adding the path params
    loadPathPropertyFile( "config_path_params.properties" );
    final Map<String, String> pathParams
        = mapPropertiesByParent( parentName, getPathProperties() );
    addResourceValuesToMap( pathParams, resource );

    // Adding the query params
    loadQueryPropertyFile( "config_query_params_upload.properties" );
    final Map<String, String> queryProperties
        = mapPropertiesByParent( parentName, getQueryProperties() );
    addResourceValuesToMap( queryProperties, resource );

    // Build the uri with query params
    final UriBuilder uriBuilder = fromUri( endpoint );
    for( final String name : queryProperties.keySet() )
    {
      uriBuilder.queryParam( name, queryProperties.get( name ) );
    }

    loadHeaderPropertyFile( "config_header_upload.properties" );
    final Map<String, String> headers
        = mapPropertiesByParent( parentName, getHeaderProperties() );
    addResourceValuesToMap( headers, resource );

    loadServicePropertyFile( "config_provider_service.properties" );
    serviceProperties.putAll( mapPropertiesByParent( parentName, getServiceProperties() ) );

    try
    {
      final URI uri = uriBuilder.buildFromMap( pathParams );

      final String correlationId = UUID.randomUUID().toString();

      final Map<String, Object> input = new LinkedHashMap<>( serviceProperties );
      input.put( "correlation_id", correlationId );
      final String body = getBody( collectionName, input );
      if( body == null )
      {
        // TODO: 2/13/17 Resolve headers
        headers.put( SECURITY_TOKEN_PARAM, (String)serviceProperties.get( "encoded_security_token" ) );
        headers.put( ACCOUNT_TOKENS_PARAM, (String)serviceProperties.get( "account_token" ) );
      }
	  if(parentName.contains("check_spending_power"))
        {
          headers.put(Constants.BINARY_SECURITY_TOKEN,(String)serviceProperties.get( "encoded_security_token" ));
          headers.put(Constants.REQ_ID,UUID.randomUUID().toString());
          headers.put(Constants.TIME_STAMP,String.valueOf(System.currentTimeMillis()));
        }
    else if(parentName.contains("pin"))
    {
      headers.put(HEADER_SECURITY_TOKEN, serviceProperties.get( "client_id" ).toString());
      headers.put(HEADER_SYSTEM_ID, APPLICATION_ID);
      headers.put(HEADER_CLIENT_ID, AIM_ID);
      headers.put(HEADER_CORRELATION_ID, createCorrelationId());
      headers.put(HEADER_UNIQUE_REQUEST_ID,serviceProperties.get( "public_guid" ).toString() );
    }
      else {
        // TODO: 2/13/17 Resolve headers
          headers.put( CLIENT_ID_PARAM, CLIENT_ID_VAL );
      }
      headers.put(CORRELATION_ID_PARAM, correlationId);
      headers.put( CONTENT_TYPE, APPLICATION_JSON );

      if( endsWith( serviceCallName, APIGEE_HMAC_SUFFIX ) )
      {
        final long ts = epochTimeInSeconds();

        final String nonce
            = correlationId
              + NONCE_DELIMITER
              + AMEX_TAG
              + NONCE_DELIMITER;

        final String authToken
            = getAuthorizationToken
            (
                serviceProperties.get( "client_id" ).toString(),
                serviceProperties.get( "client_secret" ).toString(),
                ts,
                nonce,
                httpMethod,
                uri,
                false,
                body
            );

        headers.put( AUTHORIZATION, authToken );
      }
      else if( endsWith( serviceCallName, APIGEE_OAUTH_SUFFIX ) )
      {
        // TODO: 10/3/16 Implement Apigee OAuth Option
      }

      return invokeServiceCall( httpMethod, uri, headers, body );

      // TODO: 9/30/16 Should the Response Status Code be checked and/or stored?
    }
    catch( final Exception log )
    {
      log.printStackTrace();
    }

    return Response.noContent().build();
  }

  private void addResourceValuesToMap
      (
          final Map<String, String> map,
          final Map<String, Object> resource
      )
  {
    if( map != null && resource != null )
    {
      map
          .keySet()
          .stream()
          .filter( key -> getRestEndpointType( key ) != null )
          .map( this::removeEndpointMarker )
          .forEach
              (
                  key ->
                  {
                    ofNullable( resource.get( key ) )
                        .filter( String.class::isInstance )
                        .map( String.class::cast )
                        .ifPresent( value -> map.put( key, value ) );
                  }
              );
    }
  }

  @SuppressWarnings("unused")
  private void saveRestServiceResponse
      (
          final MongoDatabase db,
          final String serviceCallName,
          final Response response,
          final Map<String, Object> serviceProperties
      )
  {
	  final int status = response.getStatus();
    final String content = getContentAsString( response );
    System.out.println( "Response ::" + content );
    if( status == NO_CONTENT.getStatusCode() || content != null )
    {
      final Object userId = serviceProperties.get( "user_id" );
      final Object accountNumber = serviceProperties.get( "account_number" );
      final Object accountToken = serviceProperties.get( "account_token" );

      final Document document = new Document();
      document.put( "userId", userId );
      // TODO: 2/13/17 Resolve accountToken vs accountTokn
      document.put( "account_token", accountToken );
      document.put( "account_number", accountNumber );
      document.put( "publicGuid", serviceProperties.get( "public_guid" ) );
      document.put( "privateGuid", serviceProperties.get( "private_guid" ) );
      document.put( "multi_accounts_flag", "N" );
      if( status != NO_CONTENT.getStatusCode() )
      {
        document.put("response", content);
      }
      if( status > OK.getStatusCode() && familyOf( status ) == SUCCESSFUL )
      {
        document.put( "http_status", status );
      }

      final Bson target;
      final Bson userTarget
          = and
          (
              eq( "userId", userId ),
              eq( "account_token", accountToken ),
              eq( "account_number", accountNumber )
          );

      final Object resource = serviceProperties.get( RESOURCE_KEY );
      if( resource != null )
      {
        document.put( RESOURCE_KEY, resource );
        target
            = and
            (
                userTarget,
                eq( RESOURCE_KEY, toBson( resource ) )
            );
      }
      else
      {
        target = userTarget;
      }

      final String baseServiceCallName = removeEndpointMarker( serviceCallName );
      final String collectionName = getDocumentCollectionName( baseServiceCallName );
      System.out.println( "collectionName = " + collectionName );
      final CodecRegistry codecRegistry = db.getCodecRegistry();
      System.out.println( "target = " + target.toBsonDocument( Document.class, codecRegistry ) );
      System.out.println( "document = " + document );
      db.getCollection( collectionName )
        .replaceOne
            (
                target,
                document,
                new UpdateOptions().upsert( true )
            );
    }
  }

  @SuppressWarnings("unchecked")
  private Document toBson( final Object value )
  {
    if( value instanceof Map )
    {
      return new Document( (Map<String, Object>)value );
    }
    return null;
  }


  // Capturing soap response

  public void saveSoapResponse( String userId, String accountNumber, String privateGuid, String modifedxmlResp,
                                SecurityToken securityToken, String accountToken, MongoDatabase db, String collectionName, String publicGuid )
  {

    Document document2 = new Document();

    document2.put( "userId", userId );
    document2.put( "privateGuid", privateGuid );
    document2.put( "account_number", accountNumber );
    document2.put( "account_token", accountToken );
    document2.put( "publicGuid", publicGuid );
    document2.put( "response", modifedxmlResp );

    db.getCollection( collectionName ).replaceOne( new Document( "userId", userId ), document2,
                                                   new UpdateOptions().upsert( true ) );
  }

  /**
   * Loading data to Mongo Both Soap and REST
   *
   * @param userId
   * @param accountNumber
   * @param privateGuid
   * @param racfId
   * @param region
   * @param modifedxmlResp
   * @param securityToken
   * @param modifedxmlResp2
   * @param db
   * @param collectionName
   * @param responseNumber
   * @param flag
   */

  public void saveSoapResponseISL( String userId, String accountNumber, String privateGuid, String racfId,
                                   String region, String modifedxmlResp, SecurityToken securityToken, String modifedxmlResp2, MongoDatabase db,
                                   String collectionName, int responseNumber, int flag )
  {
    Document document2 = new Document();
    if( flag == 1 )
    {
      FindIterable<Document> d = db.getCollection( collectionName ).find( new Document( "userId", userId ) );
      Document d1 = d.first();
      int prevResponseNumber = d1.getInteger( "numberOfResponses" );
      String responses[] = new String[ responseNumber ];
      for( int i = 0; i < prevResponseNumber; i++ )
      {
        responses[ i ] = d1.getString( "response_" + (i + 1) );
      }
      responses[ prevResponseNumber ] = modifedxmlResp;
      document2.put( "userId", userId );
      document2.put( "privateGuid", privateGuid );
      document2.put( "account_number", accountNumber );
      document2.put( "racfId", racfId );
      document2.put( "numberOfResponses", responseNumber );
      document2.put( "region", region );
      for( int i = 0; i < responseNumber; i++ )
      {
        document2.put( "response_" + (i + 1), responses[ i ] );
      }
    }
    else
    {
      document2.put( "userId", userId );
      document2.put( "privateGuid", privateGuid );
      document2.put( "account_number", accountNumber );
      document2.put( "racfId", racfId );
      document2.put( "numberOfResponses", responseNumber );
      document2.put( "region", region );
      document2.put( "response_" + responseNumber, modifedxmlResp );
    }
    db.getCollection( collectionName ).replaceOne( new Document( "userId", userId ), document2,
                                                   new UpdateOptions().upsert( true ) );
  }

  protected Response invokeServiceCall
      (
          final String httpMethod,
          final URI uri,
          final Map<String, String> headers,
          final String body
      )
  {
    final RestServiceInvoker restInvoker = new RestServiceInvoker();
    return invokeServiceCall( restInvoker, httpMethod, uri, headers, body );
  }

  protected Response invokeServiceCall
      (
          final RestServiceInvoker restInvoker,
          final String httpMethod,
          final URI uri,
          final Map<String, String> headers,
          final String body
      )
  {
    if( SUPPORTED_HTTP_METHOD_SET.contains( httpMethod ) )
    {
      switch( httpMethod )
      {
        case PUT:
        case POST:
        case DELETE:
        case GET:
          final String contentType = defaultString( headers.get( CONTENT_TYPE ), APPLICATION_JSON );
          if( body != null )
          {
            return restInvoker.method( httpMethod, uri, headers, entity( body, contentType ) );
          }
          else
          {
            return restInvoker.method( httpMethod, uri, headers );
          }
      }
    }
    throw new IllegalArgumentException( format( "Unsupported HTTP Method: %s", httpMethod ) );
  }

  public static Map<String, String> mapPropertiesByParent
      (
          final String namePrefix,
          final Properties properties
      )
  {
    final Map<String, String> map = new LinkedHashMap<>();
    for( final String name : properties.stringPropertyNames() )
    {
      if( name.startsWith( namePrefix ) )
      {
        map.put( substringAfter( name, "." ), properties.getProperty( name ) );
      }
    }
    return map;
  }

  public Deque<String> getHttpMethodParts( final String requestName )
  {
    final Deque<String> parts = new LinkedList<>();
    final Matcher matcher = SUPPORTED_HTTP_METHODS_PATTERN.matcher( requestName );
    final boolean found = matcher.find();
    final int groupCount = matcher.groupCount();
    if( found && groupCount > 0 )
    {
      parts.add( upperCase( matcher.group() ) );
      for( int index = 1; index <= groupCount; index++ )
      {
        parts.add( upperCase( matcher.group( index ) ) );
      }
    }
    return parts;
  }

  public static String getContentAsString( final Response response )
  {
    if( response != null && response.hasEntity() )
    {
      if( response.bufferEntity() )
      {
        return response.readEntity( String.class );
      }
      else
      {
        final Object entity = response.getEntity();
        if( entity instanceof InputStream )
        {
          try
          {
            return IOUtils.toString( (InputStream)entity );
          }
          catch( final IOException rethrow )
          {
            throw new IllegalStateException( rethrow );
          }
        }
        else if( entity != null )
        {
          return entity.toString();
        }
      }
    }
    return null;
  }
}
