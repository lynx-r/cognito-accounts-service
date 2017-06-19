package com.workingbit.accounts.controller;

import com.workingbit.accounts.common.StringMap;
import com.workingbit.accounts.config.AwsProperties;
import com.workingbit.accounts.service.AWSCognitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(value = "/users", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class AuthUserController {

  private final AWSCognitoService awsCognitoService;
  private final AwsProperties awsProperties;

  @Autowired
  public AuthUserController(AWSCognitoService awsCognitoService,
                            AwsProperties awsProperties) {
    this.awsCognitoService = awsCognitoService;
    this.awsProperties = awsProperties;
  }

  @PostMapping("/register")
  public StringMap register(@RequestBody StringMap credentials) throws Exception {
    return awsCognitoService.register(
        credentials.getString(awsProperties.getAttributeUsername()),
        credentials.getString(awsProperties.getAttributeEmail()),
        credentials.getString(awsProperties.getAttributePassword())
    );
  }

  /**
   * Register via Facebook
   * @param credentials Facebook access token
   * @return AccessToke, IdToken, RefreshToken
   * @throws Exception
   */
  @PostMapping("/registerFacebookUser")
  public StringMap registerFacebookUser(@RequestBody StringMap credentials) throws Exception {
    return StringMap.emptyMap();
//    return awsCognitoService.registerFacebookUser(
//        credentials.getString(awsProperties.getFacebookAccessTokenName())
//    );
  }

  @PostMapping("/confirmRegistration")
  public StringMap confirmRegistration(@RequestBody StringMap confirmationCode) throws Exception {
    return awsCognitoService.confirmRegistration(
        confirmationCode.getString(awsProperties.getAttributeUsername()),
        confirmationCode.getString(awsProperties.getConfirmationCode())
    );
  }

  @PostMapping("/resendCode")
  public StringMap resendCode(@RequestBody StringMap credentials) throws Exception {
    return awsCognitoService.resendCode(
        credentials.getString(awsProperties.getAttributeUsername())
    );
  }

  @PostMapping("/forgotPassword")
  public StringMap forgotPassword(@RequestBody StringMap credentials) throws Exception {
    return awsCognitoService.forgotPassword(
        credentials.getString(awsProperties.getAttributeUsername())
    );
  }

  @PostMapping("/confirmNewPassword")
  public StringMap confirmNewPassword(@RequestBody StringMap credentials) throws Exception {
    return awsCognitoService.confirmNewPassword(
        credentials.getString(awsProperties.getAttributeUsername()),
        credentials.getString(awsProperties.getConfirmationCode()),
        credentials.getString(awsProperties.getAttributePassword())
    );
  }

  @PostMapping("/authenticate")
  public StringMap authenticate(@RequestBody StringMap credentials) throws Exception {
    return awsCognitoService.authenticateUser(
        credentials.getString(awsProperties.getAttributeUsername()),
        credentials.getString(awsProperties.getAttributePassword()));
  }

  /**
   * Register via Facebook
   * @param credentials Facebook access token
   * @return AccessToke, IdToken, RefreshToken
   * @throws Exception
   */
  @PostMapping("/authenticateFacebookUser")
  public StringMap authenticateFacebookUser(@RequestBody StringMap credentials) throws Exception {
    return StringMap.emptyMap();
//    return awsCognitoService.authenticateFacebookUser(
//        credentials.getString(awsProperties.getFacebookAccessTokenName())
//    );
  }
}