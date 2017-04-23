/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.ui.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.dist.util.attach.EnvironmentRuntimeInfo;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;
import org.everit.osgi.dev.e4.plugin.ui.command.CommandUtil;

public class EOSGiProjectPopupMenu extends ExtensionContributionFactory {

  public static final String COMMAND_ID_PREFIX = "org.everit.osgi.dev.e4.plugin.command.";

  private static final ImageDescriptor ICON_CLEAN = createIcon("trash.png");

  private static final ImageDescriptor ICON_DEBUG = createIcon("ldebug_obj.gif");

  private static final ImageDescriptor ICON_DIST = createIcon("refresh_tab.gif");

  private static final ImageDescriptor ICON_START = createIcon("lrun_obj.gif");

  private static final ImageDescriptor ICON_STOP = createIcon("terminatedlaunch_obj.gif");

  private static final ImageDescriptor ICON_SYNCBACK = createIcon("undo_edit.png");

  private static ImageDescriptor createIcon(final String fileName) {
    ImageDescriptor fileImageDescriptor =
        ImageDescriptor.createFromFile(EOSGiProjectPopupMenu.class, "/icons/" + fileName);

    return ImageDescriptor.createFromImage(fileImageDescriptor.createImage());
  }

  public EOSGiProjectPopupMenu() {
  }

  private void addMenuItem(final String label, final String commandId,
      final IContributionRoot additions, final ImageDescriptor icon,
      final IServiceLocator serviceLocator, final Map<Object, Object> parameters,
      final boolean enabled) {
    CommandContributionItem menuItem =
        createMenuItem(label, commandId, icon, serviceLocator, parameters, enabled);
    additions.addContributionItem(menuItem, null);
  }

  private void addMenuItemsForEnvironment(final ExecutableEnvironment executableEnvironment,
      final IContributionRoot additions, final IServiceLocator serviceLocator) {
    EOSGiVMManager eosgiVMManager =
        EOSGiEclipsePlugin.getDefault().getEOSGiManager().getEosgiVMManager();
    eosgiVMManager.refresh();

    final Set<EnvironmentRuntimeInfo> runtimeInformations =
        eosgiVMManager.getRuntimeInformations(executableEnvironment.getEnvironmentId(),
            executableEnvironment.getRootFolder());

    if (runtimeInformations.isEmpty()) {
      boolean launchInProgress = executableEnvironment.getEOSGiProject().isLaunchInProgress();
      addMenuItem("Start", COMMAND_ID_PREFIX + "start", additions, ICON_START, serviceLocator,
          null, !launchInProgress);

      addMenuItem("Debug", COMMAND_ID_PREFIX + "debug", additions, ICON_DEBUG, serviceLocator,
          null, !launchInProgress);

      addMenuItem("Dist", COMMAND_ID_PREFIX + "dist", additions, ICON_DIST, serviceLocator, null,
          !launchInProgress);
      addSyncbackMenuItem(executableEnvironment, additions, serviceLocator);

      if (executableEnvironment.getRootFolder().exists()) {
        addMenuItem("Clean", COMMAND_ID_PREFIX + "clean", additions, ICON_CLEAN,
            serviceLocator, null, !launchInProgress);
      }
    } else if (runtimeInformations.size() == 1) {
      addMenuItem("Dist", COMMAND_ID_PREFIX + "dist", additions, ICON_DIST, serviceLocator, null,
          true);
      addSyncbackMenuItem(executableEnvironment, additions, serviceLocator);

      String vmId = runtimeInformations.iterator().next().virtualMachineId;
      Map<Object, Object> parameters = new HashMap<>();
      parameters.put(COMMAND_ID_PREFIX + "stopCommand.vmId", vmId);

      addMenuItem("Stop", COMMAND_ID_PREFIX + "stop", additions, ICON_STOP, serviceLocator,
          parameters, true);
    } else {
      addSyncbackMenuItem(executableEnvironment, additions, serviceLocator);
      MenuManager stopMainMenu = new MenuManager();
      stopMainMenu.setMenuText("Stop");
      stopMainMenu.setImageDescriptor(ICON_STOP);

      for (EnvironmentRuntimeInfo runtimeInfo : runtimeInformations) {
        String vmId = runtimeInfo.virtualMachineId;
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put(COMMAND_ID_PREFIX + "stopCommand.vmId", vmId);

        CommandContributionItem stopMenuItem =
            createMenuItem(vmId, COMMAND_ID_PREFIX + "stop", null, serviceLocator, parameters,
                true);

        stopMainMenu.add(stopMenuItem);

      }

      additions.addContributionItem(stopMainMenu, null);
    }
  }

  private void addSyncbackMenuItem(final ExecutableEnvironment executableEnvironment,
      final IContributionRoot additions, final IServiceLocator serviceLocator) {

    if (executableEnvironment.getRootFolder().exists()) {
      addMenuItem("SyncBack", COMMAND_ID_PREFIX + "syncback", additions, ICON_SYNCBACK,
          serviceLocator, null, true);
    }
  }

  @Override
  public void createContributionItems(final IServiceLocator serviceLocator,
      final IContributionRoot additions) {

    Object selectionObject = getSingleSelection(serviceLocator);

    if (selectionObject == null) {
      return;
    }

    if (selectionObject instanceof ExecutableEnvironment) {
      addMenuItemsForEnvironment((ExecutableEnvironment) selectionObject, additions,
          serviceLocator);
    }

  }

  private CommandContributionItem createMenuItem(final String label, final String commandId,
      final ImageDescriptor icon, final IServiceLocator serviceLocator,
      final Map<?, ?> parameters, final boolean enabled) {

    CommandContributionItemParameter parameter = new CommandContributionItemParameter(
        serviceLocator, null, commandId, CommandContributionItem.STYLE_PUSH);

    parameter.label = label;
    parameter.visibleEnabled = true;
    parameter.icon = icon;
    if (parameters != null) {
      parameter.parameters = parameters;
    }
    if (enabled) {
      return new CommandContributionItem(parameter);
    } else {
      return new DisabledCommandContributionItem(parameter);
    }
  }

  private Object getSingleSelection(final IServiceLocator serviceLocator) {
    ISelectionService selectionService = serviceLocator.getService(ISelectionService.class);

    if (selectionService == null) {
      return null;
    }

    ISelection selection = selectionService.getSelection();

    return CommandUtil.getSingleSelection(selection);
  }

  @Override
  public void setInitializationData(final IConfigurationElement config, final String propertyName,
      final Object data)
      throws CoreException {
    super.setInitializationData(config, propertyName, data);
  }

}
