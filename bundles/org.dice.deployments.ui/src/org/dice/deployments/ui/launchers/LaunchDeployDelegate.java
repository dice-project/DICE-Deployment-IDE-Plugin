package org.dice.deployments.ui.launchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dice.deployments.client.model.Blueprint;
import org.dice.deployments.client.model.Container;
import org.dice.deployments.client.model.Service;
import org.dice.deployments.datastore.model.ContainerProvider;
import org.dice.deployments.datastore.model.DeploymentProvider;
import org.dice.deployments.datastore.model.ServiceProvider;
import org.dice.deployments.ui.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

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

    Path archive;
    try {
      archive = packageBlueprintData(blueprintPath, resourcesPath);
    } catch (IOException e) {
      System.err.println("Failed to create blueprint archive.");
      e.printStackTrace();
      return;
    }
    sub.worked(20);

    // Force container update in order to operate on a fresh list.
    ContainerProvider.INSTANCE.update();
    Container container = ContainerProvider.INSTANCE.get(containerId);

    Service service = ServiceProvider.INSTANCE.get(serviceId);
    service.emptyContainer(container);
    ContainerProvider.INSTANCE.update();
    if (!container.waitWhileBusy()) {
      System.err.printf("Failed to empty the container %s\n", containerId);
      return;
    }
    sub.worked(20);

    Blueprint blueprint = service.deployBlueprint(container, archive.toFile());
    if (blueprint == null) {
      System.err.printf("Failed to upload the blueprint to container %s\n",
          containerId);
      return;
    }

    // TODO: Add check for project here
    IProject project = Utils.getResourceFromPath(blueprintPath).getProject();
    DeploymentProvider provider = DeploymentProvider.getProvider(project);
    provider.createDeloyment(blueprint);
    ContainerProvider.INSTANCE.update();
    container.waitWhileBusy();

    sub.worked(60);
  }

  private static Path packageBlueprintData(String blueprintPath,
      String resourcesPath) throws IOException {
    Path archive = Files.createTempFile("blueprint-", ".tar.gz");
    TarGz c = new TarGz(archive.toString(), "blueprint");
    c.writeFile(Paths.get(blueprintPath), "blueprint.yaml");
    c.writeDir(Paths.get(resourcesPath));
    c.close();

    return archive;
  }

}
