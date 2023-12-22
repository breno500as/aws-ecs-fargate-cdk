package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.constructs.Construct;

public class ClusterStack extends Stack {

	private Cluster cluster;

	public ClusterStack(final Construct scope, final String id, Vpc vpc) {
		super(scope, id);
		
		this.cluster = Cluster.Builder.create(this, id).clusterName("cluster-01").vpc(vpc).build();
	}

	public Cluster getCluster() {
		return cluster;
	}

}
