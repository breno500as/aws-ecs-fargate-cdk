package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {

	private Vpc vpc;

	public VpcStack(final Construct scope, final String id) {
		super(scope, id);
		this.vpc = Vpc.Builder.create(this, "vpc-01").maxAzs(3).build();

	}

	 

	public Vpc getVpc() {
		return vpc;
	}

}
