
### Why Mock Framework

1.  Wiremock library for stubbing and proxying services.
2.  Easy Setup.
3.  Recording and Playback of services.
4.  Fault Injection.
5.  Removed downstream services dependency.
6.  Increased test coverage.

### Why Mock Wrapper

1.  Mocks the REST and Soap services.
2.  Record and store the services payload in MongoDB.
3.  Share mock objects across applications.
4.  CRUD operations on mocked data.
5.  proxies to backend if mock data is not present  



![Mock framework](screenshot/mock_framework_architecture.png) 


I. Steps to run axp_api_mock for loading mock services.
---------------------------------------------------------

1.  Clone the American2 Express MYCA Mock repository, work against the _master_ branch.

    ```bash
    $ https://stash.aexp.com/stash/scm/fa/axp-api-mock.git
    ```

2.  Add an entry to the `config_mock.properties` file in the `./src/main/resources/` directory to
    configure a mock endpoint path.
    
    **Mock Entry Format**:
    
    `{collection_name}{service_type}={endpoint_path_pattern}`
    
    _where_:
    
    - `collection_name` - the MongoDB collection name to link to the mock endpoint path
    - `service_type` - the service type of the mock endpoint path.
      Supported service types: `_rest` or `_soap`
    - `endpoint_path_pattern` - the endpoint path pattern to mock

    **Example of `config_mock.properties` file**:

    ```properties
    member_rest=/account_servicing/member/v1/accounts
    payments_soap=/myca/services/fins/v2/FinancialService
    ```

    **Supported HTTP Methods**:
    
    - **`GET`** - Add a mock entry, and if needed implement the related
      `RequestStubBuilderService` and `RequestStubBuilder` classes.
    - **`POST`** - Add a mock entry, and implement the related
      `RequestStubBuilderService` and `RequestStubBuilder` classes.

3.  Implement `RequestStubBuilderService` and `RequestStubBuilder` Classes

    > **IMPORTANT NOTE:** THIS STEP IS ONLY REQUIRED FOR THE STUBBING OF MOCK HTTP REQUESTS WITH
      DETAILED MATCHES LIKE QUERY STRING PARAMETERS, HEADER NAME/VALUE PAIR, AND REQUEST BODY
      CONTENT.
    
    > The implementation of the `RequestStubBuilderService` class should gather together
      related backend service calls and their associated `RequestStubBuilder` implementations
      by `collection_name` into logical groupings.

    > _For example_: All Offers backend service calls are under
      the `OffersRequestStubBuilderService` implementation.

    Register the implementation of the `RequestStubBuilderService` class
    by adding the fully qualified class name
    into the `com.americanexpress.dc.mock.service.spi.RequestStubBuilderService` file
    in the `./src/main/resources/META-INF/services/` directory.
    
    **`RequestStubBuilderService` Implementation Registration Example**:

    ```
    com.americanexpress.dc.mock.service.members.MemberRequestStubBuilder
    ```

    An implementation of the `RequestStubBuilderService` class must implement the `getBuilder`
    method to provide a mapping between the `collection_name` of a mock entry to an implementation
    of the `RequestStubBuilder` class.
    
    **`RequestStubBuilderService` Implementation Class Example**:

    ```java
    public class MemberRequestStubBuilderService extends RequestStubBuilderService
    {
    
      private final Map<String, RequestStubBuilder> map = new TreeMap<>();
    
      public MemberRequestStubBuilderService()
      {
        map.put( "member", new MemberRequestStubBuilder() );
      }
    
      @Override
      public RequestStubBuilder getBuilder( final String key )
      {
        return map.get( key );
      }
    }
    ```

    An implementation of the `RequestStubBuilder` class must implement the `build` method to
    provide an instance of the `RequestPattern` class used to stub a mock endpoint path.
    
    **`RequestStubBuilder` Implementation Class Example**:

    ```java
    public class MemberRequestStubBuilder implements RequestStubBuilder
    {
    
      @Override
      public RequestPattern build( final UserAccountsBean user )
      {
        final RequestPattern requestPattern = new RequestPattern();
    
        // Set HTTP Method to be matched. If not set defaults to GET method.
        requestPattern.setMethod( POST );
    
        // Set URL Path Pattern to be matched. If not set defaults to 'endpoint_path_pattern'
        // value of mock entry that is mapped the 'collection_name'.
        final String urlPathPattern = "/account_servicing/member/v1/accounts";
        requestPattern.setUrlPathPattern( urlPathPattern );
    
        // Specific Query Parameter Patterns
        final Map<String, ValuePattern> queryParamPatterns = new LinkedHashMap<>();
        queryParamPatterns.put( "mode", ValuePattern.equalTo( "new" ) );
        requestPattern.setQueryParameters( queryParamPatterns );
    
        // Extract User Account Data
        final String accountNumber;
        final String accountToken;
        if( user != null )
        {
          accountNumber = user.getAccountNumber();
          accountToken = user.getAccountToken();
        }
        else
        {
          accountNumber = null;
          accountToken = null;
        }
    
        // Specific Header Name/Value Patterns
        final Map<String, ValuePattern> headerPatterns = new LinkedHashMap<>();
        headerPatterns.put( "account_tokens", ValuePattern.containing( accountToken ) );
        requestPattern.setHeaders( headerPatterns );
    
        // Specific Request Body Content Patterns
        final List<ValuePattern> bodyPatterns = new LinkedList<>();
        final Collection<String> bodyParts
            = new LinkedList<>( asList( "profile", "identifier" ) );
        if( accountNumber != null )
        {
          bodyParts.add( accountNumber );
        }
        for( final String bodyPart : bodyParts )
        {
          final String quotedBodyPart = wrap( bodyPart, '"' );
          bodyPatterns.add( ValuePattern.containing( quotedBodyPart ) );
        }
        requestPattern.setBodyPatterns( bodyPatterns );
    
        return requestPattern;
      }
    }
    ```

