package com.workingbit.accounts.controller

import com.workingbit.accounts.common.BundleOAuth
import org.apache.oltu.oauth2.client.OAuthClient
import org.apache.oltu.oauth2.client.URLConnectionClient
import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse
import org.apache.oltu.oauth2.common.OAuthProviderType
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.common.exception.OAuthSystemException
import org.apache.oltu.oauth2.common.message.types.GrantType
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.websocket.server.PathParam
/**
 * Created by Aleksey Popryaduhin on 13:27 16/06/2017.
 */
@Controller
class HomeController {

    static final String USERS = '/users'
    static final String REGISTER = '/register'
    static final String AUTHENTICATE = '/authenticate'
    static final String CONFIRM_REGISTRATION = '/confirmRegistration'
    static final String RESEND_CODE = '/resendCode'

    @GetMapping('/')
    def index(Model model) {
        model.addAttribute('registerRedirect', loginRedirect('/reg'))
        model.addAttribute('loginRedirect', loginRedirect('/login'))
        return 'index'
    }

    @PostMapping(value = HomeController.REGISTER, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def register(@RequestBody MultiValueMap credentials, RedirectAttributes redirectAttributes) {
        final String uri = "${BundleOAuth.API_HOST}${USERS}${REGISTER}"

        Map<String, Object> params = new HashMap<>()
        params.put('username', credentials.username[0])
        params.put('email', credentials.email[0])
        params.put('password', credentials.password[0])

        RestTemplate restTemplate = new RestTemplate()
        Map result = restTemplate.postForObject(uri, params, Map.class)
        prepareResp(result, redirectAttributes, 'reg')
        return 'redirect:/'
    }

    @PostMapping(value = HomeController.CONFIRM_REGISTRATION, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def confirmRegistration(@RequestBody MultiValueMap credentials, RedirectAttributes redirectAttributes) {
        final String uri = "${BundleOAuth.API_HOST}${USERS}${HomeController.CONFIRM_REGISTRATION}"

        Map<String, Object> params = new HashMap<>()
        params.put('username', credentials.username[0])
        params.put('confirmation_code', credentials.confirmation_code[0])

        RestTemplate restTemplate = new RestTemplate()
        Map result = restTemplate.postForObject(uri, params, Map.class)
        prepareResp(result, redirectAttributes, 'reg')
        return 'redirect:/'
    }

    @PostMapping(value = HomeController.RESEND_CODE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def resendConfirmationCode(@RequestBody MultiValueMap credentials, RedirectAttributes redirectAttributes) {
        final String uri = "${BundleOAuth.API_HOST}${USERS}${RESEND_CODE}"

        Map<String, Object> params = new HashMap<>()
        params.put('username', credentials.username[0])

        RestTemplate restTemplate = new RestTemplate()
        Map result = restTemplate.postForObject(uri, params, Map.class)
        prepareResp(result, redirectAttributes, 'reg')
        return 'redirect:/'
    }

    @PostMapping(value = HomeController.AUTHENTICATE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def authenticate(@RequestBody MultiValueMap credentials, RedirectAttributes redirectAttributes) {
        final String uri = "${BundleOAuth.API_HOST}${USERS}${AUTHENTICATE}"

        Map<String, Object> params = new HashMap<>()
        params.put('username', credentials.username[0])
        params.put('password', credentials.password[0])

        RestTemplate restTemplate = new RestTemplate()
        Map result = restTemplate.postForObject(uri, params, Map.class)
        prepareResp(result, redirectAttributes, 'auth')
        return 'redirect:/'
    }

    @GetMapping('/loginCallback/reg')
    String registerCallback(@PathParam('code') String code, @PathParam('state') String state,
                            RedirectAttributes redirectAttributes) {
        try {
            // CSRF Protection
            if (!'123'.equals(state)) {
                return 'index'
            } else if (null == code) {
                return 'index'
            }

            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(BundleOAuth.FB_API_GRAPH + "/" + BundleOAuth.FB_API_VERSION
                    + "/" + BundleOAuth.FB_API_OAUTH_PATH)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(BundleOAuth.FB_CLIENT_ID)
                    .setClientSecret(BundleOAuth.FB_CLIENT_SECRET)
                    .setScope(BundleOAuth.FB_SCOPE)
                    .setParameter("redirect_uri", HOST + BundleOAuth.FB_REDIRECT_URL + '/reg')
                    .setCode(code)
                    .buildQueryMessage()

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient())
            OAuthAccessTokenResponse response = oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class)

            String accessToken = response.getAccessToken()

            final String uri = "${BundleOAuth.API_HOST}${USERS}/registerFacebookUser"

            Map<String, Object> params = new HashMap<>()
            params.put('facebook_access_token', accessToken)

            RestTemplate restTemplate = new RestTemplate()
            Map result = restTemplate.postForObject(uri, params, Map.class)
            prepareResp(result, redirectAttributes, 'fb_reg')
            return 'redirect:/'
        } catch (OAuthProblemException | OAuthSystemException | IOException e) {
            e.printStackTrace()
        }
        return 'index'
    }

    @GetMapping('/loginCallback/login')
    String loginCallback(@PathParam('code') String code, @PathParam('state') String state,
                         RedirectAttributes redirectAttributes) {
        try {
            // CSRF Protection
            if (null == code) {
                return 'index'
            }

            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(BundleOAuth.FB_API_GRAPH + "/" + BundleOAuth.FB_API_VERSION
                    + "/" + BundleOAuth.FB_API_OAUTH_PATH)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(BundleOAuth.FB_CLIENT_ID)
                    .setClientSecret(BundleOAuth.FB_CLIENT_SECRET)
                    .setScope(BundleOAuth.FB_SCOPE)
                    .setParameter("redirect_uri", HOST + BundleOAuth.FB_REDIRECT_URL + '/login')
                    .setCode(code)
                    .buildQueryMessage()

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient())
            OAuthAccessTokenResponse response = oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class)

            String accessToken = response.getAccessToken()

            final String uri = "${BundleOAuth.API_HOST}${USERS}/authenticateFacebookUser"

            Map<String, Object> params = new HashMap<>()
            params.put('facebook_access_token', accessToken)

            RestTemplate restTemplate = new RestTemplate()
            Map result = restTemplate.postForObject(uri, params, Map.class)
            prepareResp(result, redirectAttributes, 'fb_reg')
            return 'redirect:/'
        } catch (OAuthProblemException | OAuthSystemException | IOException e) {
            e.printStackTrace()
        }
        return 'index'
    }

