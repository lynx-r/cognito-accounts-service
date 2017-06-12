package online.shashki.accounts.service;

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
import online.shashki.accounts.common.EnumRole;
import online.shashki.accounts.common.StringMap;
import online.shashki.accounts.config.AwsProperties;
import online.shashki.accounts.exception.DataAccessException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
   * @param email
   * @param logins
   * @return
   */
  private String getIdentityId(String email, Map<String, String> logins) throws DataAccessException {
    GetIdRequest idRequest = new GetIdRequest()
        .withAccountId(awsProperties.getClientId())
        .withIdentityPoolId(awsProperties.getIdentityPoolId())
        .withLogins(logins);
    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
    GetIdResult idResult = amazonCognitoIdentity.getId(idRequest);
    String identityId = idResult.getIdentityId();
    dynamoDbService.storeIdentityId(email, identityId);
    return identityId;
  }

  public StringMap getId(String facebookSessionKey) throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException {
    Map<String, String> providerTokens = new HashMap<>();
    providerTokens.put(awsProperties.getFacebookProviderName(), facebookSessionKey);

    StringMap userDetailsFromFacebook = oAuthClientService.getUserDetailsFromFacebook(facebookSessionKey);

    String email = (String) userDetailsFromFacebook.get(awsProperties.getAttributeEmail());
    String identityId = getIdentityId(email, providerTokens);

    StringMap resp = new StringMap();
    resp.put(awsProperties.getAttributeIdentityId(), identityId);
    return resp;
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

  public String getOpenIdToken(String email) throws DataAccessException {
    String identityId = dynamoDbService.retrieveIdentityId(email);
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

  public StringMap register(String email, String password, String givenName) throws Exception {
    List<AttributeType> userAttributes = new ArrayList<>();
    userAttributes.add(new AttributeType().withName(awsProperties.getAttributeEmail()).withValue(email));
    userAttributes.add(new AttributeType().withName("custom:" + awsProperties.getAttributeGivenName()).withValue(givenName));
    SignUpRequest signUpRequest = new SignUpRequest()
        .withClientId(awsProperties.getAppClientId())
        .withSecretHash(getSecretHash(email))
        .withUsername(email)
        .withPassword(password)
        .withUserAttributes(userAttributes);
    awsCognitoIdentityProvider.signUp(signUpRequest);
    return createStatusOk();
  }

  public StringMap confirmRegistration(String email, String confirmationCode) throws Exception {
    ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest()
        .withClientId(awsProperties.getAppClientId())
        .withUsername(email)
        .withSecretHash(getSecretHash(email))
        .withConfirmationCode(confirmationCode);
    awsCognitoIdentityProvider.confirmSignUp(confirmSignUpRequest);
    return createStatusOk();
  }

  public StringMap resendCode(String email) throws Exception {
    ResendConfirmationCodeRequest resendConfirmationCodeRequest = new ResendConfirmationCodeRequest()
        .withClientId(awsProperties.getAppClientId())
        .withSecretHash(getSecretHash(email))
        .withUsername(email);
    awsCognitoIdentityProvider.resendConfirmationCode(resendConfirmationCodeRequest);
    return createStatusOk();
  }

  public StringMap authenticate(String email, String password) throws Exception {
    Map<String, String> authParameters = new HashMap<>();
    authParameters.put("USERNAME", email);
    authParameters.put("PASSWORD", password);
    authParameters.put("SECRET_HASH", getSecretHash(email));
    AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        .withClientId(awsProperties.getAppClientId())
        .withUserPoolId(awsProperties.getUserPoolId())
        .withAuthParameters(authParameters);

    AdminInitiateAuthResult adminInitiateAuthResult = awsCognitoIdentityProvider.adminInitiateAuth(adminInitiateAuthRequest);
    AuthenticationResultType authenticationResult = adminInitiateAuthResult.getAuthenticationResult();

    return getAuthenticationResult(authenticationResult);

    // If you are authenticating your users through an identity provider
    // then you can set the Map of tokens in the request
//    Map<String, String> logins = new HashMap<>();
//    logins.put(awsProperties.getCognitoUserPoolName(), authenticationResult.getIdToken());
//    return getCredentialsForIdentity(getIdentityId(email, logins), logins);
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

  private StringMap getAuthenticationResult(AuthenticationResultType authenticationResult) {
    StringMap resp = new StringMap();
//    resp.put(awsProperties.getAccessToken(), authenticationResult.getAccessToken());
    resp.put(awsProperties.getRefreshToken(), authenticationResult.getRefreshToken());
    resp.put(awsProperties.getIdToken(), authenticationResult.getIdToken());
    return resp;
  }

  public void forgotPassword(String email) {
    ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest()
        .withClientId(awsProperties.getClientId())
        .withUsername(email);
    awsCognitoIdentityProvider.forgotPassword(forgotPasswordRequest);
  }

  public void confirmNewPassword(String email, String confirmation, String password) {
    ConfirmForgotPasswordRequest confirmForgotPasswordRequest = new ConfirmForgotPasswordRequest()
        .withClientId(awsProperties.getClientId())
        .withUsername(email)
        .withConfirmationCode(confirmation)
        .withPassword(password);
    awsCognitoIdentityProvider.confirmForgotPassword(confirmForgotPasswordRequest);
  }

  public void logout(String email) {
    AdminUserGlobalSignOutRequest adminUserGlobalSignOutRequest = new AdminUserGlobalSignOutRequest()
        .withUserPoolId(awsProperties.getUserPoolId())
        .withUsername(email);
    awsCognitoIdentityProvider.adminUserGlobalSignOut(adminUserGlobalSignOutRequest);
  }

  private String getSecretHash(String email) throws Exception {
    String appClientId = awsProperties.getAppClientId(),
        appSecretKey = awsProperties.getAppClientSecret();
    byte[] data = (email + appClientId).getBytes("UTF-8");
    byte[] key = appSecretKey.getBytes("UTF-8");

    return encodeAsString(hmacSHA256(data, key));
  }

  private byte[] hmacSHA256(byte[] data, byte[] key) throws Exception {
    String algorithm = "HmacSHA256";
    Mac mac = Mac.getInstance(algorithm);
    mac.init(new SecretKeySpec(key, algorithm));
    return mac.doFinal(data);
  }

  private StringMap createStatusOk() {
    StringMap resp = new StringMap();
    resp.put(awsProperties.getAttributeStatus(), awsProperties.getStatusOk());
    return resp;
  }
}
