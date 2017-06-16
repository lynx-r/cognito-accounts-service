package com.workingbit.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Aleksey Popryaduhin on 21:28 15/06/2017.
 */
@Controller
@RequestMapping("/")
public class HomeController {

  @GetMapping("/")
  public String index() {
    return "index";
  }
}
