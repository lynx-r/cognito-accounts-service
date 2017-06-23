package com.workingbit.accounts.service;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.workingbit.accounts.common.EnumRole;
import com.workingbit.accounts.common.StringMap;
import com.workingbit.accounts.config.AwsProperties;
import com.workingbit.accounts.exception.DataAccessException;
import com.workingbit.accounts.exception.UnauthorizedException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 13:59 15/06/2017.
 */
@Service
public class AmazonCognitoIdentityService {

  private final AmazonCognitoIdentity amazonCognitoIdentity;
  private final DynamoDbService dynamoDbService;
  private final OAuthClientService oAuthClientService;
  private final AwsProperties awsProperties;

  @Autowired
  public AmazonCognitoIdentityService(DynamoDbService dynamoDbService,
                                      OAuthClientService oAuthClientService,
                                      AwsProperties awsProperties) {
    this.amazonCognitoIdentity = AmazonCognitoIdentityClient.builder()
        .withRegion(awsProperties.getRegion())
        .build();
    this.dynamoDbService = dynamoDbService;
    this.oAuthClientService = oAuthClientService;
    this.awsProperties = awsProperties;
  }

  /**
   * send a get id request. This only needs to be executed the first time
   * and the result should be cached.
   *
   * @param username
   * @param email
   *@param logins  @return
   */
    private String getIdentityId(String username, String email, Map<String, String> logins) throws DataAccessException {
    GetIdRequest idRequest = new GetIdRequest()
        .withAccountId(awsProperties.getClientId())
        .withIdentityPoolId(awsProperties.getIdentityPoolId())
        .withLogins(logins);
    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
    GetIdResult idResult = amazonCognitoIdentity.getId(idRequest);
    String identityId = idResult.getIdentityId();
    String storedIdentityId = dynamoDbService.retrieveByIdentityId(identityId);
    if (!identityId.equalsIgnoreCase(storedIdentityId)) {
      dynamoDbService.storeIdentityId(username, email, identityId);
    }
    return identityId;
  }

  public StringMap getId(String username, String facebookAccessToken) throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException {
    Map<String, String> providerTokens = new HashMap<>();
    providerTokens.put(awsProperties.getFacebookProviderName(), facebookAccessToken);
    StringMap userDetailsFromFacebook = oAuthClientService.getUserDetailsFromFacebook(facebookAccessToken);
    String email = userDetailsFromFacebook.getString(awsProperties.getAttributeEmail());
    String identityId = getIdentityId(username, email, providerTokens);

    StringMap resp = new StringMap();
    resp.put(awsProperties.getAttributeIdentityId(), identityId);
    return resp;
  }

  public StringMap getCredentialsForIdentityFacebook(String facebookAccessToken, String identityId)
      throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException, UnauthorizedException {
    StringMap userDetailsFromFacebook = oAuthClientService.getUserDetailsFromFacebook(facebookAccessToken);
    String username = dynamoDbService.retrieveByIdentityId(identityId);
    if (null == username) {
      throw new UnauthorizedException("User is not registered");
    }
    Map<String, String> logins = new HashMap<>();
    logins.put(awsProperties.getFacebookProviderName(), facebookAccessToken);
    return getCredentialsForIdentity(identityId, username, userDetailsFromFacebook, logins);
  }

  public String getOpenIdToken(String username) throws DataAccessException {
    String identityId = dynamoDbService.retrieveByUsername(username).get(awsProperties.getAttributeIdentityId()).getS();
    // Create the request object
    GetOpenIdTokenRequest tokenRequest = new GetOpenIdTokenRequest();
    tokenRequest.setIdentityId(identityId);
// If you are authenticating your users through an identity provider
// then you can set the Map of tokens in the request
// Map providerTokens = new HashMap();
// providerTokens.put("graph.facebook.com", "facebook session key");
// tokenRequest.setLogins(providerTokens);

    GetOpenIdTokenResult tokenResp = amazonCognitoIdentity.getOpenIdToken(tokenRequest);
// get the OpenID token from the response
    return tokenResp.getToken();
  }

  public String assumeRoleWithWebIdentity(String openIdToken, EnumRole enumRole) {
    AnonymousAWSCredentials anonymousAWSCredentials = new AnonymousAWSCredentials();
    // you can now create a set of temporary, limited-privilege credentials to access
// your AWS resources through the Security Token Service utilizing the OpenID
// token returned by the previous API call. The IAM Role ARN passed to this call
// will be applied to the temporary credentials returned
    AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClient.builder()
        .withCredentials(new AWSStaticCredentialsProvider(anonymousAWSCredentials))
        .build();
    AssumeRoleWithWebIdentityRequest stsReq = new AssumeRoleWithWebIdentityRequest();
    stsReq.setRoleArn(enumRole.getRoleArn());
    stsReq.setWebIdentityToken(openIdToken);
    stsReq.setRoleSessionName("AppTestSession");

    AssumeRoleWithWebIdentityResult stsResp = stsClient.assumeRoleWithWebIdentity(stsReq);
    com.amazonaws.services.securitytoken.model.Credentials stsCredentials = stsResp.getCredentials();

// Create the session credentials object
    AWSSessionCredentials sessionCredentials = new BasicSessionCredentials(
        stsCredentials.getAccessKeyId(),
        stsCredentials.getSecretAccessKey(),
        stsCredentials.getSessionToken()
    );
// save the timeout for these credentials
    Date sessionCredentialsExpiration = stsCredentials.getExpiration();
    return sessionCredentials.getSessionToken();
// these credentials can then be used to initialize other AWS clients,
// for workingbit the Amazon Cognito Sync client
//    AmazonCognitoSync syncClient = new AmazonCognitoSyncClient(sessionCredentials);
//    ListDatasetsRequest syncRequest = new ListDatasetsRequest();
//    syncRequest.setIdentityId(idResp.getIdentityId());
//    syncRequest.setIdentityPoolId("YOUR_COGNITO_IDENTITY_POOL_ID");
//    ListDatasetsResult syncResp = syncClient.listDatasets(syncRequest);
  }

  private StringMap getCredentialsForIdentity(String identityId, String username, StringMap userDetails, Map<String, String> logins)
      throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException, UnauthorizedException {
    // Create the request object
    GetCredentialsForIdentityRequest tokenRequest = new GetCredentialsForIdentityRequest()
        .withIdentityId(identityId)
        // If you are authenticating your users through an identity provider
        // then you can set the Map of tokens in the request
        .withLogins(logins);

    GetCredentialsForIdentityResult tokenResp = amazonCognitoIdentity.getCredentialsForIdentity(tokenRequest);
    // get the OpenID token from the response
    Credentials credentials = tokenResp.getCredentials();

    // save the timeout for these credentials
    StringMap resp = new StringMap();
    resp.put(awsProperties.getAwsSessionToken(), credentials.getSessionToken());
//    BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(credentials.getAccessKeyId(), credentials.getSecretKey());
//    AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
//    AmazonApiGateway amazonApiGatewayClientBuilder = AmazonApiGatewayClientBuilder
//        .standard()
//        .withCredentials(awsCredentialsProvider)
//        .withRegion(awsProperties.getRegion())
//        .build();
    return resp;
  }

}
