package com.americanexpress.dc.soap.impl;

import com.americanexpress.dc.util.AccountRACFRegionReader;
import com.americanexpress.dc.util.CommonUtil;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.*;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.CardAccountInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.AccountInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.DirectDebitInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.EnumPaymentMethodType;


import java.util.List;


/**
 * Created by kalyan raavi on 3/27/2017.
 */
public class ISLDirectDebitEnrollRequestBuilder implements SoapConsumer {

    private static com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.ObjectFactory updateDDObjFactory =
            new com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.ObjectFactory();

    private static com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.ObjectFactory updateDomainObjectFactory =
            new com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.ObjectFactory();



    public SetAccountInfoReqType buildRequest(List<String> accountNumbers, String userId){

        SetAccountInfoReqType setAccountInfoReqType = null;
        if(accountNumbers.size() > 0) {
            setAccountInfoReqType = populateDDInfoType(accountNumbers.get(0));
            populateLetterCode(setAccountInfoReqType);
        }

        return setAccountInfoReqType;
    }


    SetAccountInfoReqType populateDDInfoType(String accountNumber) {
        SetAccountInfoReqType setAccountInfoReqType = updateDDObjFactory.createSetAccountInfoReqType();
        AccountInfoType accountInfoType = updateDomainObjectFactory.createAccountInfoType();
        DirectDebitInfoType directDebitInfoType = updateDomainObjectFactory.createDirectDebitInfoType();
        AccountInfoUpdateKeyType accountInfoUpdateKeyType = updateDDObjFactory.createAccountInfoUpdateKeyType();

        directDebitInfoType.setBankID("0000090128");
        directDebitInfoType.setBankAccountNO("9999");
        directDebitInfoType.setBankAccountHolderNM("KALYAN RAAVI");
        directDebitInfoType.setStartDT(CommonUtil.convertStringToXMLGregCal("2017-01-03"));
        directDebitInfoType.setPaymentMethodCD(EnumPaymentMethodType.valueOf("BALANCE"));
        accountInfoUpdateKeyType.setCardNO(accountNumber);


        accountInfoType.setDirectDebitInfo(directDebitInfoType);
        CardAccountInfoType cardAccountInfoType = new CardAccountInfoType();
        cardAccountInfoType.setAccountInfo(accountInfoType);
        cardAccountInfoType.setAccountInfoUpdateKey(accountInfoUpdateKeyType);
        setAccountInfoReqType.setCardAccountInfo(cardAccountInfoType);
        return setAccountInfoReqType;
    }


    private void populateLetterCode(SetAccountInfoReqType setAccountInfoReqType){

        SendLetterParametersType sendLetterParameters = updateDDObjFactory.createSendLetterParametersType();
        String caseNo = generateCaseNumber();
        sendLetterParameters.setCaseNO(caseNo);

        LetterVariableType letterVariable = updateDDObjFactory.createLetterVariableType();
        AnnotationType annotationType = updateDDObjFactory.createAnnotationType();

        annotationType.setActionCD("WDDE");
        annotationType.setNotes("Direct Debit Enrolment Min Payment Due");
        sendLetterParameters.setLetterCD("M1P");

        sendLetterParameters.setLetterVariable(letterVariable);
        setAccountInfoReqType.setAnnotation(annotationType);
        setAccountInfoReqType.setSendLetterParameters(sendLetterParameters);

    }


    private static String generateCaseNumber() {
        long startOfTime = 974844 * 1000000;
        long currentTime = System.currentTimeMillis() - startOfTime;
        return "0" + (String.valueOf(currentTime / 1000));
    }


    public static com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.RequestHeaderType buildRequestHeader(String cardNum) {

    com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.ObjectFactory globalObjFactory= new com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.ObjectFactory();
    com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.RequestHeaderType requestHeader = globalObjFactory.createRequestHeaderType();
    com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.SORRegionTokenType sorRegionToken = globalObjFactory.createSORRegionTokenType();

    String userID = AccountRACFRegionReader.getInstance().getRacfForAccount(cardNum);
    String sorRegionID = AccountRACFRegionReader.getInstance().getRegionForAccount(cardNum);

    sorRegionToken.setAutomationID(userID);
    sorRegionToken.setSORRegionID(sorRegionID);

    requestHeader.setUserId(userID);
    requestHeader.setCorrelationID("ISLDDUpateRequestFromMockServer");
    requestHeader.getSORRegionToken().add(sorRegionToken);

    return requestHeader;
    }

}


