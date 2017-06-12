package online.shashki.accounts.controller;

import online.shashki.accounts.common.EnumRole;
import online.shashki.accounts.common.StringMap;
import online.shashki.accounts.config.AwsProperties;
import online.shashki.accounts.exception.DataAccessException;
import online.shashki.accounts.service.AWSCognitoService;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

@RestController
@RequestMapping(value = "/users", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class AuthUserController {

  private final AWSCognitoService amazonCognitoService;
  private final AwsProperties awsProperties;

  @Autowired
  public AuthUserController(AWSCognitoService amazonCognitoService,
                            AwsProperties awsProperties) {
    this.amazonCognitoService = amazonCognitoService;
    this.awsProperties = awsProperties;
  }

  @PostMapping("/getId")
  public StringMap getId(@RequestBody StringMap usernameWrapper) throws DataAccessException, OAuthSystemException, OAuthProblemException, IOException {
    return amazonCognitoService.getId(
        usernameWrapper.getString(awsProperties.getFacebookAccessTokenName()));
  }

  @PostMapping("/getCredentialsForIdentityFacebook")
  public StringMap getCredentialsForIdentityFacebook(@RequestBody StringMap identityId) throws DataAccessException {
    return amazonCognitoService.getCredentialsForIdentityFacebook(
        identityId.getString(awsProperties.getFacebookAccessTokenName()),
        identityId.getString(awsProperties.getAttributeIdentityId()));
  }

  @PostMapping("/register")
  public StringMap register(@RequestBody StringMap credentials) throws Exception {
    return amazonCognitoService.register(
        credentials.getString(awsProperties.getAttributeEmail()),
        credentials.getString(awsProperties.getAttributePassword()),
        credentials.getString(awsProperties.getAttributeGivenName()));
  }

  @PostMapping("/confirmRegistration")
  public StringMap confirmRegistration(@RequestBody StringMap confirmationCode) throws Exception {
    return amazonCognitoService.confirmRegistration(
        confirmationCode.getString(awsProperties.getAttributeEmail()),
        confirmationCode.getString(awsProperties.getAttributeConfirmationCode())
    );
  }

  @PostMapping("/resendCode")
  public StringMap resendCode(@RequestBody StringMap email) throws Exception {
    return amazonCognitoService.resendCode(
        email.getString(awsProperties.getAttributeEmail())
    );
  }

  @PostMapping("/authenticate")
  public StringMap authenticate(@RequestBody StringMap credentials) throws Exception {
    return amazonCognitoService.authenticate(
        credentials.getString(awsProperties.getAttributeEmail()),
        credentials.getString(awsProperties.getAttributePassword()));
  }

  @PostMapping("/assumeRoleWithWebIdentity")
  public String assumeRoleWithWebIdentity(@RequestParam("openIdToken") String openIdToken,
                                          @RequestParam("role") String role) {
    return amazonCognitoService.assumeRoleWithWebIdentity(openIdToken, EnumRole.valueOf(role));
  }
}