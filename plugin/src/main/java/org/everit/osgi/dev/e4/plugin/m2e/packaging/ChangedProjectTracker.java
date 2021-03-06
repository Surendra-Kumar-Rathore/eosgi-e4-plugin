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
package org.everit.osgi.dev.e4.plugin.m2e.packaging;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.maven.model.BuildBase;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Tracks changes in projects and fires change event if any of the projects should be re-packaged
 * later. More specifically: If any files changes that are not in {@link BuildBase#getDirectory()}
 * or the file is one of the generated artifacts of the project
 * <ul>
 * <li>{@link MavenProject#getArtifact()}</li>
 * <li>{@link MavenProject#getAttachedArtifacts()}</li>
 * </ul>
 * .
 */
public class ChangedProjectTracker implements IResourceChangeListener {

  private static boolean isResourceArtifactFileOrSource(final IResourceDelta resourceDelta,
      final Set<File> attachedFiles, final boolean inTargetFolder) {

    if (!inTargetFolder) {
      return true;
    }
    IResource resource = resourceDelta.getResource();
    File resourceFile = resource.getLocation().toFile();

    return attachedFiles.contains(resourceFile);

  }

  /**
   * The function returns whether the a resource is changed.
   *
   * @param delta
   *          The delta of the resource.
   * @return True if the resource is affected, but false if the resource is a folder that has some
   *         resource that is affected.
   */
  private static boolean resourceDeltaMeansResourceChange(final IResourceDelta delta) {
    int kind = delta.getKind();
    return delta.getFlags() != 0
        || (kind != 0 && (kind & IResourceDelta.CHANGED) == 0);
  }

  private final Function<IProject, ProjectArtifacts> projectArtifactsProvider;

  private final Consumer<IProject> projectChangeHandler;

  public ChangedProjectTracker(final Consumer<IProject> projectChangeHandler,
      final Function<IProject, ProjectArtifacts> projectArtifactsProvider) {
    this.projectChangeHandler = projectChangeHandler;
    this.projectArtifactsProvider = projectArtifactsProvider;
  }

  private boolean processChangeEventOnMavenProject(final IResourceDelta delta,
      final IMavenProjectFacade mavenProjectFacade) {
    try {
      String targetDirectory = mavenProjectFacade.getMavenProject(new NullProgressMonitor())
          .getBuild().getDirectory();
      File targetDirectoryFile = new File(targetDirectory);

      ProjectArtifacts projectArtifacts =
          projectArtifactsProvider.apply(mavenProjectFacade.getProject());

      Set<File> artifactFileSet = toArtifactFileSet(projectArtifacts);

      return processMavenProjectChangeDeltaRecurce(delta.getAffectedChildren(), targetDirectoryFile,
          artifactFileSet, false);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private void processChangeEventRecurse(final IResourceDelta delta) {
    IResource resource = delta.getResource();
    if (resource instanceof IProject) {
      IProject eclipseProject = (IProject) resource;

      if (!projectArtifactsEvaluated(eclipseProject)) {
        return;
      }

      if (eclipseProject.isOpen()) {
        IMavenProjectFacade mavenProjectFacade =
            MavenPlugin.getMavenProjectRegistry().getProject(eclipseProject);

        if (mavenProjectFacade != null
            && processChangeEventOnMavenProject(delta, mavenProjectFacade)) {
          projectChangeHandler.accept(eclipseProject);
        }
      }
    } else {
      IResourceDelta[] affectedChildren = delta.getAffectedChildren();
      if (affectedChildren != null) {
        for (IResourceDelta childDelta : affectedChildren) {
          processChangeEventRecurse(childDelta);
        }
      }
    }
  }

  private boolean processMavenProjectChangeDeltaRecurce(final IResourceDelta[] deltaArray,
      final File targetDirectoryFile, final Set<File> artifactFiles,
      final boolean pInTargetFolder) {

    for (IResourceDelta delta : deltaArray) {
      boolean inTargetFolder =
          pInTargetFolder || targetDirectoryFile.equals(delta.getResource().getLocation().toFile());

      if (resourceDeltaMeansResourceChange(delta)
          && isResourceArtifactFileOrSource(delta, artifactFiles, inTargetFolder)) {
        return true;
      }

      if (processMavenProjectChangeDeltaRecurce(delta.getAffectedChildren(), targetDirectoryFile,
          artifactFiles, inTargetFolder)) {
        return true;
      }
    }
    return false;
  }

  private boolean projectArtifactsEvaluated(final IProject resource) {
    return projectArtifactsProvider.apply(resource) != null;
  }

  @Override
  public synchronized void resourceChanged(final IResourceChangeEvent event) {
    int eventType = event.getType();

    switch (eventType) {
      case IResourceChangeEvent.PRE_DELETE:
      case IResourceChangeEvent.PRE_CLOSE:
        IResource resource = event.getResource();
        if (resource instanceof IProject && projectArtifactsEvaluated((IProject) resource)) {
          projectChangeHandler.accept((IProject) resource);
        }
        break;
      case IResourceChangeEvent.POST_CHANGE:
        processChangeEventRecurse(event.getDelta());
        break;
      default:
        break;
    }
  }

  private Set<File> toArtifactFileSet(final ProjectArtifacts projectArtifacts) {
    Set<File> result = new HashSet<>();
    if (projectArtifacts.artifact != null) {
      // Happens if no file is generated from packaging (e.g.: pom) but a plugin generates an
      // attached artifact
      result.add(projectArtifacts.artifact.getFile());
    }

    for (Artifact artifact : projectArtifacts.attachedArtifacts) {
      result.add(artifact.getFile());
    }
    return result;
  }

}
