package com.workingbit.accounts.common;

/**
 * Created by Aleksey Popryaduhin on 07:42 11/06/2017.
 */
public enum EnumRole {
  ANONYMOUS("arn:aws:iam::6157xxxxxxxx:role/a_valid_aws_role_arn"),
  AUTHOR("arn:aws:iam::6157xxxxxxxx:role/a_valid_aws_role_arn"), // writes an articles
  VISITOR("arn:aws:iam::6157xxxxxxxx:role/a_valid_aws_role_arn"), // has advertisement
  READER("arn:aws:iam::6157xxxxxxxx:role/a_valid_aws_role_arn"); // doesn't have advertisement

  private final String roleArn;

  EnumRole(String roleArn) {
    this.roleArn = roleArn;
  }

  public String getRoleArn() {
    return roleArn;
  }
}
