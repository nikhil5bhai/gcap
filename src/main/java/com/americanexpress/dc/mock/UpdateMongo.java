package com.americanexpress.dc.mock;


import org.bson.Document;
import org.bson.conversions.Bson;

import com.americanexpress.dc.config.MongoConfiguration;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

public class UpdateMongo {
	public static void main(String args[])
	{
		MongoDatabase db = MongoConfiguration.getDatabase();
		MongoCollection<Document> collection = db.getCollection("transactions_pending");
		Bson rename = Updates.rename("account_tokens", "account_token");
		FindIterable<Document> d = collection.find(); 
		for(Document d1 : d)
		{
			collection.updateOne(d1, rename);
		}
		
	}
	
}
