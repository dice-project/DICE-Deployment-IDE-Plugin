package org.dice.deployments.client.http;

public class Result<T, U> {

  public T first;
  public U second;
  public boolean ok;

  public Result(T first, U second) {
    this.first = first;
    this.second = second;
    this.ok = first != null;
  }

  @Override
  public String toString() {
    return String.format("Result[%b]", ok);
  }

}
