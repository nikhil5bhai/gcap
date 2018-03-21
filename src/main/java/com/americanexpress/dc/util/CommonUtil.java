package com.americanexpress.dc.util;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.americanexpress.as.schemas.fsresponseservice._2.FinancialServiceResponse;
import com.americanexpress.schemas.as.creditstatusservice._1.GlobalCreditStatusResponseDef;


public class CommonUtil {

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String buildSoapEnvresponseCSS(GlobalCreditStatusResponseDef globalCreditStatusResponseDef) throws JAXBException, SOAPException {

		JAXBContext jaxbContext = JAXBContext.newInstance(GlobalCreditStatusResponseDef.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		JAXBElement<GlobalCreditStatusResponseDef> jaxbElement = new JAXBElement<>(new QName("GlobalCreditStatusResponse"), GlobalCreditStatusResponseDef.class, globalCreditStatusResponseDef);
		
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		jaxbMarshaller.marshal(jaxbElement, sw);
		//String newTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String xmlString = sw.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
		//System.out.println("xml string >>"+xmlString);
        
       
		//document.put("response", xmlString);
		  // Start the API
	    MessageFactory messageFactory = MessageFactory.newInstance();
	    SOAPMessage message = messageFactory.createMessage();
	   // SOAPHeader header = message.getSOAPHeader();
	    SOAPBody body = message.getSOAPBody();

	    // Get the body. How do I add the raw xml directly into the body?
	    body.addTextNode(xmlString);

	    DOMSource source = new DOMSource(body);
	    StringWriter stringResult = new StringWriter();
	    try {
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
		} catch (TransformerException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		
		String xmlStr = stringResult.toString().replace("&lt;", "<").replace("&gt;", ">");
    	
	    String soapEnvStartTag ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
	    String soapEnvEndTag ="</soapenv:Envelope>";

	    String str=xmlStr.replace("<GlobalCreditStatusResponse xmlns:ns2=\"http://www.americanexpress.com/schemas/as/creditstatusservice/1\" xmlns:ns4=\"http://www.americanexpress.com/schemas/as/globalmsgdata/1\" xmlns:ns3=\"http://www.americanexpress.com/schemas/as/limitservice/1\" xmlns:ns9=\"http://www.americanexpress.com/schemas/as/globalservicedata/1\" xmlns:ns5=\"http://www.americanexpress.com/schemas/as/messagingservice/1\" xmlns:ns6=\"http://www.americanexpress.com/schemas/as/gloslimitservice/1\" xmlns:ns7=\"http://www.americanexpress.com/schemas/as/arlimitservice/1\" xmlns:ns8=\"http://www.americanexpress.com/schemas/as/travellimitservice/1\">","<ns2:GlobalCreditStatusResponse xmlns:ns6=\"http://www.americanexpress.com/schemas/as/gloslimitservice/1\" xmlns:ns5=\"http://www.americanexpress.com/schemas/as/messagingservice/1\" xmlns:ns8=\"http://www.americanexpress.com/schemas/as/travellimitservice/1\" xmlns:ns7=\"http://www.americanexpress.com/schemas/as/arlimitservice/1\" xmlns:ns9=\"http://www.americanexpress.com/schemas/as/globalservicedata/1\" xmlns:ns2=\"http://www.americanexpress.com/schemas/as/creditstatusservice/1\" xmlns:ns4=\"http://www.americanexpress.com/schemas/as/globalmsgdata/1\" xmlns:ns3=\"http://www.americanexpress.com/schemas/as/limitservice/1\">").replace("</GlobalCreditStatusResponse>", "</ns2:GlobalCreditStatusResponse>");
	    String resp = soapEnvStartTag+str+soapEnvEndTag;
	    String modifedxmlResp= resp.replace("SOAP-ENV", "soapenv").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		return modifedxmlResp;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String buildSoapEnvresponseFins(FinancialServiceResponse financialServiceResponse) throws JAXBException, SOAPException {

		JAXBContext jaxbContext = JAXBContext.newInstance(FinancialServiceResponse.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		JAXBElement<FinancialServiceResponse> jaxbElement = new JAXBElement<>( new QName( "ns2:getCardFinancialsResult" ), FinancialServiceResponse.class, financialServiceResponse );
		
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		jaxbMarshaller.marshal(jaxbElement, sw);
		String xmlString = sw.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
		//System.out.println("xml string >>"+xmlString);
        
       
		//document.put("response", xmlString);
		  // Start the API
	    MessageFactory messageFactory = MessageFactory.newInstance();
	    SOAPMessage message = messageFactory.createMessage();
	    SOAPHeader header = message.getSOAPHeader();
	    SOAPBody body = message.getSOAPBody();

	    // Get the body. How do I add the raw xml directly into the body?
	    body.addTextNode(xmlString);

	    DOMSource source = new DOMSource(body);
	    StringWriter stringResult = new StringWriter();
	    try {
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
		} catch (TransformerException
				| TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		
		String xmlStr = stringResult.toString().replace("&lt;", "<").replace("&gt;", ">");

	    String soapEnvStartTag ="<SOAP-ENV:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
	    String soapEnvEndTag ="</SOAP-ENV:Envelope>";
	    
	    String resp = soapEnvStartTag+xmlStr+soapEnvEndTag;
	    String resp1=resp.replace("<ns2:cardFinancials>", "<ns2:return><ns2:cardFinancials>").replace("</ns2:cardFinancials>", "</ns2:cardFinancials></ns2:return>");
	    String modifedxmlResp= resp1.replace("SOAP-ENV", "soapenv").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
	    System.out.println(resp.replace("SOAP-ENV", "soapenv").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
		return modifedxmlResp;
	}
	
	/**
	 * Method to return the XMLGregorianCalendar date.
	 *
	 * @param date
	 *            Date
	 * @return xmlDate XMLGregorianCalendar
	 */
	public static XMLGregorianCalendar dateToXmlDate(Date date) {
		final String METHOD_NAME = "dateToXmlDate";
		XMLGregorianCalendar xmlDate = null;
		if (date != null) {
			GregorianCalendar gcal = (GregorianCalendar) Calendar.getInstance();
			gcal.setTime(date);
			try {
				xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			} catch (DatatypeConfigurationException ex) {

			}
		}
		return xmlDate;
	}


	public static XMLGregorianCalendar convertStringToXMLGregCal(String sourceDate) {

		XMLGregorianCalendar finalDate = null;
		Date srcUtilDate;

		DateFormat srcformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			srcUtilDate = srcformat.parse(sourceDate);
		} catch(ParseException pe) {

			return null;
		}
		String destDate = destFormat.format(srcUtilDate);

		try {
			finalDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(destDate);
		} catch(DatatypeConfigurationException dce) {

		}
		return finalDate;

	}


	public static BigDecimal convertBigDecimalToWholeNumber(Double doubleAmount) {

		BigDecimal bigDecimalAmt = null;
		String amountString = null;
		if (doubleAmount != null) {
			amountString = convertBigDecimalToPercentage(BigDecimal.valueOf(doubleAmount));
		}

		bigDecimalAmt = new BigDecimal(amountString).setScale(0, RoundingMode.UNNECESSARY);

		return bigDecimalAmt;
	}


	public static String convertBigDecimalToPercentage(BigDecimal bigDecimalAmount) {

		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		String amount = null;
		if (bigDecimalAmount != null) {
			amount = decimalFormat.format(bigDecimalAmount.multiply(new BigDecimal(100))).toString();
		}
		return amount;
	}

	public static String getRACFIDForISL(){
		return Constants.DEFAULT_RACFID;
	}


	public static String getDefaultSORRegion(){
		return Constants.AR_SOR_REGION_ID;
	}


}
