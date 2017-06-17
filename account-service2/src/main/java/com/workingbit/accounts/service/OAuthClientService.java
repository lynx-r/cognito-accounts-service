package com.workingbit.accounts.service;

import com.workingbit.accounts.common.CommonUtils;
import com.workingbit.accounts.common.StringMap;
import com.workingbit.accounts.config.OAuthProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 21:33 12/06/2017.
 */
@Service
public class OAuthClientService {

  private final OAuthProperties oAuthProperties;

  @Autowired
  public OAuthClientService(OAuthProperties oAuthProperties) {
    this.oAuthProperties = oAuthProperties;
  }

  public StringMap getUserDetailsFromFacebook(String accessToken) throws OAuthSystemException, OAuthProblemException, IOException {
    if (StringUtils.isBlank(accessToken)) {
      throw new IllegalArgumentException("Invalid access token");
    }

    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    StringMap socialNetworkResponseMap = getGeneralUserInfo(oAuthClient, accessToken);
    if (CollectionUtils.isEmpty(socialNetworkResponseMap)) {
      throw new IllegalArgumentException("Invalid access token");
    }
    getProfilePicture(socialNetworkResponseMap, oAuthClient, accessToken);

    if (!CollectionUtils.isEmpty(socialNetworkResponseMap)) {
      StringMap pictureHashMap = (StringMap) socialNetworkResponseMap.get("picture");
      String picture = "";
      if (null != pictureHashMap && !pictureHashMap.isEmpty()) {
        picture = (String) ((HashMap) pictureHashMap.get("data")).get("url");
      }
      socialNetworkResponseMap.put("picture", picture);

      HashMap currencyMap = (HashMap) socialNetworkResponseMap.remove("currency");
      if (null != currencyMap) {
        String user_currency = (String) currencyMap.get("user_currency");
        socialNetworkResponseMap.put("userCurrency", user_currency);
      }

      HashMap hometownMap = (HashMap) socialNetworkResponseMap.remove("hometown");
      if (null != hometownMap) {
        String hometown = (String) hometownMap.get("name");
        socialNetworkResponseMap.put("hometown", hometown);
      }
      if (socialNetworkResponseMap.get("verified") != null) {
        final boolean verified = (Boolean) socialNetworkResponseMap.get("verified");
        socialNetworkResponseMap.put("verified", verified ? 1 : 0);
      }
    }

    socialNetworkResponseMap.put("accessToken", accessToken);
    socialNetworkResponseMap.put("tokenType", "Facebook");

    socialNetworkResponseMap.put("age", CommonUtils.getUserAge((String) socialNetworkResponseMap.get("birthday")));
    socialNetworkResponseMap.put("birthday", CommonUtils.formatDate(socialNetworkResponseMap.get("birthday")));

    // save user info in db
    return socialNetworkResponseMap;
  }

  private StringMap getGeneralUserInfo(OAuthClient oAuthClient, String accessToken)
      throws OAuthSystemException, OAuthProblemException, IOException {
    return requestToFacebook(oAuthClient, accessToken, "me", oAuthProperties.getFbFields(), null);
  }

  private void getProfilePicture(Map<String, Object> map, OAuthClient oAuthClient,
                                 String accessToken) throws OAuthSystemException, OAuthProblemException, IOException {
    if (!CollectionUtils.isEmpty(map)) {
      map.put("picture", requestToFacebook(oAuthClient, accessToken, map.get("id") + "/picture", null, "type=large&redirect=false&width=300&height=300"));
    }
  }

  private StringMap requestToFacebook(OAuthClient oAuthClient, String accessToken, String id, String fbFields, String queryParam) throws OAuthSystemException, OAuthProblemException, IOException {
    String params = (fbFields == null ? "" : ("fields=" + fbFields)) + (queryParam == null ? "" : queryParam);
    OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(oAuthProperties.getFbApiGraph() + "/" + id
        + "?" + params)
        .setAccessToken(accessToken)
        .buildQueryMessage();

    OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest,
        OAuth.HttpMethod.GET,
        OAuthResourceResponse.class);

    return processResponse(resourceResponse);
  }

  private StringMap processResponse(OAuthResourceResponse resourceResponse) throws IOException {
    if (resourceResponse.getResponseCode() != 200) {
      return StringMap.emptyMap();
    }

    InputStream inputStream = new ByteArrayInputStream(resourceResponse.getBody().getBytes(StandardCharsets.UTF_8));
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    return CommonUtils.convertJSONtoMap(reader.readLine(), false);
  }

}