4.  Running Mock Framework

    ##### Option 1: Generate and Execute Java Archive (JAR) file

    **Run gradle command**:

    ```
    $ gradle axp_api_mock
    ```

    > This will package the `MockDataLoader` class with an embedded jetty server
      into the `axp_api_mock_mock-all-1.0.jar` file in the `./build/libs/` directory.
      
    **Run java command**:

    ```
    $ java -jar axp_api_mock-all-1.0.jar
    ```
    
    > This `axp_api_mock-all-1.0.jar` file can be run in any NGI server,
      which has a java 8. Whether running on your local or any E1
      environment there must be access to the Mongo servers.

    ##### Option 2: Execute `main` method of `MockDataLoader` class
    
    **Run the `MockDataLoader` class via your IDE**
    
    _**OR**_

    **Run the `MockDataLoader` class via java command**:

    ```
    $ java com.americanexpress.dc.mock.MockDataLoader
    ```


II. Steps to update mock data (REST and SOAP)-
---------------------------------------

1.  Change the method to PUT and use the **MOCK-DATA-UPLOAD-URL** url from the response header.

2.  Work on the response payload and paste it in the request body and update it.

3.  Try the GET for the same request and should see with the modified response.
 	
 	![Mock framework](screenshot/mock_put_response_header.jpeg) 
 	
 	![Mock framework](screenshot/mock_put_scenario.jpeg) 


III. Steps to record mock data (REST).
---------------------------------------

1.  Clone the American Express MYCA Mock repository, work against the _master_ branch.

    ```bash
    $ git clone https://stash.aexp.com/stash/scm/fa/axp-myca-mock.git
    ```

2.  Add an entry to the `config_upload.properties` file in the `./src/main/resources/` directory to
    configure a recording of backend data.
    
    **Recording Entry Format**:
    
    `{collection_name}{http_method}{auth_type}={backend_url}`
    
    _where_:
    
    - `collection_name` - the MongoDB collection name to link to the recording backend data.
    - `http_method` - the HTTP method of the backend service call.
      If not present then defaults to HTTP GET method.
      Supported HTTP methods: `_get` or `_post`
    - `auth_type` - the authorization type of the backend service call.
      Optional, maybe unspecified.
      Supported authorization types: `_apigee_hmac`
    - `backend_url` - the canonical backend service endpoint URL.

    **Example of `config_upload.properties` file**:

    ```properties
    member=https://mycaservicese1qonline.webqa.ipc.us.aexp.com:5556/account_servicing/member/v1/accounts
    offer_total_savings_post_apigee_hmac=https://in.api.dev.app.aexp.com/marketing/v1/registeredcard/cardmembers/offer-savings/history
    ```
   

