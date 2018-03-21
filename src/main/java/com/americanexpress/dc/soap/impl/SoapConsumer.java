package com.americanexpress.dc.soap.impl;

import java.util.List;

public interface SoapConsumer {

	public Object buildRequest(List<String> accountNumbers, String userId) ;
}
