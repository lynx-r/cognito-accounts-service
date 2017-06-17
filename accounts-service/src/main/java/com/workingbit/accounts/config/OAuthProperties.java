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

  @Value("${TEMP_PASSWORD_SECRET}")
  private @Getter
  String tempPasswordSecret;

  private @Getter
  char[] asd = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'G', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '{', '}', '[', ']',':', ':', '\\', '/', '|', '.', ',', '?', '"', '\'', '`'};//Alphabet&Digit

  @Autowired
  public OAuthProperties(Environment env) {
    this.env = env;
  }

}