3.  Configure the provider service, the query parameters, and the header values properties in
    their respective files within the `./src/main/resources/` directory.  
    
    **Properties Files**:

    - `config_path_params.properties` - the path parameter properties of the backend service call.
    - `config_query_params_upload.properties` - the query parameter properties of the backend
       service call.
    - `config_header_upload.properties` - the header value properties of the backend service call.
    - `config_provider_service.properties` - the provider service properties of the backend
       service call.

    **Properties Entry Format**:
    
    `{collection_name}.{property_name}={property_value}`
    
    _where_:
    
    - `collection_name` - the MongoDB collection name to link to the recording backend data.
    - `property_name` - the HTTP method of the backend service call.
    - `property_value` - the canonical backend service endpoint URL.

    **Example of `config_query_params_upload.properties` file**:

    ```properties
    #Transactions
    transactions.offset=offset
    transactions.limit=limit
    ```
    **Example of `config_header_upload.properties` file**:

    ```properties
    transactionsummary.type=split_by_cardmember
    loyalty.locale=en-NZ
    offer_total_savings.X-AMEX-API-KEY=3I0HxajcqB81aRFD4pz8AGhWQDR1X6cm
    offer_total_savings.X-AMEX-RC-PARTNERID=YUVC8504
    ```

    **Example of `config_provider_service.properties` file**:

    ```properties
    offer_total_savings.partner_id=YUVC8504
    offer_total_savings.partner_key=DB0F48CA85C55B9F12B1B582C9A6BBC5AAE23923BBF66817146B3526CEBB8817
    ```

    **Recording multi accounts-**
        
         _**config_multiaccounts_api.properties**_ file will be used to configure multi flag like below
        
                balances=Y
                transactions_posted=N
                transactions_pending=N
                interest_rates=Y
                statement_periods=Y
                transaction_summary=Y
            
         Flag **'Y'** depicts the collection could be a multi accounts call and **'N'** for not a multi account call.


4.  Configure test accounts 'userId' to be recorded in the Microsoft Excel with the sheet name 'be_accounts'

    ![Mock framework](screenshot/testdata_screenshot.png)     
    
    
        Note: Same has to be followed while recording for Soap service

5.  Implement `RequestBuilderService` and `RequestBuilder` Classes

    > **IMPORTANT NOTE:** THIS STEP IS ONLY REQUIRED FOR BUILDING A REQUEST BODY FOR
      THE HTTP METHODS OF `POST` AND `PUT` OF A BACKEND SERVICE CALL.
    
    > The implementation of the `RequestBuilderService` class should gather together related
      backend service calls and their associated `RequestBuilder` implementations by
      `collection_name` into logical groupings.

    > _For example_: All Financial Service (FINS) backend service calls could be under
      the `FinsRequestBuilderService` implementation.

    Register the implementation of the `RequestBuilderService` class
    by adding the fully qualified class name
    into the `com.americanexpress.dc.builder.spi.RequestBuilderService` file
    in the `./src/main/resources/META-INF/services/` directory.
    
    **`RequestBuilderService` Implementation Registration Example**:

    ```
    com.americanexpress.dc.builder.offers.OffersRequestBuilderService
    ```

    An implementation of the `RequestBuilderService` class must implement the `getBuilder`
    method to provide a mapping between the `collection_name` of a recording entry to an
    implementation of the `RequestBuilder` class.
    
    **`RequestBuilderService` Implementation Class Example**:

    ```java
    public class OffersRequestBuilderService extends RequestBuilderService
    {
    
      private final Map<String, RequestBuilder> map = new TreeMap<>();
    
      public OffersRequestBuilderService()
      {
        map.put( "offer_total_savings", new OfferTotalSavingsRequestBuilder() );
        map.put( "offer_savings_detail", new OfferSavingsDetailRequestBuilder() );
      }
    
      @Override
      public RequestBuilder getBuilder( final String key )
      {
        return map.get( key );
      }
    }
    ```

    An implementation of the `RequestBuilder` class must implement the `getBody` method to
    provide the request body as a `String` for executing the backend service call.
    
    **`RequestBuilder` Implementation Class Example**:

    ```java
    public class OfferTotalSavingsRequestBuilder extends RequestBuilder
    {
    
      private static final Gson GSON = new GsonBuilder().create();
    
      static final String FETCH_TOTAL_SAVINGS = "T";
    
      /**
       * Constructs the HTTP request body based on provided input
       *
       * @param input
       *     the map containing input request objects
       *
       * @return the constructed HTTP request body
       */
      @Override
      public String getBody( final Map<String, Object> input )
      {
        requireNonNull( input );
    
        final JsonObject requestJson = new JsonObject();
    
        requestJson.addProperty( "prtrKey", getRequiredValue( input, "partner_key", String.class ) );
    
        final JsonArray accountNumbers
            = Stream
            .of( getRequiredValue( input, "account_number", String.class ) )
            .collect( toJsonArray() );
        final JsonObject cmNoListJson = new JsonObject();
        cmNoListJson.add( "cardNbr", accountNumbers );
        requestJson.add( "cmNoList", cmNoListJson );
    
        requestJson.addProperty( "savingsDetlType", FETCH_TOTAL_SAVINGS );
        requestJson.addProperty( "version", "1.0" );
    
        final String correlationId
            = getValue( input, "correlation_id", String.class )
            .orElseGet( () -> UUID.randomUUID().toString() );
        requestJson.addProperty( "msgId", right( correlationId, 20 ) );
    
        final Locale locale
            = getValue( input, "locale", Locale.class )
            .orElse( Locale.US );
        requestJson.addProperty( "langCd", upperCase( locale.getLanguage() ) );
        requestJson.addProperty( "ctryCd", upperCase( locale.getCountry() ) );
    
        return GSON.toJson( requestJson );
      }
    }
    ```

