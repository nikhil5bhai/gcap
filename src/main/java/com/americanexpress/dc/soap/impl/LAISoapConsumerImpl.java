package com.americanexpress.dc.soap.impl;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.americanexpress.dc.mock.CollectionUploadUtil;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.Account;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.AccountInfo;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.AccountList;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.ExtendedEntityInfo;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.ExtendedEntityList;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.GetLoyaltyAccountInfoWSRequest;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.LoyaltyProgram;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.LoyaltyProgramList;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.LoyaltyRequestFilterParamsType;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.LoyaltyRequestParamsType;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.ProgramCodeInfo;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.ProgramCodesList;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.RequestExtension;
import com.americanexpress.loyalty.processservices.schemas.accountinfo.getlyacctinfo.request.v1.RequestExtensions;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.ClientContext;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.CommonRequestContextType;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.RequestContext;
import com.americanexpress.loyalty.processservices.schemas.commonschema.v1.ServiceContext;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
public class LAISoapConsumerImpl implements SoapConsumer {
	@Override
	public GetLoyaltyAccountInfoWSRequest buildRequest(List<String> accountNumbers, String userId) {

		GetLoyaltyAccountInfoWSRequest request = new GetLoyaltyAccountInfoWSRequest();
		try {
		  SecurityToken securityToken = null;
	      CollectionUploadUtil collectionUploadUtil = new CollectionUploadUtil();
	      securityToken = collectionUploadUtil.generateClaims(userId,"");

					
			CommonRequestContextType commonRequestContextType = new CommonRequestContextType();
			CommonRequestContextType commonRequestContextType2 = commonRequestContextType;
			CommonRequestContextType commonrequest = commonRequestContextType2;
			
			XMLGregorianCalendar result = null;
			
			java.util.Date date;
			 SimpleDateFormat simpleDateFormat;
			 GregorianCalendar gregorianCalendar;
			 
			 simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			                date = simpleDateFormat.parse("2016-10-10T12:19:18.849-07:00");
			                
			                gregorianCalendar = 
			                        (GregorianCalendar)GregorianCalendar.getInstance();
			                    gregorianCalendar.setTime(date);
			                    result = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
			
			ClientContext value = new ClientContext();
			value.setBusinessProcess("SDP");
			value.setSystemID("SDP");
			value.setChannelId("WEB");
			value.setPrincipal("ccpid");
			value.setPrincipalType("XA4883A");
			commonrequest.setClientContext(value);
			
			ServiceContext value2 = new ServiceContext();
			value2.setServiceMinorVersion("0");
			commonrequest.setServiceContext(value2);
			
			RequestContext value3 = new RequestContext();
			value3.setCorrelationID("CVatZ/BH/EXEiBq2awIi+KkD5f0=");
			value3.setTimestamp(result);
			commonrequest.setRequestContext(value3);
			
			request.setCommonRequestContextType(commonrequest);
			
			/*LoyaltyRequestParamsType value4 = new LoyaltyRequestParamsType();
			AccountList value5 = new AccountList();
				AccountInfo accountInfo = new AccountInfo();
					Account account = new Account();
							account.setAccountNumber(accountNumber);
							account.setAccountType("CARD_ACCOUNT_NUMBER");
							accountInfo.getAccount().add(account);
						value5.getAccountInfo().add(accountInfo);
						value4.setAccountList(value5);
			request.setLoyaltyRequestParamsType(value4);
			
			LoyaltyRequestFilterParamsType value6 = new LoyaltyRequestFilterParamsType();
			LoyaltyProgramList value7 = new LoyaltyProgramList();
			
			LoyaltyProgram loyaltyProgram = new LoyaltyProgram();
						   loyaltyProgram.setProgramType("REBATES_PROGRAMS");
						   loyaltyProgram.setLoyaltyAccountStatus("ACTIVE");
						   loyaltyProgram.setRrPeriodInd("CURR");
						   loyaltyProgram.setRrEndDT("N");
						   ProgramCodesList value8 = new ProgramCodesList();
						   					ProgramCodeInfo element = new ProgramCodeInfo();
						   									element.setProgramCode("ALL");
						   									ExtendedEntityList value9 = new ExtendedEntityList();
						   													ExtendedEntityInfo element3 = new ExtendedEntityInfo();
						   																	   element3.setTransactionType("ACCOUNT_INFO");
						   																	   value9.getExtendedEntityInfo().add(element3);
															element.setExtendedEntityList(value9);
											value8.getProgramCodeInfo().set(0, element);
			loyaltyProgram.setProgramCodesList(value8);
						
			
			LoyaltyProgram loyaltyProgram1 = new LoyaltyProgram();
						   loyaltyProgram1.setProgramType("REWARDS_PROGRAMS");
				           loyaltyProgram1.setLoyaltyAccountStatus("ACTIVE");
				           ProgramCodesList value10 = new ProgramCodesList();
				           				ProgramCodeInfo programCodeInfo = new ProgramCodeInfo();
				           								programCodeInfo.setProgramCode("ALL");
				           								ExtendedEntityList value11 = new ExtendedEntityList();
				           								ExtendedEntityInfo extendedEntityInfo = new ExtendedEntityInfo();
				           												   extendedEntityInfo.setTransactionType("ACCOUNT_INFO");
				           												   value11.getExtendedEntityInfo().add(extendedEntityInfo);
				           								programCodeInfo.setExtendedEntityList(value11);
				           					value8.getProgramCodeInfo().add(programCodeInfo);
				           loyaltyProgram.setProgramCodesList(value10);
		    

				           
			
			value7.getLoyaltyProgram().add(loyaltyProgram);
			value6.setLoyaltyProgramList(value7);
			
			
			request.setLoyaltyRequestFilterParamsType(value6);*/
			LoyaltyRequestParamsType loyaltyReqParmsType = new LoyaltyRequestParamsType();
			AccountList value5 = new AccountList();
			AccountInfo accountInfo = new AccountInfo();
				Account account = new Account();
						account.setAccountNumber(accountNumbers.get(0));
						account.setAccountType("CARD_ACCOUNT_NUMBER");
						accountInfo.getAccount().add(account);
					value5.getAccountInfo().add(accountInfo);
					loyaltyReqParmsType.setAccountList(value5);
			request.setLoyaltyRequestParamsType(loyaltyReqParmsType);

			//loyaltyReqParmsType.setAccountList(accountNumber);
			LoyaltyRequestFilterParamsType loyaltyReqFltParams = new LoyaltyRequestFilterParamsType();
			LoyaltyProgramList loyaltyProgLst = new LoyaltyProgramList();

			
			// COMMON FOR ALL PROGRAMS
			ProgramCodesList programCodesList = new ProgramCodesList();
			ProgramCodeInfo programCodeInfo = new ProgramCodeInfo();
			programCodeInfo.setProgramCode("ALL");
			ExtendedEntityList extendedEntityList = new ExtendedEntityList();
			ExtendedEntityInfo extendedEntityInfo = new ExtendedEntityInfo();
			extendedEntityInfo.setTransactionType("ACCOUNT_INSTRUMENT_INFO");
			extendedEntityList.getExtendedEntityInfo().add(extendedEntityInfo);
			programCodeInfo.setExtendedEntityList(extendedEntityList);
			programCodesList.getProgramCodeInfo().add(programCodeInfo);


			//REBATE_PROGRAMS
			LoyaltyProgram rebateLoyaltyProgram = new LoyaltyProgram();
			rebateLoyaltyProgram.setProgramType("REBATE_PROGRAMS");
			rebateLoyaltyProgram.setLoyaltyAccountStatus("ACTIVE");
			rebateLoyaltyProgram.setRrPeriodInd("CURR");
			rebateLoyaltyProgram.setRrEndDT("N");
			rebateLoyaltyProgram.setProgramCodesList(programCodesList);
			loyaltyProgLst.getLoyaltyProgram().add(rebateLoyaltyProgram);

			//REWARD_PROGRAMS
			LoyaltyProgram rewardsLoyaltyProgram = new LoyaltyProgram();
			rewardsLoyaltyProgram.setProgramType("REWARD_PROGRAMS");
			rewardsLoyaltyProgram.setLoyaltyAccountStatus("ACTIVE");
			ProgramCodesList programCodesListReward = new ProgramCodesList();
			ProgramCodeInfo programCodeInfoReward = new ProgramCodeInfo();
			RequestExtensions requestExtensions= new RequestExtensions();
			RequestExtension activeReqExtension = new RequestExtension();
			activeReqExtension.setName("CM_STATUS_FILTER");
			activeReqExtension.setValue("ACTIVE");
			RequestExtension allReqExtension = new RequestExtension();
			allReqExtension.setName("CM_STATUS_FILTER_2");
			allReqExtension.setValue("ALL");
			ExtendedEntityList extendedEntityListReward = new ExtendedEntityList();
			ExtendedEntityInfo extendedEntityInfoReward = new ExtendedEntityInfo();
			requestExtensions.getRequestExtension().add(activeReqExtension);
			requestExtensions.getRequestExtension().add(allReqExtension);
			extendedEntityInfoReward.setRequestExtensions(requestExtensions);
			extendedEntityInfoReward.setTransactionType("ACCOUNT_INSTRUMENT_INFO");
			extendedEntityListReward.getExtendedEntityInfo().add(extendedEntityInfoReward);
			programCodeInfoReward.setProgramCode("ALL");
			programCodeInfoReward.setExtendedEntityList(extendedEntityListReward);
			programCodesListReward.getProgramCodeInfo().add(programCodeInfoReward);
			rewardsLoyaltyProgram.setProgramCodesList(programCodesListReward);
			loyaltyProgLst.getLoyaltyProgram().add(rewardsLoyaltyProgram);

			//OPENSAVINGS_PROGRAMS
			LoyaltyProgram openSavLoyaltyProgram = new LoyaltyProgram();
			openSavLoyaltyProgram.setProgramType("OPENSAVINGS_PROGRAMS");
			openSavLoyaltyProgram.setLoyaltyAccountStatus("ACTIVE");
			openSavLoyaltyProgram.setProgramCodesList(programCodesList);
			loyaltyProgLst.getLoyaltyProgram().add(openSavLoyaltyProgram);
			
			//COBRAND_PROGRAMS
			LoyaltyProgram cobrandLoyaltyProgram = new LoyaltyProgram();
			cobrandLoyaltyProgram.setProgramType("COBRAND_PROGRAMS");
			cobrandLoyaltyProgram.setLoyaltyAccountStatus("ACTIVE");
			ProgramCodesList programCodesListCobrand = new ProgramCodesList();
			ProgramCodeInfo programCodeInfoCobrand = new ProgramCodeInfo();
			ExtendedEntityList extendedEntityListCobrand = new ExtendedEntityList();
			ExtendedEntityInfo extendedEntityInfoCobrandInfo = new ExtendedEntityInfo();
			ExtendedEntityInfo extendedEntityInfoCobrandInstruments = new ExtendedEntityInfo();
			extendedEntityInfoCobrandInfo.setTransactionType("ACCOUNT_INFO");
			extendedEntityInfoCobrandInstruments.setTransactionType("ASSOCIATED_INSTRUMENTS");
			extendedEntityListCobrand.getExtendedEntityInfo().add(extendedEntityInfoCobrandInfo);
			extendedEntityListCobrand.getExtendedEntityInfo().add(extendedEntityInfoCobrandInstruments);
			programCodeInfoCobrand.setProgramCode("ALL");
			programCodeInfoCobrand.setExtendedEntityList(extendedEntityListCobrand);
			programCodesListCobrand.getProgramCodeInfo().add(programCodeInfoCobrand);
			cobrandLoyaltyProgram.setProgramCodesList(programCodesListCobrand);
			loyaltyProgLst.getLoyaltyProgram().add(cobrandLoyaltyProgram);

			loyaltyReqFltParams.setLoyaltyProgramList(loyaltyProgLst);

			request.setCommonRequestContextType(commonRequestContextType);
			request.setLoyaltyRequestParamsType(loyaltyReqParmsType);
			request.setLoyaltyRequestFilterParamsType(loyaltyReqFltParams);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}
}