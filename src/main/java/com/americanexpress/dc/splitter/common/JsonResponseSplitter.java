package com.americanexpress.dc.splitter.common;


import com.google.gson.Gson;
import com.google.gson.JsonElement;


/** @author Richard Wilson */
public abstract class JsonResponseSplitter extends AbstractResponseSplitter<JsonElement>
{

  protected static final Gson GSON = new Gson();

  public JsonResponseSplitter( final Integer... validStatuses )
  {
    super( validStatuses );
  }

  @Override
  protected JsonElement convertTo( final String content )
  {
    return GSON.fromJson( content, JsonElement.class );
  }
}
