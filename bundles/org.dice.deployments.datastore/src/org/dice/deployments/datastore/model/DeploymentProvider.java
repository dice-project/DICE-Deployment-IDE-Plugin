package org.dice.deployments.datastore.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.dice.deployments.client.model.Blueprint;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

// TODO: Add update functionality to past deployments

public final class DeploymentProvider {

  private final static Map<IProject, DeploymentProvider> INSTANCES =
      new HashMap<>();

  public static DeploymentProvider getProvider(IProject project) {
    if (!INSTANCES.containsKey(project)) {
      INSTANCES.put(project, new DeploymentProvider(project));
    }
    return INSTANCES.get(project);
  }

  private Map<String, Deployment> deployments;
  private IPath storage;
  private Gson parser;

  private DeploymentProvider(IProject project) {
    deployments = new TreeMap<>(); // For stable order of values()
    storage = project.getLocation().append("deploys.json");
    parser = createParser();
    if (storage.toFile().canRead()) {
      try {
        loadFromDisk();
      } catch (IOException e) {
        // TODO: Inform user that file cannot be read
        e.printStackTrace();
      }
    }
  }

  private class TimeSerializer implements JsonSerializer<ZonedDateTime> {
    @Override
    public JsonElement serialize(ZonedDateTime time, Type type,
        JsonSerializationContext context) {
      DateTimeFormatter fmt =
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);
      return new JsonPrimitive(
          fmt.format(time.truncatedTo(ChronoUnit.MILLIS)));
    }
  }

  private class TimeDeserializer implements JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(JsonElement json, Type type,
        JsonDeserializationContext context) throws JsonParseException {
      DateTimeFormatter fmt =
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);
      return ZonedDateTime.from(fmt.parse(json.getAsString()));
    }
  }

  private Gson createParser() {
    GsonBuilder builder = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .serializeNulls().excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ZonedDateTime.class, new TimeDeserializer())
        .registerTypeAdapter(ZonedDateTime.class, new TimeSerializer());
    return builder.create();
  }

  private void loadFromDisk() throws IOException {
    JsonReader reader = new JsonReader(new FileReader(storage.toOSString()));
    deployments = parser.fromJson(reader, deployments.getClass());
    reader.close();
  }

  private void saveToDisk() throws IOException {
    JsonWriter writer = new JsonWriter(new FileWriter(storage.toOSString()));
    parser.toJson(deployments, deployments.getClass(), writer);
    writer.close();
  }

  public synchronized boolean createDeloyment(Blueprint blueprint) {
    Deployment d = new Deployment(blueprint);
    deployments.put(d.getId(), d);
    try {
      saveToDisk();
    } catch (IOException e) {
      // TODO: Log reason for failure
      return false;
    }
    return true;
  }

  public synchronized boolean deleteDeployment(String id) {
    return deployments.remove(id) != null;
  }

  public Collection<Deployment> listDeployments() {
    return deployments.values();
  }

}
