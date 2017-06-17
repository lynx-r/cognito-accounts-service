package com.workingbit.accounts.common;

import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 23.02.16
 * Time: 22:19
 */
public class BundleOAuth {
  private static final ResourceBundle OA_Bundle	= ResourceBundle.getBundle("oauth");

  public static final String FB_CLIENT_ID = OA_Bundle.getString("FB_CLIENT_ID");
  public static final String FB_CLIENT_SECRET = OA_Bundle.getString("FB_CLIENT_SECRET");
  public static final String FB_FIELDS = OA_Bundle.getString("FB_FIELDS");
  public static final String FB_SCOPE = OA_Bundle.getString("FB_SCOPE");
  public static final String FB_REDIRECT_URL = OA_Bundle.getString("FB_REDIRECT_URL");
  public static final String FB_API_GRAPH = OA_Bundle.getString("FB_API_GRAPH");
  public static final String FB_API_VERSION = OA_Bundle.getString("FB_API_VERSION");
  public static final String FB_API_OAUTH_PATH = OA_Bundle.getString("FB_API_OAUTH_PATH");

  public static final String ERROR_URL = OA_Bundle.getString("ERROR_URL");
}
