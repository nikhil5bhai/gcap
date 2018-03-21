/*
* -------------------------------------------------------------------------
*
* (C) Copyright / American Express, Inc. All rights reserved.
* The contents of this file represent American Express trade secrets and
* are confidential. Use outside of American Express is prohibited and in
* violation of copyright law.
*
* -------------------------------------------------------------------------
*/
package com.americanexpress.dc.bean.member.accounts;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * {@link LastLoginEntity} is a response entity class used to build Last Login
 * response.
 * 
 * @author akommu
 * @version 1.0
 */
public class LastLoginEntity  {
	private Long timestamp;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		return gson.toJson(this);
	}

}
