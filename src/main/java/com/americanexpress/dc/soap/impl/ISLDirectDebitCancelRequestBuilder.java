package com.americanexpress.dc.soap.impl;

import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.*;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.CardAccountInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.AccountInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.DirectDebitInfoType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.EnumPaymentMethodType;


import java.util.List;


/**
 * Created by kalyan raavi on 3/27/2017.
 */
public class ISLDirectDebitCancelRequestBuilder implements SoapConsumer {


    private static com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.ObjectFactory updateDDObjFactory =
            new com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.ObjectFactory();

    private static com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.ObjectFactory updateDomainObjectFactory =
            new com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.domaindata.ObjectFactory();



    public SetAccountInfoReqType buildRequest(List<String> accountNumbers, String userId) {
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

        accountInfoUpdateKeyType.setCardNO(accountNumber);
        directDebitInfoType.setPaymentMethodCD(EnumPaymentMethodType.NO_DD);

        accountInfoType.setDirectDebitInfo(directDebitInfoType);
        CardAccountInfoType cardAccountInfoType = new CardAccountInfoType();
        cardAccountInfoType.setAccountInfo(accountInfoType);
        cardAccountInfoType.setAccountInfoUpdateKey(accountInfoUpdateKeyType);
        setAccountInfoReqType.setCardAccountInfo(cardAccountInfoType);
        return setAccountInfoReqType;
    }

    private void populateLetterCode(SetAccountInfoReqType setAccountInfoReqType) {

        SendLetterParametersType sendLetterParameters = updateDDObjFactory.createSendLetterParametersType();
        String caseNo = generateCaseNumber();
        sendLetterParameters.setCaseNO(caseNo);

        LetterVariableType letterVariable = updateDDObjFactory.createLetterVariableType();
        AnnotationType annotationType = updateDDObjFactory.createAnnotationType();

        annotationType.setActionCD("WDDX");
        annotationType.setNotes("Direct Debit Unenrol");
        sendLetterParameters.setLetterCD("Y04");
        letterVariable.setNumber3(caseNo);

        sendLetterParameters.setLetterVariable(letterVariable);
        setAccountInfoReqType.setAnnotation(annotationType);
        setAccountInfoReqType.setSendLetterParameters(sendLetterParameters);

    }


    private static String generateCaseNumber() {
        long startOfTime = 974844 * 1000000;
        long currentTime = System.currentTimeMillis() - startOfTime;
        return "0" + (String.valueOf(currentTime / 1000));
    }


}




