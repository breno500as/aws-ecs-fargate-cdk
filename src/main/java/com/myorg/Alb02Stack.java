package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class Alb02Stack extends Stack {
	
	  public Alb02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productTopic, Table productEventsTable, Queue productQueue) {
	        this(scope, id, null, cluster, productTopic, productEventsTable,productQueue);
	    }

	    public Alb02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productTopic, Table productEventsTable, Queue productQueue) {
	        super(scope, id, props);
 

	       
	       final SqsSubscription sqsSubscription = SqsSubscription.Builder.create(productQueue).build();
	       productTopic.getTopic().addSubscription(sqsSubscription);
	       
	       final Map<String, String> envVariables = new HashMap<>();
	       envVariables.put("AWS_REGION", "us-east-1");
	       envVariables.put("AWS_SQS_QUEUE_PRODUCT_NAME", productQueue.getQueueName());

	   

	        final ApplicationLoadBalancedFargateService alb02 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB02")
	                .serviceName("alb-02")
	                .cluster(cluster)
	                .cpu(512)
	                .memoryLimitMiB(1024)
	                .desiredCount(2)
	                .listenerPort(9090)
	                .taskImageOptions(
	                        ApplicationLoadBalancedTaskImageOptions.builder()
	                                .containerName("aws_project02")
	                                .image(ContainerImage.fromRegistry("breno500as/aws-listener:1.3.0"))
	                                .containerPort(9090)
	                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
	                                        .logGroup(LogGroup.Builder.create(this, "Service02LogGroup")
	                                                .logGroupName("Service02")
	                                                .removalPolicy(RemovalPolicy.DESTROY)
	                                                .build())
	                                        .streamPrefix("Service02")
	                                        .build()))
	                                .environment(envVariables)
	                                .build())
	                .publicLoadBalancer(true)
	                .build();

	        alb02.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
	                .path("/actuator/health")
	                .port("9090")
	                .healthyHttpCodes("200")
	                .build());

	        ScalableTaskCount scalableTaskCount = alb02.getService().autoScaleTaskCount(EnableScalingProps.builder()
	                .minCapacity(2)
	                .maxCapacity(4)
	                .build());

	        scalableTaskCount.scaleOnCpuUtilization("Alb02AutoScalling", CpuUtilizationScalingProps.builder()
	                .targetUtilizationPercent(50)
	                .scaleInCooldown(Duration.seconds(60))
	                .scaleOutCooldown(Duration.seconds(60))
	                .build());
	        
	        productQueue.grantConsumeMessages(alb02.getTaskDefinition().getTaskRole());
	        
	        productEventsTable.grantReadWriteData(alb02.getTaskDefinition().getTaskRole());

 
	      
	    }

}
