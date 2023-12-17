package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

public class SnsStack extends Stack {

	private SnsTopic productTopic;

	public SnsStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public SnsStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		this.productTopic = SnsTopic.Builder
				.create(Topic.Builder.create(this, "ProductTopic").topicName("product-topic").build()).build();
		
		
		this.productTopic.getTopic().addSubscription(EmailSubscription.Builder.create("breno500as@gmail.com").json(true).build());

	}

	public SnsTopic getProductTopic() {
		return productTopic;
	}

}
