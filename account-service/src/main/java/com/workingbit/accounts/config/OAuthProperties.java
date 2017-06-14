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
@PropertySource("classpath:oauth.properties")
public class OAuthProperties {

  private final Environment env;

  @Value("${FB_FIELDS}")
  private @Getter
  String fbFields;

  @Value("${FB_API_GRAPH}")
  private @Getter
  String fbApiGraph;

  @Autowired
  public OAuthProperties(Environment env) {
    this.env = env;
  }

}
