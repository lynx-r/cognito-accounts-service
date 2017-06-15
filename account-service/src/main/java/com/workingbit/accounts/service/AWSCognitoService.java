package com.workingbit.accounts.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.workingbit.accounts.common.StringMap;
import com.workingbit.accounts.config.AwsProperties;
import com.workingbit.accounts.config.OAuthProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

import static com.amazonaws.util.Base64.encodeAsString;

/**
 * Created by Aleksey Popryaduhin on 13:33 11/06/2017.
 */
@Service
public class AWSCognitoService {

  private final AwsProperties awsProperties;
  private final OAuthProperties oAuthProperties;
  private final OAuthClientService oAuthClientService;
  private final DynamoDbService dynamoDbService;

  private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;

  @Autowired
  public AWSCognitoService(AwsProperties awsProperties,
                           OAuthProperties oAuthProperties,
                           OAuthClientService oAuthClientService,
                           DynamoDbService dynamoDbService) {
    this.awsProperties = awsProperties;
    this.awsCognitoIdentityProvider = AWSCognitoIdentityProviderClient.builder()
        .withRegion(awsProperties.getRegion())
        .build();
    this.oAuthProperties = oAuthProperties;
    this.oAuthClientService = oAuthClientService;
    this.dynamoDbService = dynamoDbService;
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

  public StringMap registerFacebookUser(String accessToken)
      throws Exception {
    StringMap userDetailsFromFacebook = oAuthClientService.getUserDetailsFromFacebook(accessToken);
    return adminCreateUser(userDetailsFromFacebook);
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
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
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
    return getAuthenticationResult(username, authenticationResult);

    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
//    Map<String, String> logins = new HashMap<>();
//    logins.put(awsProperties.getCognitoUserPoolName(), authenticationResult.getUserIdToken());
//    return getCredentialsForIdentity(getIdentityId(email, logins), logins);
  }

  /**
   * Set permanent password for a user and sign he up
   * @param username
   * @param password
   * @param tempPassword
   * @return AccessToke, IdToken, RefreshToken
   * @throws Exception
   */
  public StringMap authenticateNewUser(String username, String password, String tempPassword) throws Exception {
    if (StringUtils.isBlank(username) || StringUtils.isBlank(tempPassword) || StringUtils.isBlank(password)) {
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
    if (StringUtils.isBlank(challengeResponse.getChallengeName())) {
      return getAuthenticationResult(username, challengeResponse.getAuthenticationResult());
    } else {
      throw new RuntimeException("unexpected challenge: " + challengeResponse.getChallengeName());
    }
  }

  public StringMap authenticateFacebookUser(String accessToken) throws Exception {
    GetUserRequest getUserRequest = new GetUserRequest()
        .withAccessToken(accessToken);
    GetUserResult user = awsCognitoIdentityProvider.getUser(getUserRequest);

    Map<String, AttributeValue> userFromDb = dynamoDbService.retrieveByUsername(user.getUsername());
    String password = userFromDb.get(awsProperties.getAttributePassword()).getS() + oAuthProperties.getTempPasswordSecret();
    return authenticateUser(user.getUsername(), password);
  }

  public StringMap refreshToken(String username, String refreshToken) throws Exception {
    Map<String, String> authParameters = new HashMap<>();
    authParameters.put("REFRESH_TOKEN", refreshToken);
    authParameters.put("SECRET_HASH", getSecretHash(username));
    AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
        .withClientId(awsProperties.getAppClientId())
        .withUserPoolId(awsProperties.getUserPoolId())
        .withAuthParameters(authParameters);
    AdminInitiateAuthResult adminInitiateAuthResult = awsCognitoIdentityProvider.adminInitiateAuth(adminInitiateAuthRequest);
    return getAuthenticationResult(username, adminInitiateAuthResult.getAuthenticationResult());
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

  private StringMap getAuthenticationResult(String username, AuthenticationResultType authenticationResult) {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getUserAccessToken(), authenticationResult.getAccessToken());
    resp.put(awsProperties.getRefreshToken(), authenticationResult.getRefreshToken());
    resp.put(awsProperties.getUserIdToken(), authenticationResult.getIdToken());
    resp.put(awsProperties.getAttributeUsername(), username);
    return resp;
  }

  private StringMap adminCreateUser(StringMap userDetailsFromFacebook) throws Exception {
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
    String email = userDetailsFromFacebook.getString("email");
    String tempPassword = RandomStringUtils.random(90, oAuthProperties.getAsd());
    AdminCreateUserRequest adminCreateUserRequest = new AdminCreateUserRequest()
        .withUsername(email)
        .withTemporaryPassword(tempPassword)
        .withMessageAction(MessageActionType.SUPPRESS)
        .withUserPoolId(awsProperties.getUserPoolId())
        .withUserAttributes(userAttributes);
    awsCognitoIdentityProvider.adminCreateUser(adminCreateUserRequest);
    dynamoDbService.storeUser(email, tempPassword);

    String permPassword = tempPassword + oAuthProperties.getTempPasswordSecret();
    return authenticateNewUser(email, permPassword, tempPassword);
  }
}