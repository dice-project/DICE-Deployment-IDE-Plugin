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
    IProject project = Utils
        .getResourceFromPath(
            config.getAttribute(LaunchDeployConfig.BLUEPRINT_PATH, ""))
        .getProject();
    DeploymentProvider provider = DeploymentProvider.getProvider(project);

    Path archive = null;
    try {
      archive =
          Files.createTempDirectory("blueprint-").resolve("blueprint.yaml");
      Path local = Paths
          .get(config.getAttribute(LaunchDeployConfig.BLUEPRINT_PATH, ""));
      Files.copy(local, archive);
    } catch (IOException e) {
      // TODO: What would be the course of action if /tmp is not working?
      e.printStackTrace();
      return;
    }

    String serviceId = config.getAttribute(LaunchDeployConfig.SERVICE_ID, "");
    Service service = ServiceProvider.INSTANCE.get(serviceId);

    String containerId =
        config.getAttribute(LaunchDeployConfig.CONTAINER_ID, "");
    Container container = ContainerProvider.INSTANCE.get(containerId);

    service.emptyContainer(container);
    ContainerProvider.INSTANCE.update();
    if (!container.waitWhileBusy()) {
      // TODO: Inform user about error
      return;
    }

    Blueprint blueprint = service.deployBlueprint(container, archive.toFile());
    if (blueprint == null) {
      // TODO: Inform user about error
      return;
    }
    provider.createDeloyment(blueprint);
    ContainerProvider.INSTANCE.update();
    container.waitWhileBusy();

    SubMonitor sub = SubMonitor.convert(monitor, 10);
    sub.worked(10);
  }

}
