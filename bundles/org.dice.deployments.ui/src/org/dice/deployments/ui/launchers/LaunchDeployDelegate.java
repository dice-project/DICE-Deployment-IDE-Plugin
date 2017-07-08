package org.dice.deployments.ui.launchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dice.deployments.client.exception.ClientError;
import org.dice.deployments.client.http.Result;
import org.dice.deployments.client.model.Blueprint;
import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Error;
import org.dice.deployments.client.model.Message;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.model.ContainerProvider;
import org.dice.deployments.datastore.model.DeploymentProvider;
import org.dice.deployments.datastore.model.ServiceProvider;
import org.dice.deployments.ui.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class LaunchDeployDelegate extends LaunchConfigurationDelegate {

  @Override
  public void launch(ILaunchConfiguration config, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    SubMonitor sub = SubMonitor.convert(monitor, 100);

    String blueprintPath =
        config.getAttribute(LaunchDeployConfig.BLUEPRINT_PATH, "");
    String resourcesPath =
        config.getAttribute(LaunchDeployConfig.RESOURCES_PATH, "");
    String serviceId = config.getAttribute(LaunchDeployConfig.SERVICE_ID, "");
    String containerId =
        config.getAttribute(LaunchDeployConfig.CONTAINER_ID, "");

    Path archive = packageBlueprintData(blueprintPath, resourcesPath);
    sub.worked(20);

    // TODO: Add check for project here
    IProject project = Utils.getResourceFromPath(blueprintPath).getProject();
    Map<String, String> metadata = gatherMetadata(project);

    // Force container update in order to operate on a fresh list.
    ContainerProvider.INSTANCE.update();
    Container container = ContainerProvider.INSTANCE.get(containerId);
    Service service = ServiceProvider.INSTANCE.get(serviceId);
    Blueprint blueprint =
        deployBlueprint(service, container, archive, metadata);

    // Force container update in order to operate on a fresh list.
    ContainerProvider.INSTANCE.update();
    container = ContainerProvider.INSTANCE.get(containerId);

    DeploymentProvider provider = DeploymentProvider.getProvider(project);
    provider.createDeloyment(blueprint);
    ContainerProvider.INSTANCE.update();

    sub.worked(80);
  }

  private static Map<String, String> gatherMetadata(IProject project) {
    Map<String, String> meta = new HashMap<>();
    meta.put("timestamp", Instant.now().toString());
    meta.put("project-name", project.getName());

    meta.putAll(addGitMetadata(project));
    meta.putAll(addSvnMetadata(project));

    return meta;
  }

  private static Map<String, String> addSvnMetadata(IProject project) {
    // TODO: SVN metadata extraction is currently missing.
    return new HashMap<>();
  }

  private static Map<String, String> addGitMetadata(IProject project) {
    Map<String, String> data = new HashMap<>();

    // TODO: Next piece of code check if jgit bundle is present if we ever
    // decide to make metadata extraction optional. Note that besides this
    // check, we need to code around
    // OSGI loader in order not to get errors on instantiation of the plugin.
    /*
     * Bundle bundle = Platform.getBundle("org.eclipse.jgit"); if (bundle ==
     * null) { return data; }
     */

    try {
      Repository repository = new RepositoryBuilder()
          .findGitDir(project.getLocation().toFile()).build();
      try (RevWalk walk = new RevWalk(repository)) {
        RevCommit commit =
            walk.parseCommit(repository.resolve(Constants.HEAD));
        data.put("git-commit-id", commit.getId().name());
        data.put("git-commit-msg", commit.getFullMessage());
      }
    } catch (IOException e) {
      // TODO: Log error to eclipse
    } catch (NullPointerException e) {
      // TODO: Log error to eclipse
    }

    return data;
  }

  private Blueprint deployBlueprint(Service service, Container container,
      Path archive, Map<String, String> metadata) throws CoreException {
    String errorMsg = "Failed to upload blueprint";

    try {
      Result<Blueprint, Message> response =
          service.deployBlueprint(container, archive.toFile(), metadata, true);
      if (!response.ok) {
        throw prepareCoreException(null, errorMsg, response.second.toString());
      }
    } catch (ClientError e) {
      throw prepareCoreException(e, errorMsg);
    }

    ContainerProvider.INSTANCE.update();
    container = ContainerProvider.INSTANCE.get(container.getId());
    Blueprint blueprint = container.waitWhileBusy();

    if (blueprint == null) {
      throw prepareCoreException(null, "Blueprint failed to upload");
    }
    if (blueprint.getInError()) {
      List<String> errors = new ArrayList<>();
      errors.add("Blueprint failed to install");
      for (Error e : blueprint.getErrors()) {
        errors.add(e.toString());
      }
      throw prepareCoreException(null, errors.toArray(new String[] {}));
    }

    return blueprint;
  }

  private static CoreException prepareCoreException(Exception exc,
      String... details) {
    List<Status> statuses = new ArrayList<>();

    for (String detail : details) {
      Status s = new Status(IStatus.ERROR, "org.dice.deployments.ui", detail);
      statuses.add(s);
    }

    if (exc != null) {
      StackTraceElement[] stackTraces = exc.getStackTrace();
      for (StackTraceElement stackTrace : stackTraces) {
        Status status = new Status(IStatus.ERROR, "org.dice.deployments.ui",
            stackTrace.toString());
        statuses.add(status);
      }
    }

    return new CoreException(new MultiStatus("org.dice.deployments.ui",
        IStatus.ERROR, statuses.toArray(new Status[] {}), details[0], exc));
  }

  private static Path packageBlueprintData(String blueprintPath,
      String resourcesPath) throws CoreException {
    try {
      Path archive = Files.createTempFile("blueprint-", ".tar.gz");
      TarGz c = new TarGz(archive.toString(), "blueprint");
      c.writeFile(Paths.get(blueprintPath), "blueprint.yaml");
      c.writeDir(Paths.get(resourcesPath));
      c.close();
      return archive;
    } catch (IOException e) {
      throw prepareCoreException(e, "Failed to create blueprint archive.");
    }
  }

}
