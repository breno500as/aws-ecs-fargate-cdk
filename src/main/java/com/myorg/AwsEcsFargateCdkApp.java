package com.myorg;

import software.amazon.awscdk.App;

public class AwsEcsFargateCdkApp {
	
	
	public static final String AWS_REGION = "us-east-1";
	
	public static final String SPRING_PROFILE_ACTIVE = "prod";
	
	
	public static void main(final String[] args) {
		
		final App app = new App();

		final VpcStack vpcStack = new VpcStack(app, "Vpc");

		final ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
		clusterStack.addDependency(vpcStack);

		final RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
		rdsStack.addDependency(vpcStack);

		final SnsStack snsStack = new SnsStack(app, "Sns");
		
		final S3Stack s3Stack = new S3Stack(app, "S3");
		
		final DynamoDbStack dynamodbStack = new DynamoDbStack(app, "DynamoDB");
		
		final SqsStack sqsStack = new SqsStack(app, "Sqs");

		final Alb01Stack alb01Stack = new Alb01Stack(app, "Alb01", clusterStack.getCluster(),
				snsStack.getProductTopic(),s3Stack.getBucket(), s3Stack.getS3InvoiceQueue());
		alb01Stack.addDependency(clusterStack);
		alb01Stack.addDependency(rdsStack);
		alb01Stack.addDependency(snsStack);
		alb01Stack.addDependency(s3Stack);
		
		
		final Alb02Stack service02Stack = new Alb02Stack(app, "Alb02", clusterStack.getCluster(),
				snsStack.getProductTopic(), dynamodbStack.getProductsEventsTable(), sqsStack.getProductQueue());
		service02Stack.addDependency(clusterStack);
		service02Stack.addDependency(sqsStack);
		service02Stack.addDependency(dynamodbStack);

		app.synth();
	}
}
