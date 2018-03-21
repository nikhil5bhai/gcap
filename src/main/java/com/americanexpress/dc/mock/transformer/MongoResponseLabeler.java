package com.americanexpress.dc.mock.transformer;


import static com.americanexpress.dc.util.NamingUtil.kebabCase;



public class MongoResponseLabeler extends AbstractMongoResponseTransformer
{

  public static final String NAME = kebabCase( MongoResponseLabeler.class.getSimpleName() );

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public boolean applyGlobally()
  {
    return false;
  }
}
