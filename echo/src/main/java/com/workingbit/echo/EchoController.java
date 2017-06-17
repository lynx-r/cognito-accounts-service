package com.workingbit.echo;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 16:03 17/06/2017.
 */
@RestController
@RequestMapping("/test")
public class EchoController {

  @PostMapping("/echo")
  public Map<String, String> echo(@RequestBody Map<String, String> echo) {
    return echo;
  }

  @GetMapping(value = "/echo")
  public String echo(@RequestParam("echo") String echo) {
    return echo;
  }
}
