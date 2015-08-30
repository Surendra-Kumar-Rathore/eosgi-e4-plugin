package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Manage IProject , MavenProject connections and store eosgi dist informations.
 */
public interface EosgiManager {

  void addModelChangeListener(EosgiModelChangeListener listener);

  List<String> fetchBundlesBy(IProject project);

  List<String> fetchEnvironmentsBy(IProject project);

  void generateDistFor(final IProject project, final String environmentId);

  void init(IProject[] projects) throws CoreException;

  void refreshProject(IProject project, MavenProject mavenProject);

  void registerProject(IProject project);

  void removeModelChangeListener(EosgiModelChangeListener listener);

  void updateEnvironments(IProject project, Xpp3Dom configuration);

}
