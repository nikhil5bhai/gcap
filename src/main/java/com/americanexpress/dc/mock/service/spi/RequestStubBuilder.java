package com.americanexpress.dc.mock.service.spi;


import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.util.Map;



public interface RequestStubBuilder
{

  RequestPatternBuilder build( Map<String, Object> context );
}
