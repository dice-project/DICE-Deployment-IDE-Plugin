package org.dice.deployments.client.model;

import com.google.gson.annotations.Expose;

public class Token extends ModelObject {

  @Expose
  private String token;

  public String getToken() {
    return token;
  }

  @Override
  public String toString() {
    return String.format("Token[%s]", token);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Token && ((Token) other).token.equals(token);
  }

  @Override
  public int hashCode() {
    return token.hashCode();
  }

}
