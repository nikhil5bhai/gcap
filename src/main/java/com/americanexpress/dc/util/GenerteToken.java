package com.americanexpress.dc.util;


import com.americanexpress.as.cs.sap.SecurityAccessPoint;
import com.americanexpress.as.cs.sap.exception.SAPException;
import com.americanexpress.as.cs.sap.valuebean.ApplicationMap;
import com.americanexpress.as.cs.sap.valuebean.SecurityGUIDs;
import com.americanexpress.as.cs.sap.valuebean.SecurityServices;
import com.americanexpress.as.myca.web.pl.auth.SecurityAccess;
import com.americanexpress.wss.shr.authorization.token.SecurityToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;


public class GenerteToken
{


  @Mock
  static
  HttpServletRequest request;
  /*
	 *      * Retrieves the security token based on the user id and
	 * password.     *      * @param userId     *            String    
	 * * @param passwd     *            String     * @return secToken
	 * SecurityToken     * @throws Exception     *             exception    
	 */

  public SecurityToken getSecurityToken( String userId, String password ) throws Exception
  {
    try
    {
      System.setProperty( "spring.profiles.active", "E2_PROD" );
      System.setProperty( "Config.market", "US" );

      MockitoAnnotations.initMocks( this );

      SecurityToken securityToken = SecurityAccessPoint.getInstance().logon( userId, "flower1",
                                                                             SecurityServices.MYCA, ApplicationMap.CAZM );
      SecurityGUIDs guids = SecurityAccessPoint.getInstance().getGUIDs( securityToken, SecurityServices.MYCA,
                                                                        ApplicationMap.CAZM );
      com.americanexpress.as.security.chkreg.shr.SecurityGUIDs guidsSecurity = new com.americanexpress.as.security.chkreg.shr.SecurityGUIDs();
      guidsSecurity.setCSRPrivateGUID( guids.getCsrPrivateGUID() );
      guidsSecurity.setCSRPublicGUID( guids.getCsrPublicGUID() );
      System.out.println("Private Guid "+guids.getCsrPublicGUID());
      guidsSecurity.setPublicGUID( guids.getPublicGUIDs() );
      guidsSecurity.setPrivateGUID( guids.getPrivateGUIDs() );
      SecurityAccess.getInstance().addSecurityTokenToRequest( request, guidsSecurity, securityToken );
      getSecurityGuid( securityToken );
      return securityToken;
    }
    catch( Exception exc )
    {
      System.out.println( "Exception while creating claims " + exc.getMessage() );
      exc.printStackTrace();
      throw exc;
    }
  }


  public static SecurityGUIDs getSecurityGuid( SecurityToken securityToken )
  {
    SecurityGUIDs Guid = null;
    try
    {
      Guid = SecurityAccessPoint.getInstance().getGUIDs( securityToken, SecurityServices.MYCA, ApplicationMap.CAZM );
      System.out.println( "private Guid >>" + Guid.getPrivateGUIDs() );
      System.out.println( "public Guid >>" + Guid.getPublicGUIDs());
    }
    catch( SAPException e )
    {
      e.printStackTrace();
    }
    return Guid;
  }

  public static void main( String[] args ) throws Exception
  {
    GenerteToken generteToken = new GenerteToken();
    System.out.println( generteToken.getSecurityToken( "falc24138", "flower1" ).asXML() );
  }
}
