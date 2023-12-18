package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;
 

public class Alb01Stack extends Stack {
  
	public Alb01Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productTopic) {
        this(scope, id, null, cluster, productTopic);
    }

    public Alb01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productTopic) {
        super(scope, id, props);
        
        
      final Map<String, String> envVariables = new HashMap<>();
      envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint") + ":3306/aws-client-db?createDatabaseIfNotExist=true");
      envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
      envVariables.put("AWS_REGION", "us-east-1");
      envVariables.put("AWS_SNS_TOPIC_PRODUCT_ARN", productTopic.getTopic().getTopicArn());
      envVariables.put("SPRING_PROFILES_ACTIVE", "prod");
      envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));
        
      final  ApplicationLoadBalancedFargateService alb = ApplicationLoadBalancedFargateService.Builder.create(this, "alb-01")
        		.serviceName("service-01")
        		.cluster(cluster)
        		.cpu(512)
        		.desiredCount(2)
        		.listenerPort(8080)
        		.memoryLimitMiB(1024)
        		.taskImageOptions(ApplicationLoadBalancedTaskImageOptions
        				.builder()
        				.containerName("aws-client")
        				.image(ContainerImage.fromRegistry("breno500as/aws-client:1.7.0"))
        				.containerPort(8080)
        				.logDriver(LogDriver
        						.awsLogs(AwsLogDriverProps.builder()
        								.logGroup(LogGroup.Builder.create(this, "Service01LogGroup")
        										.logGroupName("Service01")
        										.removalPolicy(RemovalPolicy.DESTROY).build())
        								.streamPrefix("Service01LogGroup").build()))
        				.environment(envVariables)
        				.build())
        		.publicLoadBalancer(true)
        		.build();
      
      
      alb.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
    	        .path("/actuator/health")
    	        .port("8080")
    	        .healthyHttpCodes("200")
    	        .build());
      
      
      ScalableTaskCount scalableTaskCount = alb.getService().autoScaleTaskCount(
    		  EnableScalingProps.builder()
    		  .minCapacity(2)
    		  .maxCapacity(4)
    		  .build());
      
      scalableTaskCount.scaleOnMemoryUtilization("Alb01AutoScalling", 
    		  MemoryUtilizationScalingProps.builder()
    		  .targetUtilizationPercent(50)
    		  .scaleInCooldown(Duration.seconds(60))
    		  .scaleOutCooldown(Duration.seconds(60))
    		  .build());
      
      productTopic.getTopic().grantPublish(alb.getTaskDefinition().getTaskRole());

 
    }
}
