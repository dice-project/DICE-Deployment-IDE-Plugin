package org.dice.deployments.client.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.CloseableHttpResponse;

public class Response {

  public int statusCode;
  public byte[] data;

  public Response(CloseableHttpResponse response) throws IOException {
    statusCode = response.getStatusLine().getStatusCode();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    response.getEntity().writeTo(baos);
    data = baos.toByteArray();
  }

  public String asString() {
    try {
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new InternalError("JVM is missing UTF-8 encoding.");
    }
  }
}
