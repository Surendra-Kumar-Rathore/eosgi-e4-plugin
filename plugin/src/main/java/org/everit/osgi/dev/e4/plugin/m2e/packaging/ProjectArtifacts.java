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

import java.util.Collection;

import org.eclipse.aether.artifact.Artifact;

/**
 * Holder class for artifacts of a maven project.
 */
public class ProjectArtifacts {

  public final Artifact artifact;

  public final Collection<Artifact> attachedArtifacts;

  public ProjectArtifacts(final Artifact artifact, final Collection<Artifact> attachedArtifacts) {
    this.artifact = artifact;
    this.attachedArtifacts = attachedArtifacts;
  }

}