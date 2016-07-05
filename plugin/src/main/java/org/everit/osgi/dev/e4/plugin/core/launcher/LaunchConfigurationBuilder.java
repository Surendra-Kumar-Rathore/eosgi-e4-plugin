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
package org.everit.osgi.dev.e4.plugin.core.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.everit.osgi.dev.dist.util.configuration.DistributedEnvironmentConfigurationProvider;
import org.everit.osgi.dev.dist.util.configuration.LaunchConfigurationDTO;
import org.everit.osgi.dev.dist.util.configuration.schema.EnvironmentType;
import org.everit.osgi.dev.dist.util.configuration.schema.UseByType;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiLog;

/**
 * Builder class for launch configurations.
 */
public class LaunchConfigurationBuilder {

  private final EOSGiLog eosgiLog;

  private final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

  /**
   * Constructor.
   *
   * @param projectName
   *          name of the project.
   * @param environmentId
   *          id of the environment
   * @param buildDirectory
   *          build directory of the project.
   */
  public LaunchConfigurationBuilder() {
    ILog log = EOSGiEclipsePlugin.getDefault().getLog();
    eosgiLog = new EOSGiLog(log);
  }

  /**
   * Build a launcher configuration instance.
   *
   * @return {@link ILaunchConfiguration} instance.
   */
  public ILaunchConfiguration build(final String projectName, final String environmentId,
      final String buildDirectory) {
    ILaunchConfigurationType type = manager
        .getLaunchConfigurationType(
            "org.everit.osgi.dev.e4.plugin.core.launcher.launchConfigurationType");

    ILaunchConfigurationWorkingCopy launchConfig = null;
    try {
      launchConfig = type.newInstance(null, environmentId + "_" + projectName);
    } catch (CoreException e) {
      eosgiLog.error("Could not create laucher working copy", e);
    }

    if (launchConfig == null) {
      return null;
    }

    String workingDirectory = buildDirectory + File.separator + "eosgi-dist" + File.separator
        + environmentId;

    DistributedEnvironmentConfigurationProvider configurationProvider =
        new DistributedEnvironmentConfigurationProvider();

    EnvironmentType environmentConfig = configurationProvider
        .getOverriddenDistributedEnvironmentConfig(new File(workingDirectory), UseByType.IDE);

    LaunchConfigurationDTO launchConfigDTO =
        configurationProvider.getLaunchConfiguration(environmentConfig);

    updateCurrentLauncherConfigurationWorkingCopy(launchConfig, projectName,
        environmentId, workingDirectory, launchConfigDTO);

    return launchConfig;
  }

  private String createArgumentsString(
      final List<String> argumentList) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (argumentList == null) {
      return stringBuilder.toString();
    }

    for (String argument : argumentList) {
      stringBuilder.append(' ' + argument);
    }

    String argumentsString = stringBuilder.toString();
    return argumentsString;
  }

  private List<String> createClasspathList(final String rootDirectory, final String classpath)
      throws CoreException {
    List<String> classpathEntryList = new ArrayList<>();

    if ("*".equals(classpath)) {
      List<String> jarFiles = fetchAllJarFileFromRootDirectory(rootDirectory);
      classpathEntryList.addAll(jarFiles);
    } else if (classpath != null) {
      classpathEntryList.addAll(Arrays.asList(classpath.split(":")));
    }

    List<String> classpathMementoEntryList = new ArrayList<>();
    for (String classpathEntry : classpathEntryList) {
      classpathMementoEntryList.add(toMemento(rootDirectory, classpathEntry));
    }
    return classpathMementoEntryList;
  }

  private String createVmArgs(final List<String> vmArgumentList) {
    StringBuilder stringBuilder = new StringBuilder();

    if (vmArgumentList == null) {
      return stringBuilder.toString();
    }

    for (String vmOption : vmArgumentList) {
      if (vmOption.indexOf("Xrunjdwp") == -1) {
        stringBuilder.append(" " + vmOption);
      }
    }
    return stringBuilder.toString();
  }

  private List<String> fetchAllJarFileFromRootDirectory(final String rootDirectory) {
    File rootFolder = new File(rootDirectory);
    if (rootFolder.isDirectory()) {
      String[] files = rootFolder.list(new SuffixFileFilter(".jar"));
      if (files != null) {
        return Arrays.asList(files);
      }
    }
    return Collections.emptyList();
  }

  private String toMemento(final String rootDirectory, final String classpath)
      throws CoreException {
    IPath path = new Path(rootDirectory + "/" + classpath);
    IRuntimeClasspathEntry classpathEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
    // IRuntimeClasspathEntry2 newRuntimeClasspathEntry =
    // LaunchingPlugin.getDefault().newRuntimeClasspathEntry("");
    classpathEntry.setExternalAnnotationsPath(path);
    return classpathEntry.getMemento();
  }

  private void updateCurrentLauncherConfigurationWorkingCopy(
      final ILaunchConfigurationWorkingCopy launchConfig,
      final String projectName, final String environmentId,
      final String workingDirectory,
      final LaunchConfigurationDTO environmentConfigurationDTO) {

    String argumentsString = createArgumentsString(environmentConfigurationDTO.programArguments);
    String vmArgsList = createVmArgs(environmentConfigurationDTO.vmArguments);

    List<String> classPathList = new ArrayList<>();
    try {
      classPathList = createClasspathList(workingDirectory,
          environmentConfigurationDTO.classpath);
    } catch (CoreException e) {
      eosgiLog.error("Could not resolv classpath entries.", e);
    }

    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
        environmentConfigurationDTO.mainClass);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
        workingDirectory);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
        argumentsString);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classPathList);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgsList);
    launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);

    launchConfig.setAttribute(EOSGILaunchConfigurationDelegate.LAUNCHER_ATTR_ENVIRONMENT_ID,
        environmentId);

  }

}
