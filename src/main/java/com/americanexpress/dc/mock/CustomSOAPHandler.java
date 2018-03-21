package com.americanexpress.dc.mock;

import java.io.IOException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class CustomSOAPHandler implements SOAPHandler<SOAPMessageContext> {
	 @Override
	    public boolean handleMessage(SOAPMessageContext messageContext)  {
	        SOAPEnvelope envelope;
	        SOAPMessage msg = messageContext.getMessage();
			try {
				
					envelope = msg.getSOAPPart().getEnvelope();
					SOAPHeader header = envelope.getHeader();
					
			        SOAPElement corrId = header.addChildElement("corrId", "pre","http://www.americanexpress.com/services/as/sfwk/1");
			        corrId.addTextNode("PC165985b0002400232323812121212121");
			        
			        SOAPElement appId = header.addChildElement("appId", "pre","http://www.americanexpress.com/services/as/sfwk/1");
			        appId.addTextNode("ecommsvc/sdpref");
			        
			        SOAPElement bpToken = header.addChildElement("bpToken", "pre","http://www.americanexpress.com/services/as/sfwk/1");
			        bpToken.addTextNode("ECS_SIGN");
			        msg.writeTo(System.out);
			        System.out.println();
			        System.out.print("Successfully added headers!!!");
			} catch (SOAPException | IOException e) {
				e.printStackTrace();
			}
			
	        return true;
	    }

	@Override
	public void close(MessageContext arg0) {

	}

	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		return false;
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}
}