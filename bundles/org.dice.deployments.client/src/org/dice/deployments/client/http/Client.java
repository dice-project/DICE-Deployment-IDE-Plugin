package org.dice.deployments.client.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.dice.deployments.client.exception.ClientError;
import org.dice.deployments.client.model.Blueprint;
import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Message;
import org.dice.deployments.client.model.Token;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Client {

  private static final int CONNECTION_TIMEOUT_MS = 5000;

  private String token;
  private CloseableHttpClient http;
  private URI address;
  private Gson parser;

  public Client(URI baseAddress, int timeout, String keystoreFile,
      String keystorePass) throws ClientError {
    final RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
        .setSocketTimeout(timeout).build();

    HttpClientBuilder builder =
        HttpClients.custom().setDefaultRequestConfig(config);

    if (keystoreFile != null && !keystoreFile.equals("")) {
      try {
        KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
        tks.load(new FileInputStream(keystoreFile),
            keystorePass.toCharArray());
        SSLContext sslCtx = SSLContexts.custom()
            .loadTrustMaterial(tks, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory csf =
            new SSLConnectionSocketFactory(sslCtx, new String[] {"TLSv1"},
                null, SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(csf);
      } catch (NoSuchAlgorithmException | CertificateException
          | KeyStoreException | KeyManagementException | IOException e) {
        throw new ClientError(e.getMessage());
      }
    }

    http = builder.build();
    token = null;
    address = baseAddress;
    parser = new GsonBuilder().serializeNulls()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithoutExposeAnnotation().create();
  }

  public Client(URI baseAddress, String keystoreFile, String keystorePass)
      throws ClientError {
    this(baseAddress, CONNECTION_TIMEOUT_MS, keystoreFile, keystorePass);
  }

  public boolean authenticate(String username, String password)
      throws IOException {
    Map<String, String> data = new HashMap<>();
    data.put("username", username);
    data.put("password", password);

    Response response = post("/auth/get-token", null, data);
    if (response.statusCode != HttpStatus.SC_OK) {
      return false;
    }
    token = parser.fromJson(response.asString(), Token.class).getToken();
    return true;
  }

  public void close() {
    try {
      http.close();
    } catch (IOException e) {
      // Really cannot do much here, but since we are already tearing the
      // connection down, it is relatively safe to assume that ignoring this
      // will cause no harm.
    }
  }

  // Heartbeat
  public boolean getHeartbeat() throws IOException {
    Response response = get("/heartbeat", null);
    return response.statusCode == HttpStatus.SC_OK;
  }

  // Container methods
  public Result<Container[], Message> listContainers() throws IOException {
    Response response = get("/containers", null);
    return getResult(response, Container[].class, HttpStatus.SC_OK);
  }

  public Result<Container, Message> getContainer(String id)
      throws IOException {
    Response response = get("/containers/" + id, null);
    return getResult(response, Container.class, HttpStatus.SC_OK);
  }

  public Result<Blueprint, Message> deployBlueprint(String containerId,
      File blueprint) throws IOException {
    String path = String.format("/containers/%s/blueprint", containerId);
    Response response = post(path, null, blueprint);
    return getResult(response, Blueprint.class, HttpStatus.SC_ACCEPTED);
  }

  public Result<Blueprint, Message> undeployBlueprint(String containerId)
      throws IOException {
    String path = String.format("/containers/%s/blueprint", containerId);
    Response response = delete(path, null);
    return getResult(response, Blueprint.class, HttpStatus.SC_ACCEPTED);
  }

  // Private API
  private <T> Result<T, Message> getResult(Response resp, Class<T> klass,
      int status) {
    if (resp == null) {
      return new Result<>(null, new Message("Service is not accessible"));
    }
    return resp.statusCode != status ? error(resp) : ok(resp, klass);
  }

  private <T> Result<T, Message> error(Response response) {
    return new Result<>(null,
        parser.fromJson(response.asString(), Message.class));
  }

  private <T> Result<T, Message> ok(Response response, Class<T> klass) {
    return new Result<>(parser.fromJson(response.asString(), klass), null);
  }

  private URI buildURI(String path, Map<String, String> params) {
    URIBuilder builder = new URIBuilder(address);
    builder.setPath(builder.getPath() + path);
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.addParameter(entry.getKey(), entry.getValue());
      }
    }
    try {
      return builder.build();
    } catch (URISyntaxException e) {
      throw new InternalError(String.format("Bad path: %d", path));
    }
  }

  private void addAuth(RequestBuilder builder) {
    if (token != null) {
      builder.addHeader("Authorization", "Token " + token);
    }
  }

  private Response execute(RequestBuilder builder) throws IOException {
    try {
      return new Response(http.execute(builder.build()));
    } catch (IllegalStateException e) {
      return null;
    }
  }

  private Response get(String path, Map<String, String> params)
      throws IOException {
    RequestBuilder builder =
        RequestBuilder.get().setUri(buildURI(path, params));
    addAuth(builder);
    return execute(builder);
  }

  private Response post(String path, Map<String, String> params,
      Map<String, String> data) throws IOException {
    RequestBuilder builder =
        RequestBuilder.post().setUri(buildURI(path, params));
    if (data != null) {
      for (Map.Entry<String, String> entry : data.entrySet()) {
        builder.addParameter(entry.getKey(), entry.getValue());
      }
    }
    addAuth(builder);
    return execute(builder);
  }

  private Response post(String path, Map<String, String> params, File file)
      throws IOException {
    RequestBuilder builder =
        RequestBuilder.post().setUri(buildURI(path, params));
    addAuth(builder);
    HttpEntity entity =
        MultipartEntityBuilder.create().addBinaryBody("file", file).build();
    builder.setEntity(entity);
    return execute(builder);
  }

  private Response delete(String path, Map<String, String> params)
      throws IOException {
    RequestBuilder builder =
        RequestBuilder.delete().setUri(buildURI(path, params));
    addAuth(builder);
    return execute(builder);
  }

}
