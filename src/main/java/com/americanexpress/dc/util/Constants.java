package com.americanexpress.dc.util;

public class Constants {

    public static final String STMT_PERIODS_COLLECTION_NAME = "Statement_Periods";
    public static final String TRANSACTIONS_COLLECTION_NAME = "Transactions";
    public static final String MEMBERS_COLLECTION_NAME = "Members";
    public static final String BALANCE_COLLECTION_NAME = "Balances";
    public static final String INTEREST_RATES_COLLECTION_NAME = "interest_rates";
    public static final String CREDIT_LIMITS_COLLECTION_NAME = "CreditStatusServiceWS";
    public static final String LOYALTY_INFO_ACCOUNT_COLLECTION_NAME = "LoyaltyAccountInfoNonSecuredServiceWS";
    public static final String CARD_SUMMARY_COLLECTION_NAME = "card_activity_summary";
    public static final String PAYMENTS_COLLECTION_NAME = "FinancialService";
    public static final String PAYMENTS_SUMMARY_COLLECTION_NAME = "PaymentServiceWS";
    public static final String ACCOUNT_UPDATE_SERVICE_NAME = "AccountUpdateService";
    public static final String ISL_DD_ENROLL_COLLECTION_NAME = "AccountUpdateService_DD_Enroll";
    public static final String ISL_DD_CANCEL_COLLECTION_NAME = "AccountUpdateService_DD_Cancel";
    public static final String US_LOYALTY_INFO_ACCOUNT_COLLECTION_NAME = "LoyaltyServiceWS";
    public static final String ACC_TOKENS_COLLECTION_NAME = "AccountTokens_AccountNumber_Mapper";
    public static final String SDP_INQUIRY_COLLECTION_NAME = "StatementDeliveryPreferenceInquiryService";
    public static final String SDP_UPDATE_COLLECTION_NAME = "StatementDeliveryPreferenceUpdateService";
    public static final String ACP_INQUIRY_COLLECTION_NAME = "ACPInquiryService";
    public static final String ACP_UPDATE_COLLECTION_NAME = "ACPUpdateService";
    public static final String ALERTS_PREF_INQUIRY_COLLECTION_NAME = "AlertsPreferenceInquiryServicePort";
    public static final String ALERTS_PREF_UPDATE_COLLECTION_NAME = "AlertsPreferenceUpdateServicePort";
    public static final String PRIVACY_PREF_INQUIRY_COLLECTION_NAME = "PrivacyPreferenceInquiryServicePort";
    public static final String PRIVACY_PREF_UPDATE_COLLECTION_NAME = "PrivacyPreferenceUpdateServicePort";
    public static final String EMAIL_ADDRESS_INQUIRY_COLLECTION_NAME = "EAddressInquiryService";
    public static final String EMAIL_ADDRESS_UPDATE_COLLECTION_NAME = "EAddressUpdateService";
    public static final String INTL_LOYALTY_COLLECTION_NAME = "IntlLoyaltyService";
    public static final String LOYALTY_BONUS_COLLECTION_NAME = "LoyaltyBonusTransNonSecuredServiceWS";

    public static final String DEFAULT_CORRELATION_ID = "12345678";
    public static final String DEFAULT_CLIENT_ID = "AmexAPI";

    public static final String CORRELATION_ID_HEADER_FIELD = "correlation_id";
    public static final String ACCOUNT_TOKEN_HEADER_FIELD = "account_tokens";
    public static final String ACCOUNT_TOKEN_MGM_ELIGIBILITY_HEADER_FIELD = "account_token";
    public static final String CLIENT_ID_HEADER_FIELD = "client_id";
    protected static final String ACCOUNT_TOKEN_MYCA_ENCRYPTION = "account_token_myca_encryption";
    public static final String LOCALE_KEY = "locale";

    public static final String TEST_DATA_SHEET = "testdata.xls";
    public static final String TEST_DATA_SHEET_NAME = "be_accounts";
    public static final String SECURITY_TOKEN_PARAM = "security_token";

    public static final String MOCK_PROXY_URL = "MOCK-PROXY-URL";
    public static final String MOCK_SERVICE_CALL_NAME = "MOCK-SERVICE-CALL-NAME";
    public static final String MOCK_COLLECTION_NAME = "MOCK-COLLECTION-NAME";
    public static final String MOCK_RESPONSE_ID = "MOCK-RESPONSE-ID";
    public static final String MOCK_DATA_UPLOAD_URL = "MOCK-DATA-UPLOAD-URL";
    public static final String COLLECTION_NAME_QUERY_PARAM = "collection_name";
    public static final String RESPONSE_ID_QUERY_PARAM = "response_id";
    public static final String USER_KEY = "user";
    public static final String RESOURCE_KEY = "resource";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String BINARY_SECURITY_TOKEN = "binary_security_token";
    public static final String TIME_STAMP = "timeStamp";
    public static final String REQ_ID = "requestID";
    public static final String CALLER_TYPE_BASIC = "BASIC";
    public static final String CALLER_TYPE_SUPP = "SUPP";
    public static final String CAS_SERVICE = "/servicing/v1/customers/accounts/card_accounts/spend_limit/enquiry";
    public static final String CAS_SUCCESS_RESPONSE = "builder.checkspendpower/success_resp_approved.json";
    public static final String DEFAULT_RACFID = "XA4226A";
    public static final String AR_SOR_REGION_ID = "CICDKBX0";
}
