package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamoDbStack extends Stack {

	private Table productsEventsTable;

	public DynamoDbStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public DynamoDbStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);
		
		
		productsEventsTable = Table.Builder.create(this, "ProductsEventsTable")
				.tableName("product-events")
				.readCapacity(1)
				.writeCapacity(1)
				.billingMode(BillingMode.PROVISIONED)
				.partitionKey(Attribute.builder().name("pk").type(AttributeType.STRING).build())
				.sortKey(Attribute.builder().name("sk").type(AttributeType.STRING).build())
				.timeToLiveAttribute("ttl")
				.removalPolicy(RemovalPolicy.DESTROY)
				.build();

	}

	public Table getProductsEventsTable() {
		return productsEventsTable;
	}

}
