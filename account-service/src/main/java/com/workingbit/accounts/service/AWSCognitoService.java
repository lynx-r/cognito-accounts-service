package com.workingbit.accounts.service;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.workingbit.accounts.common.EnumRole;
import com.workingbit.accounts.common.StringMap;
import com.workingbit.accounts.exception.DataAccessException;
import com.workingbit.accounts.config.AwsProperties;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.*;

import static com.amazonaws.util.Base64.encodeAsString;

/**
 * Created by Aleksey Popryaduhin on 13:33 11/06/2017.
 */
@Service
public class AWSCognitoService {

  private final AwsProperties awsProperties;
  private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;
  private final AmazonCognitoIdentity amazonCognitoIdentity;
  private final DynamoDbService dynamoDbService;
  private final OAuthClientService oAuthClientService;

  @Autowired
  public AWSCognitoService(AwsProperties awsProperties,
                           DynamoDbService dynamoDbService,
                           OAuthClientService oAuthClientService) {
    this.awsProperties = awsProperties;
    this.awsCognitoIdentityProvider = AWSCognitoIdentityProviderClient.builder()
        .withRegion(awsProperties.getRegion())
        .build();
    this.amazonCognitoIdentity = AmazonCognitoIdentityClient.builder()
        .withRegion(awsProperties.getRegion())
        .build();
    this.dynamoDbService = dynamoDbService;
    this.oAuthClientService = oAuthClientService;
  }

  /**
   * send a get id request. This only needs to be executed the first time
   * and the result should be cached.
   *
   * @param username
   * @param logins
   * @return
   */
  private String getIdentityId(String username, Map<String, String> logins) throws DataAccessException {
    GetIdRequest idRequest = new GetIdRequest()
        .withAccountId(awsProperties.getClientId())
        .withIdentityPoolId(awsProperties.getIdentityPoolId())
        .withLogins(logins);
    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
    GetIdResult idResult = amazonCognitoIdentity.getId(idRequest);
    String identityId = idResult.getIdentityId();
    dynamoDbService.storeIdentityId(username, identityId);
    return identityId;
  }

  public StringMap getId(String username, String facebookSessionKey) throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException {
    Map<String, String> providerTokens = new HashMap<>();
    providerTokens.put(awsProperties.getFacebookProviderName(), facebookSessionKey);

    StringMap userDetailsFromFacebook = oAuthClientService.getUserDetailsFromFacebook(facebookSessionKey);

    adminCreateUser(username, userDetailsFromFacebook);

    String email = (String) userDetailsFromFacebook.get(awsProperties.getAttributeEmail());
    String identityId = getIdentityId(username, providerTokens);

    StringMap resp = new StringMap();
    resp.put(awsProperties.getAttributeIdentityId(), identityId);
    return resp;
  }