    @PostMapping(value = '/echo', consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def echo(@RequestBody MultiValueMap data, RedirectAttributes redirectAttributes) {
        final String uri = "${BundleOAuth.TEST_HOST}/test/echo?echo={echo}"

        Map<String, Object> params = new HashMap<>()
        params.put('echo', data.echo[0])

        RestTemplate restTemplate = new RestTemplate()
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.GET, null, String.class, params)

        redirectAttributes.addFlashAttribute('type', 'echo')
        redirectAttributes.addFlashAttribute('register_STATE_STATUS', true)
        redirectAttributes.addFlashAttribute('register_STATE_MESSAGE', "SUCCESS ${result.body}")

        return 'redirect:/'
    }

    private static void prepareResp(Map result, RedirectAttributes redirectAttributes, String type) {
        redirectAttributes.addFlashAttribute('type', type)
        if (result.status == 'fail') {
            redirectAttributes.addFlashAttribute('register_STATE_STATUS', false)
            redirectAttributes.addFlashAttribute('register_STATE_MESSAGE', "FAIL ${result.message}")
        } else {
            redirectAttributes.addFlashAttribute('register_STATE_STATUS', true)
            redirectAttributes.addFlashAttribute('register_STATE_MESSAGE', "SUCCESS ${result.message}")
        }
    }

    private static String loginRedirect(String typeAction) {
        try {
            OAuthClientRequest oAuthRequest = OAuthClientRequest
                    .authorizationProvider(OAuthProviderType.FACEBOOK)
                    .setClientId(BundleOAuth.FB_CLIENT_ID)
                    .setScope(BundleOAuth.FB_SCOPE)
                    .setRedirectURI(BundleOAuth.HOST + BundleOAuth.FB_REDIRECT_URL + typeAction)
                    .setResponseType("code")
                    .setParameter("state", "123")
                    .buildQueryMessage()
            return oAuthRequest.getLocationUri()
        } catch (OAuthSystemException e) {
            e.printStackTrace()
        }
        return "index"
    }
}
