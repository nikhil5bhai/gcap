package com.americanexpress.dc.util;

import static com.axp.myca.common.util.Logger.logInfo;
import static com.axp.myca.common.util.constants.Constants.SERVICE_NAME;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import com.americanexpress.as.sfwk.shr.ServiceConstants;
import com.americanexpress.dc.bean.UserAccountsBean;
import com.americanexpress.dc.soap.impl.ISLDirectDebitEnrollRequestBuilder;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.SetAccountInfoRespType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.RequestHeaderType;
import com.americanexpress.integrationservice.schema.ics.accountupdateservice.v5.globaldata.ResponseHeaderType;

public class ServiceInvoker {

	String cssResponse = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.axp.myca.util.IServiceInvoker#soapInvoker(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, int,
	 * java.lang.Object,
	 * com.americanexpress.wss.shr.authorization.token.SecurityToken)
	 */
	@SuppressWarnings("rawtypes")
	public String soapInvoker(Object request, final String serviceName, UserAccountsBean accountBean,
			String collectionName) throws Exception {
		Object response = null;

		App.ConfigProperties.loadServicePropertyFile("config_soap_service.properties");
		Properties serviceDetailsProperty = App.ConfigProperties.getServicePropertyEntrySet();
		System.out.println("collectionName is :::" + collectionName);
		final int timeout = Integer
				.parseInt(serviceDetailsProperty.getProperty(collectionName + "_TIME_OUT").toString());
		final String endpointURL = serviceDetailsProperty.getProperty(collectionName + "_ENDPOINT_URL").toString();
		final String serviceClassName = serviceDetailsProperty.getProperty(collectionName + "_SERVICE_CLASS_NAME")
				.toString();
		final String serviceMethodName = serviceDetailsProperty.getProperty(collectionName + "_SERVICE_METHOD_NAME")
				.toString();
		final String methodName = serviceDetailsProperty.getProperty(collectionName + "_METHOD_NAME").toString();
		System.out.println("The service name is " + serviceName);
		try {

			// LogFunctionHolder.setLogFunction("ServiceInvoker");

			Class<?> serviceClass = Class.forName(serviceClassName);
			Object serviceStub = serviceClass.newInstance();

			Method method = serviceClass.getMethod(serviceMethodName);
			Object interfaceObj = method.invoke(serviceStub);

			BindingProvider bp = (BindingProvider) interfaceObj;
			Map<String, Object> requestContext = bp.getRequestContext();
			((SOAPBinding) bp.getBinding()).setMTOMEnabled(true);

			Binding binding = bp.getBinding();
			List<Handler> handlerList = binding.getHandlerChain();
			// added to avoid null pointer exception
			if (handlerList == null) {
				handlerList = new ArrayList<Handler>();
			}

			handlerList.add(new SOAPHandler<SOAPMessageContext>() {
				@Override
				public boolean handleMessage(final SOAPMessageContext context) {
					try {
						Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
						if (outboundProperty.booleanValue()) {
							// Print Request
							System.out.println("Soap Request Message >>" + printSoapMessage(context.getMessage()));
						} else {
							// Print Response and return the same
							cssResponse = doStartProcess(context);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return true;
				}

				private String doStartProcess(SOAPMessageContext context) throws SOAPException {
					SOAPMessage message = context.getMessage();
					SOAPPart soapPart = message.getSOAPPart();
					SOAPEnvelope envelope = soapPart.getEnvelope();
					SOAPHeader header = envelope.getHeader();
					if (header == null) {
						header = envelope.addHeader();
					}
					QName clientOutboundQName = new QName(ServiceConstants.SFWK_NS_URI,
							ServiceConstants.CORRELATIONID_LOCAL_PART, ServiceConstants.SFWK_PREFIX);

					SOAPHeaderElement headerElement = header.addHeaderElement(clientOutboundQName);
					String correlationId = Constants.DEFAULT_CORRELATION_ID;
					headerElement.addTextNode(correlationId);
					message.saveChanges();

					String soapMsg = null;
					try {
						soapMsg = printSoapMessage(message);
						System.out.println("Soap Response Message >>" + soapMsg);
					} catch (TransformerFactoryConfigurationError | TransformerException e) {
						e.printStackTrace();
					}
					return soapMsg;
				}

				private void doEndProcess(SOAPMessageContext context, boolean bFault) {

				}

				@Override
				public boolean handleFault(SOAPMessageContext context) {

					final String function = "ClientLogging";
					if (context != null) {
						boolean request = ((Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY))
								.booleanValue();
						if (request) {
							logInfo(SERVICE_NAME, this, function, "SOAP Message from the Outbound Path-(toString)");
						} else {
							logInfo(SERVICE_NAME, this, function, "SOAP Message from the Inbound Path-(toString)");
						}
						try {
							System.out.println("Soap Fault Response :: " + printSoapMessage(context.getMessage()));

						} catch (SOAPException soapException) {
							soapException.printStackTrace();
						} catch (TransformerException e) {
							e.printStackTrace();
						}
					}
					return true;
				}

				@Override
				public void close(MessageContext context) {

				}

				@Override
				public Set<QName> getHeaders() {
					return null;
				}
			});

			binding.setHandlerChain(handlerList);

			requestContext.put("javax.xml.ws.service.endpoint.address", endpointURL);

			requestContext.put("timeout	", String.valueOf(timeout));
			requestContext.put("write_timeout	", String.valueOf(timeout));
			requestContext.put("connection_timeout", String.valueOf(timeout));

			Class<?> svcInterfaceClass = interfaceObj.getClass();

			// long currTime = Calendar.getInstance().getTimeInMillis();

			if(collectionName.contains(Constants.ACCOUNT_UPDATE_SERVICE_NAME)) {
				RequestHeaderType requestHeader = ISLDirectDebitEnrollRequestBuilder.buildRequestHeader(accountBean.getAccountNumber());
				Holder<ResponseHeaderType> responseHeaderHolder = new Holder<>();
				Holder<SetAccountInfoRespType> getAccountInfoRespHolder = new Holder<>();
				Method svcMethod = svcInterfaceClass.getMethod(methodName, requestHeader.getClass(), request.getClass(), responseHeaderHolder.getClass(), getAccountInfoRespHolder.getClass() );
				response = svcMethod.invoke(interfaceObj, requestHeader, request, responseHeaderHolder, getAccountInfoRespHolder);
			} else {
				Method svcMethod = svcInterfaceClass.getMethod(methodName, request.getClass());
				response = svcMethod.invoke(interfaceObj, request);
			}
		} catch (InvocationTargetException e) {
			System.out.println("This is an invocation error");
			e.printStackTrace();
			e.getCause();
		} catch (Exception e) {
			// exception caught while the service is timed out/busy.

			e.printStackTrace();
			throw new Exception();
		}
		
		if(cssResponse == null){
			cssResponse="<soapenv:Envelope>error</soapenv:Envelope>";
		}
		
		return cssResponse;
	}

	public static String printSoapMessage(final SOAPMessage soapMessage) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, SOAPException, TransformerException {
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();

		// Format it
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		final Source soapContent = soapMessage.getSOAPPart().getContent();

		final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		final StreamResult result = new StreamResult(streamOut);
		transformer.transform(soapContent, result);

		return streamOut.toString();
	}

}