  private void adminCreateUser(String username, StringMap userDetailsFromFacebook) {
    List<AttributeType> userAttributes = new ArrayList<>();
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeEmail())
        .withValue(userDetailsFromFacebook.getString("email")));
    userAttributes.add(new AttributeType()
        .withName("email_verified")
        .withValue("True"));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeGivenName())
        .withValue(userDetailsFromFacebook.getString("first_name")));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeFamilyName())
        .withValue(userDetailsFromFacebook.getString("last_name")));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeFamilyName())
        .withValue(userDetailsFromFacebook.getString("gender")));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeLocale())
        .withValue(userDetailsFromFacebook.getString("locale")));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributePicture())
        .withValue(userDetailsFromFacebook.getString("picture")));
    userAttributes.add(new AttributeType()
        .withName(awsProperties.getAttributeBirthday())
        .withValue(userDetailsFromFacebook.getString("birthday")));
    AdminCreateUserRequest adminCreateUserRequest = new AdminCreateUserRequest()
        .withUsername(username)
        .withUserPoolId(awsProperties.getUserPoolId())
        .withDesiredDeliveryMediums("EMAIL")
        .withUserAttributes(userAttributes);
    awsCognitoIdentityProvider.adminCreateUser(adminCreateUserRequest);
  }

  private StringMap getCredentialsForIdentity(String identityId, Map<String, String> logins) throws DataAccessException {
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
    resp.put(awsProperties.getSessionToken(), credentials.getSessionToken());
    return resp;
  }

  public StringMap getCredentialsForIdentityFacebook(String facebookSessionKey, String identityId) throws DataAccessException {
    Map<String, String> logins = new HashMap<>();
    logins.put(awsProperties.getFacebookProviderName(), facebookSessionKey);
    return getCredentialsForIdentity(identityId, logins);
  }

  public String getOpenIdToken(String username) throws DataAccessException {
    String identityId = dynamoDbService.retrieveIdentityId(username);
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
// for example the Amazon Cognito Sync client
//    AmazonCognitoSync syncClient = new AmazonCognitoSyncClient(sessionCredentials);
//    ListDatasetsRequest syncRequest = new ListDatasetsRequest();
//    syncRequest.setIdentityId(idResp.getIdentityId());
//    syncRequest.setIdentityPoolId("YOUR_COGNITO_IDENTITY_POOL_ID");
//    ListDatasetsResult syncResp = syncClient.listDatasets(syncRequest);
  }

  public StringMap register(String username, String email, String password) throws Exception {
    List<AttributeType> userAttributes = new ArrayList<>();
    userAttributes.add(new AttributeType().withName(awsProperties.getAttributeEmail()).withValue(email));
    SignUpRequest signUpRequest = new SignUpRequest()
        .withClientId(awsProperties.getAppClientId())
        .withSecretHash(getSecretHash(username))
        .withUsername(username)
        .withPassword(password)
        .withUserAttributes(userAttributes);
    awsCognitoIdentityProvider.signUp(signUpRequest);
    return createStatusOk("register.SIGN_UP");
  }

  public StringMap confirmRegistration(String username, String confirmationCode) throws Exception {
    ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest()
        .withClientId(awsProperties.getAppClientId())
        .withUsername(username)
        .withSecretHash(getSecretHash(username))
        .withConfirmationCode(confirmationCode);
    awsCognitoIdentityProvider.confirmSignUp(confirmSignUpRequest);
    return createStatusOk("confirmRegistration.CONFIRMED");
  }

  public StringMap resendCode(String username) throws Exception {
    ResendConfirmationCodeRequest resendConfirmationCodeRequest = new ResendConfirmationCodeRequest()
        .withClientId(awsProperties.getAppClientId())
        .withSecretHash(getSecretHash(username))
        .withUsername(username);
    awsCognitoIdentityProvider.resendConfirmationCode(resendConfirmationCodeRequest);
    return createStatusOk("resendCode.SENT");
  }

  public StringMap authenticateUser(String username, String password) throws Exception {
    if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
      return createStatusFail("authenticateUser.INVALID_PARAMS");
    }
    Map<String, String> authParameters = new HashMap<>();
    authParameters.put("USERNAME", username);
    authParameters.put("PASSWORD", password);
    authParameters.put("SECRET_HASH", getSecretHash(username));
    AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        .withClientId(awsProperties.getAppClientId())
        .withUserPoolId(awsProperties.getUserPoolId())
        .withAuthParameters(authParameters);

    AdminInitiateAuthResult adminInitiateAuthResult = awsCognitoIdentityProvider.adminInitiateAuth(adminInitiateAuthRequest);
    if (Objects.equals(adminInitiateAuthResult.getChallengeName(), ChallengeNameType.NEW_PASSWORD_REQUIRED.name())) {
      return createStatusFail("authenticateUser.NEW_PASSWORD_REQUIRED");
    }
    AuthenticationResultType authenticationResult = adminInitiateAuthResult.getAuthenticationResult();
    return getAuthenticationResult(authenticationResult);

    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
