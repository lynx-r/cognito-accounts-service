package com.workingbit.echo;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
  private static SpringLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

  private static SpringLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> getHandler() {
    if (null == handler) {
      try {
        handler = SpringLambdaContainerHandler.getAwsProxyHandler(EchoApplication.class);
      } catch (ContainerInitializationException e) {
        e.printStackTrace();
      }
    }
    return handler;
  }

  public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
    return getHandler().proxy(awsProxyRequest, context);
  }

}