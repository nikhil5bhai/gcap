package com.americanexpress.dc.util;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "Fragment")
@XmlEnum
public enum Fragment {

    MR_ACCT_SUMMRY,
    MR_REWARDS_BALANCE,
    MR_ACCT_DATA,
    MR_ACCT_DETAILS,
    MR_OFFER_DATA,
    MR_CARD_DATA,
    MR_MBR_ID,
    CARD,
    MR_FAVORITE_PLACES,
    MR_DASHBOARD_OFFERS,
    MR_PROFILE_DATA;

    public String value() {
        return name();
    }

    public static Fragment fromValue(String v) {
        return valueOf(v);
    }

}
