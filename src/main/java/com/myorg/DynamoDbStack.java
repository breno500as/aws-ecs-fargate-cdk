package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.UtilizationScalingProps;
import software.constructs.Construct;

public class DynamoDbStack extends Stack {

	private Table productsEventsTable;

	public DynamoDbStack(final Construct scope, final String id) {
		super(scope, id);
		
		productsEventsTable = Table.Builder.create(this, "ProductsEventsTable")
				.tableName("product-events")
				.readCapacity(1)
				.writeCapacity(1)
				.billingMode(BillingMode.PROVISIONED)
				.partitionKey(Attribute.builder().name("pk").type(AttributeType.STRING).build())
				.sortKey(Attribute.builder().name("sk").type(AttributeType.STRING).build())
				.timeToLiveAttribute("ttl")
				.removalPolicy(RemovalPolicy.DESTROY).build();

		productsEventsTable.autoScaleReadCapacity(EnableScalingProps.builder()
				                                      .minCapacity(1)
				                                      .maxCapacity(4).build())
				            .scaleOnUtilization(UtilizationScalingProps.builder().targetUtilizationPercent(50)
						                                               .scaleInCooldown(Duration.seconds(30))
						                                               .scaleOutCooldown(Duration.seconds(30))
						                                               .build());
		
		
		productsEventsTable.autoScaleWriteCapacity(EnableScalingProps.builder()
				                                                     .minCapacity(1)
				                                                     .maxCapacity(4).build())
		                   .scaleOnUtilization(UtilizationScalingProps.builder().targetUtilizationPercent(50)
				                                                      .scaleInCooldown(Duration.seconds(30))
				                                                      .scaleOutCooldown(Duration.seconds(30))
				                                                      .build());
	}

	

	public Table getProductsEventsTable() {
		return productsEventsTable;
	}

}
