package com.workingbit;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.glassfish.jersey.server.ResourceConfig;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
  private ResourceConfig jerseyApplication = new ResourceConfig().packages("com.workingbit");
  private JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler
      = JerseyLambdaContainerHandler.getAwsProxyHandler(jerseyApplication);

  public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
    return handler.proxy(awsProxyRequest, context);
  }
}