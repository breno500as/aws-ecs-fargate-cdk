package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;
import software.constructs.Construct;

public class SqsStack extends Stack {
	
	
	 private Queue productQueue;

	public SqsStack(final Construct scope, final String id) {
		super(scope, id);
		
		   final Queue productDlqQueue = Queue.Builder.create(this, "ProductDlqQueue")
		  		   .queueName("product-dlq-queue")
		  		   .enforceSsl(false)
		  	       .encryption(QueueEncryption.UNENCRYPTED)
		  		   .build();
		     
		     final DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder()
		              .queue(productDlqQueue)
		              .maxReceiveCount(3)
		              .build();
		     
		     
		     productQueue = Queue.Builder.create(this, "ProductQueue")
		              .queueName("product-queue")
		              .enforceSsl(false)
		              .encryption(QueueEncryption.UNENCRYPTED)
		              .deadLetterQueue(deadLetterQueue)
		              .build();
	}

    public Queue getProductQueue() {
	 return productQueue;
    }
}
