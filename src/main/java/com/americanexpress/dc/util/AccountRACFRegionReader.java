package com.americanexpress.dc.util;

import com.americanexpress.as.AcctRACFMappingManager;

public class AccountRACFRegionReader {

    private boolean isProd = true;
    private final String defaultRACFID;
    private final String defaultSORRegion;
    private static final AccountRACFRegionReader _instance = new AccountRACFRegionReader();

    private AccountRACFRegionReader() {
        String environment = System.getProperty("spring.profiles.active");
        isProd = environment.contains("E3");
        this.defaultRACFID = CommonUtil.getRACFIDForISL();
        defaultSORRegion = CommonUtil.getDefaultSORRegion();
    }

    /**
     * This method returns AccountRACFRegionReader instance.
     *
     * @return _instance
     */
    public static AccountRACFRegionReader getInstance() {
        return _instance;
    }

    /**
     * This method returns UserId.
     *
     * @return defaultRACFID
     */
    public String getUserId() {
        return defaultRACFID;
    }

    public String getRacfForAccount(String account) {
        final String methodName = "getRacfForAccount";
        String racfId = defaultRACFID;
        if (!isProd) {
            try {
                AcctRACFMappingManager acctRACFMappingManager = AcctRACFMappingManager.getInstance(AcctRACFMappingManager.MAPPINGTYPE.acctracf);
                if(acctRACFMappingManager != null) {
                    racfId = acctRACFMappingManager.getRacfForAccount(account);
                    if (racfId == null || "".equals(racfId)) {
                        racfId = defaultRACFID;
                    }
                }else {

                }
            } catch (Throwable ex) {

            }
        }

        return racfId;
    }

    /**
     * This method returns SOR Region for Account.
     *
     * @param
     * @return regionId
     */
    public String getRegionForAccount(String account) {
        final String methodName = "getRegionForAccount";
        String regionId = defaultSORRegion;
        if (!isProd) {
            try {
                AcctRACFMappingManager acctRACFMappingManager = AcctRACFMappingManager.getInstance(AcctRACFMappingManager.MAPPINGTYPE.acctracf);
                if(acctRACFMappingManager != null) {
                    regionId = acctRACFMappingManager.getRegionForAccount(account);
                    if (regionId == null || "".equals(regionId)) {
                        regionId = defaultSORRegion;
                    }
                }else {

                    regionId = defaultSORRegion;
                }
            } catch (Throwable ex) {

            }
        }

        return regionId;
    }
}
