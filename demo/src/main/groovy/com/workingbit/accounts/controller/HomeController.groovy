package com.workingbit.accounts.controller

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.client.RestTemplate
/**
 * Created by Aleksey Popryaduhin on 13:27 16/06/2017.
 */
@Controller
class HomeController {

    String HOST = 'http://localhost:8888'
    String USERS = 'users'

    @GetMapping('/')
    def index() {
        return 'index'
    }

    @PostMapping(value = '/register', consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def register(@RequestBody MultiValueMap credentials, Model model) {
        final String uri = "${HOST}/${USERS}/register"

        Map<String, Object> params = new HashMap<>()
        params.put('username', credentials.username[0])
        params.put('email', credentials.email[0])
        params.put('password', credentials.password[0])

        RestTemplate restTemplate = new RestTemplate()
        Map result = restTemplate.postForObject(uri, params, Map.class)
        println(result)
        if (result.status == 'fail') {
            model.addAttribute('register_STATE_STATUS', false)
            model.addAttribute('register_STATE_MESSAGE', "FAIL ${result.message}")
        } else {
            model.addAttribute('register_STATE_STATUS', true)
            model.addAttribute('register_STATE_MESSAGE', "SUCCESS ${result.message}")
        }
        return 'index'
    }

    @PostMapping(value = '/confirmRegistration', consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    def confirmRegistration(@RequestBody MultiValueMap credentials, Model model) {
        model.addAttribute('register.STATE', '')
        return 'index'
    }
}