6.  Running Recording Process

    ##### Execute `main` method of `MockDataUploadUtility` class
    
    **Run the `MockDataUploadUtility` class via your IDE**
    
    _**OR**_

    **Run the `MockDataUploadUtility` class via java command**:

    ```
    $ java com.americanexpress.dc.mock.MockDataUploadUtility
    ```


IV. Steps to record mock data (Soap).
---------------------------------------

1.  Clone the American Express MYCA Mock repository, work against the _master_ branch.

    ```bash
    $ git clone https://stash.aexp.com/stash/scm/fa/axp-myca-mock.git
    ```

2.  Update the properties file

    Add entries to the `config_upload.properties` file in the `./src/main/resources/` directory to
    configure recording of backend data.
    
    **Recording Entry Format**:
    
    `{collection_name}_soap={backend_url}`
    
    _where_:
    
    - `collection_name` - the MongoDB collection name to link to the recording backend data.
    - `backend_url` - the canonical backend service endpoint URL.

    **Example of `config_upload.properties` file**:
    
    ```properties
    FinancialService_soap=http://mycaservicese1qonline.webqa.ipc.us.aexp.com:5555/myca/services/fins/v2/FinancialService
    ```
  
3.  Add entries to the `config_soap_service.properties` file in the `./src/main/resources/`
    directory to link to the `collection_name` of the recording entry in the
    `config_upload.properties` file to SOAP client configuration.

    **SOAP Client Entries Format**:

    ```properties
    {collection_name}_SERVICE_CLASS_NAME={service_class_name}
    {collection_name}_SERVICE_METHOD_NAME={service_method_name}
    {collection_name}_METHOD_NAME={method_name}
    {collection_name}_ENDPOINT_URL={backend_url}
    {collection_name}_TIME_OUT={timeout}
    ```
    
    _where_:
    
    - `collection_name` - the MongoDB collection name to link to the recording backend data.
    - `service_class_name` - the soap service class name for the backend service.
    - `service_method_name` - the soap service method name for the backend service.
    - `method_name` - the soap method name for the backend service.
    - `backend_url` - the canonical backend service endpoint URL.
    - `timeout` - the timeout for backend service call.

    **Example of `config_soap_service.properties` file**:
  
    ```properties
    FinancialService_SERVICE_CLASS_NAME=com.americanexpress.as.schemas.fsresponseservice._2.FinancialService
    FinancialService_SERVICE_METHOD_NAME=getFinancialServicePort
    FinancialService_METHOD_NAME=getCardFinancials
    FinancialService_ENDPOINT_URL=http://mycaservicese1qonline.webqa.ipc.us.aexp.com:5555/myca/services/fins/v2/FinancialService
    FinancialService_TIME_OUT=15
    ```

