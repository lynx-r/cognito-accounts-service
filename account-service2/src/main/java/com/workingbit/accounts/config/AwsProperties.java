package com.workingbit.accounts.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by Aleksey Popryaduhin on 08:57 11/06/2017.
 */
@Component
@PropertySource("classpath:aws.properties")
public class AwsProperties {

  private final Environment env;

  @Value("${CLIENT_ID}")
  private @Getter
  String clientId;

  @Value("${REGION}")
  private @Getter
  String region;

  @Value("${USER_POOL_ID}")
  private @Getter
  String userPoolId;

  @Value("${APP_CLIENT_ID}")
  private @Getter
  String appClientId;

  @Value("${APP_CLIENT_SECRET}")
  private @Getter
  String appClientSecret;

  @Value("${IDENTITY_POOL_ID}")
  private @Getter
  String identityPoolId;

  @Value("${COGNITO_USER_POOL_NAME}")
  private @Getter
  String cognitoUserPoolName;


  @Value("${USER_TABLE}")
  private @Getter
  String userTable;

  @Value("${READ_CAPACITY_UNITS}")
  private @Getter
  Long readCapacityUnits;

  @Value("${WRITE_CAPACITY_UNITS}")
  private @Getter
  Long writeCapacityUnits;


  @Value("${ATTRIBUTE_UID}")
  private @Getter
  String attributeUid;

  @Value("${ATTRIBUTE_EMAIL}")
  private @Getter
  String attributeEmail;

  @Value("${ATTRIBUTE_USERNAME}")
  private @Getter
  String attributeUsername;

  @Value("${ATTRIBUTE_PASSWORD}")
  private @Getter
  String attributePassword;

  @Value("${ATTRIBUTE_GIVEN_NAME}")
  private @Getter
  String attributeGivenName;

  @Value("${ATTRIBUTE_FAMILY_NAME}")
  private @Getter
  String attributeFamilyName;

  @Value("${ATTRIBUTE_IDENTITY_ID}")
  private @Getter
  String attributeIdentityId;

  @Value("${ATTRIBUTE_ENABLED}")
  private @Getter
  String attributeEnabled;

  @Value("${ATTRIBUTE_GENDER}")
  private @Getter
  String attributeGender;

  @Value("${ATTRIBUTE_LOCALE}")
  private @Getter
  String attributeLocale;

  @Value("${ATTRIBUTE_PICTURE}")
  private @Getter
  String attributePicture;

  @Value("${ATTRIBUTE_BIRTHDAY}")
  private @Getter
  String attributeBirthday;


  @Value("${STATUS}")
  private @Getter
  String status;

  private @Getter
  String statusOk = "ok";

  private @Getter
  String statusFail = "fail";

  private @Getter
  String statusMessage = "message";

  private @Getter
  String statusCode = "code";

  @Value("${CONFIRMATION_CODE}")
  private @Getter
  String confirmationCode;


  @Value("${AWS_SESSION_TOKEN}")
  private @Getter
  String awsSessionToken;

  @Value("${AWS_ACCESS_KEY_ID}")
  private @Getter
  String awsAccessKeyId;

  @Value("${AWS_SECRET_KEY}")
  private @Getter
  String awsSecretKey;


  @Value("${USER_ACCESS_TOKEN}")
  private @Getter
  String userAccessToken;

  @Value("${USER_REFRESH_TOKEN}")
  private @Getter
  String refreshToken;

  @Value("${USER_ID_TOKEN}")
  private @Getter
  String userIdToken;


  @Value("${FACEBOOK_PROVIDER_NAME}")
  private @Getter
  String facebookProviderName;

  @Value("${FACEBOOK_ACCESS_TOKEN_NAME}")
  private @Getter
  String facebookAccessTokenName;


  @Autowired
  public AwsProperties(Environment env) {
    this.env = env;
  }

}
