package com.workingbit.service;

import javax.enterprise.context.RequestScoped;

/**
 * Created by Aleksey Popryaduhin on 12:52 23/06/2017.
 */
@RequestScoped
public class EchoService {

  public String echo(String echo) {
    return echo;
  }
}
