package com.americanexpress.dc.config;

import com.americanexpress.as.cs.shr.cps.CPS;

public class CPSFileResolver {

		private CPS cps;
		public CPSFileResolver() {
				cps = new CPS("mock_credentials.cps");
		}
		public CPS getCps() {
			return cps;
		}

}
