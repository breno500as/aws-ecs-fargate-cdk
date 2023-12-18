package com.myorg;

import software.amazon.awscdk.App;

public class AwsEcsFargateCdkApp {
	public static void main(final String[] args) {
		App app = new App();

		final VpcStack vpcStack = new VpcStack(app, "Vpc");

		final ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
		clusterStack.addDependency(vpcStack);

		final RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
		rdsStack.addDependency(vpcStack);

		final SnsStack snsStack = new SnsStack(app, "Sns");

		final Alb01Stack alb01Stack = new Alb01Stack(app, "Alb01", clusterStack.getCluster(),
				snsStack.getProductTopic());
		alb01Stack.addDependency(clusterStack);
		alb01Stack.addDependency(rdsStack);
		alb01Stack.addDependency(snsStack);

		final DynamoDbStack dynamodbStack = new DynamoDbStack(app, "DynamoDB");
		
		final SqsStack sqsStack = new SqsStack(app, "Sqs");

		final Alb02Stack service02Stack = new Alb02Stack(app, "Alb02", clusterStack.getCluster(),
				snsStack.getProductTopic(), dynamodbStack.getProductsEventsTable(), sqsStack.getProductQueue());
		service02Stack.addDependency(clusterStack);
		service02Stack.addDependency(sqsStack);
		service02Stack.addDependency(dynamodbStack);

		app.synth();
	}
}