//    Map<String, String> logins = new HashMap<>();
//    logins.put(awsProperties.getCognitoUserPoolName(), authenticationResult.getIdToken());
//    return getCredentialsForIdentity(getIdentityId(email, logins), logins);
  }

  public StringMap authenticateNewUser(String username, String password, String tempPassword) throws Exception {
    if (!StringUtils.hasText(username) || !StringUtils.hasText(tempPassword) || !StringUtils.hasText(password)) {
      return createStatusFail("authenticateNewUser.INVALID_PARAMS");
    }

    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", username);
    authParams.put("PASSWORD", tempPassword);
    authParams.put("SECRET_HASH", getSecretHash(username));

    AdminInitiateAuthRequest initialRequest = new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        .withAuthParameters(authParams)
        .withClientId(awsProperties.getAppClientId())
        .withUserPoolId(awsProperties.getUserPoolId());

    AdminInitiateAuthResult initialResponse = awsCognitoIdentityProvider.adminInitiateAuth(initialRequest);
    if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(initialResponse.getChallengeName())) {
      return createStatusFail("authenticateNewUser.MISMATCH_CHALLENGE", initialResponse.getChallengeName());
    }

    Map<String, String> challengeResponses = new HashMap<>();
    challengeResponses.put("USERNAME", username);
    challengeResponses.put("PASSWORD", tempPassword);
    challengeResponses.put("NEW_PASSWORD", password);
    challengeResponses.put("SECRET_HASH", getSecretHash(username));

    AdminRespondToAuthChallengeRequest finalRequest = new AdminRespondToAuthChallengeRequest()
        .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
        .withChallengeResponses(challengeResponses)
        .withClientId(awsProperties.getAppClientId())
        .withUserPoolId(awsProperties.getUserPoolId())
        .withSession(initialResponse.getSession());

    AdminRespondToAuthChallengeResult challengeResponse = awsCognitoIdentityProvider.adminRespondToAuthChallenge(finalRequest);
    if (!StringUtils.hasText(challengeResponse.getChallengeName())) {
      return createStatusOk("authenticateNewUser.LOGGED_IN");
    } else {
      throw new RuntimeException("unexpected challenge: " + challengeResponse.getChallengeName());
    }
  }

  public StringMap refreshToken(String refreshToken) {
    Map<String, String> authParameters = new HashMap<>();
    authParameters.put("REFRESH_TOKEN", refreshToken);
    AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
        .withClientId(awsProperties.getClientId())
        .withUserPoolId(awsProperties.getUserPoolId())
        .withAuthParameters(authParameters);
    AdminInitiateAuthResult adminInitiateAuthResult = awsCognitoIdentityProvider.adminInitiateAuth(adminInitiateAuthRequest);
    return getAuthenticationResult(adminInitiateAuthResult.getAuthenticationResult());
  }

  public StringMap forgotPassword(String username) throws Exception {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest()
        .withClientId(awsProperties.getAppClientId())
        .withUsername(username)
        .withSecretHash(getSecretHash(username));
    awsCognitoIdentityProvider.forgotPassword(forgotPasswordRequest);
    return createStatusOk("forgotPassword.SENT");
  }

  public void confirmNewPassword(String username, String confirmation, String password) {
    ConfirmForgotPasswordRequest confirmForgotPasswordRequest = new ConfirmForgotPasswordRequest()
        .withClientId(awsProperties.getClientId())
        .withUsername(username)
        .withConfirmationCode(confirmation)
        .withPassword(password);
    awsCognitoIdentityProvider.confirmForgotPassword(confirmForgotPasswordRequest);
  }

  public StringMap adminResetUserPassword(String username) {
    AdminResetUserPasswordRequest adminResetUserPasswordRequest = new AdminResetUserPasswordRequest()
        .withUsername(username)
        .withUserPoolId(awsProperties.getUserPoolId());
    awsCognitoIdentityProvider.adminResetUserPassword(adminResetUserPasswordRequest);
    return StringMap.emptyMap();
  }

  public void logout(String username) {
    AdminUserGlobalSignOutRequest adminUserGlobalSignOutRequest = new AdminUserGlobalSignOutRequest()
        .withUserPoolId(awsProperties.getUserPoolId())
        .withUsername(username);
    awsCognitoIdentityProvider.adminUserGlobalSignOut(adminUserGlobalSignOutRequest);
  }

  private String getSecretHash(String username) throws Exception {
    String appClientId = awsProperties.getAppClientId(),
        appSecretKey = awsProperties.getAppClientSecret();
    byte[] data = (username + appClientId).getBytes("UTF-8");
    byte[] key = appSecretKey.getBytes("UTF-8");

    return encodeAsString(hmacSHA256(data, key));
  }

  private byte[] hmacSHA256(byte[] data, byte[] key) throws Exception {
    String algorithm = "HmacSHA256";
    Mac mac = Mac.getInstance(algorithm);
    mac.init(new SecretKeySpec(key, algorithm));
    return mac.doFinal(data);
  }

  private StringMap createStatusOk(String message) {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getStatus(), message);
    return resp;
  }

  private StringMap createStatusFail(String message) {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getStatus(), awsProperties.getStatusFail());
    resp.put(awsProperties.getStatusMessage(), message);
    return resp;
  }

  private StringMap createStatusFail(String code, String message) {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getStatus(), awsProperties.getStatusFail());
    resp.put(awsProperties.getStatusCode(), code);
    resp.put(awsProperties.getStatusMessage(), message);
    return resp;
  }

  private StringMap getAuthenticationResult(AuthenticationResultType authenticationResult) {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getAccessToken(), authenticationResult.getAccessToken());
    resp.put(awsProperties.getRefreshToken(), authenticationResult.getRefreshToken());
    resp.put(awsProperties.getIdToken(), authenticationResult.getIdToken());
    return resp;
  }
}