3.  Implement `SoapConsumer` Class and Update `SoapRequestBuilderFactory` Class

    An implementation of the `SoapConsumer` class must implement the `buildRequest` method to
    provide the SOAP request object instance for executing the backend service call.
    
    **`SoapConsumer` Implementation Class Example**:

    ```java
    public class FinsSoapConsumerImpl implements SoapConsumer
    {
    
      private static final Integer CYCLE_INDEX = 0;
    
      // Build Fins request object
      public FinancialServiceRequest buildRequest( String accountNumber, String userId )
      {
        FinancialServiceRequest financialServiceRequest = new FinancialServiceRequest();
        try
        {
          // Call to financial soap service starts
          CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
          SecurityToken securityToken = collectionUploadUtil.generateClaims( userId );
    
          financialServiceRequest.setApplicationName( Constants.DEFAULT_CLIENT_ID );
          financialServiceRequest.setSecurityToken( ServiceUtil.encodeSecurityToken( securityToken ) );
    
          CardFinancialsRequest cardFinancialsRequest = new CardFinancialsRequest();
          PeriodCriteria periodCriteria = new PeriodCriteria();
          periodCriteria.setCycleIndex( CYCLE_INDEX );
          cardFinancialsRequest.setPeriod( periodCriteria );
    
          cardFinancialsRequest.setCyclesRequired( false );
          cardFinancialsRequest.setCyclesMetaDataRequired( false );
          cardFinancialsRequest.setTransactionsRequired( false );
          cardFinancialsRequest.setBalancesRequired( true );
    
          DataSources dataSources = new DataSources();
          DataSourceConfig dataSourceConfig = new DataSourceConfig();
          dataSourceConfig.setUse( true );
          dataSourceConfig.setRefreshCache( false );
          dataSourceConfig.setFailIfUnavailable( true );
          dataSources.setUseStatementCache( dataSourceConfig );
          cardFinancialsRequest.setDataSources( dataSources );
    
          cardFinancialsRequest.setRealTimeARDataRequired( false );
          cardFinancialsRequest.setMultiCurrencyDataRequired( false );
    
          AccountRequest accountRequest = new AccountRequest();
          accountRequest.setAccountNumber( accountNumber );
          cardFinancialsRequest.setAccount( accountRequest );
    
          List<CardFinancialsRequest> cardFinancialsRequestList = new ArrayList<CardFinancialsRequest>();
          cardFinancialsRequestList.add( cardFinancialsRequest );
          financialServiceRequest.getCardRequests().addAll( cardFinancialsRequestList );
        }
        catch( Exception e )
        {
          e.printStackTrace();
        }
        return financialServiceRequest;
      }
    }
    ```

    Update the `SoapRequestBuilderFactory` implementation class to provide a mapping between
    the `collection_name` of a recording entry to an implementation of the `SoapConsumer` class.
    
    **Add a Constant for the `collection_name` to the `Constants` Class**:

    ```
    public static final String PAYMENTS_COLLECTION_NAME = "FinancialService";
    ```

    **Add a Conditional for the `collection_name` to the `SoapRequestBuilderFactory` Class**:

    ```
    if( Constants.PAYMENTS_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
    {
      return new FinsSoapConsumerImpl();
    }
    ```

    **`SoapRequestBuilderFactory` Implementation Class Example**:

    ```java
    public class SoapRequestBuilderFactory
    {
    
      public SoapConsumer getSoapImplementation( String collectionName )
      {
        if( Constants.PAYMENTS_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
        {
          return new FinsSoapConsumerImpl();
        }
        else if( Constants.CREDIT_LIMITS_COLLECTION_NAME.equalsIgnoreCase( collectionName ) )
        {
          return new CSSSoapConsumerImpl();
        }
        return null;
      }
    }
    ```

4.  Running Recording Process
  
    ##### Execute `main` method of `MockDataUploadUtility` class
    
    **Run the `MockDataUploadUtility` class via your IDE**
    
    _**OR**_

    **Run the `MockDataUploadUtility` class via java command**:

    ```
    $ java com.americanexpress.dc.mock.MockDataUploadUtility
    ```


V. MongoDB Replicaset Details : 
---------------------------------------

##### MongoDB Servers

- `lpdwd550.phx.aexp.com` (Primary)
- `lpdwd551.phx.aexp.com`
- `lpdwd549.phx.aexp.com`

##### Database Name

- `APPX_API_MOCK_DB`

##### Database Credential

> Below user id needs to be used by Application.

- Username: `apiMockDBUser`
- Password: `XXXXXXXXX`
- Role: `dbAppUser`


