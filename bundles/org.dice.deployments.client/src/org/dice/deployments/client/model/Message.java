package org.dice.deployments.client.model;

import com.google.gson.annotations.Expose;

public class Message extends ModelObject {

  @Expose
  private String detail;

  public Message() {
    this("");
  }

  public Message(String detail) {
    this.detail = detail;
  }

  public String getDetail() {
    return detail;
  }

  @Override
  public String toString() {
    return String.format("Message[%s]", detail);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Message && ((Message) other).detail.equals(detail);
  }

  @Override
  public int hashCode() {
    return detail.hashCode();
  }

}
