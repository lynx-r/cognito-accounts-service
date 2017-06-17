package com.workingbit.accounts.controller;

import com.workingbit.accounts.common.StringMap;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Aleksey Popryaduhin on 16:03 17/06/2017.
 */
@RestController
@RequestMapping("/test")
public class EchoController {

  @PostMapping("/echo")
  public StringMap echo(@RequestBody StringMap echo) {
    return echo;
  }

  @GetMapping(value = "/echo")
  public String echo(@RequestParam("echo") String echo) {
    return echo;
  }
}
