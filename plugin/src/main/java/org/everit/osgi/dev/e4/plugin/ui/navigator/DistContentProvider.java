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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContext;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContextManager;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiPluginActivator;
import org.everit.osgi.dev.e4.plugin.ui.navigator.nodes.AbstractNode;
import org.everit.osgi.dev.e4.plugin.ui.navigator.nodes.RootNode;

/**
 * TreeNodeContentProvider implementation for manage the EOSGI nodes in ProjectExplorer.
 */
public class DistContentProvider extends TreeNodeContentProvider
    implements EosgiNodeChangeListener {
  private static final Object[] NO_CHILDREN = new Object[] {};

  private Map<AbstractNode, AbstractNode[]> eosgiNodeCache = new ConcurrentHashMap<>();

  private EOSGiContextManager manager;

  private final EOSGiPluginActivator plugin;

  private Map<IProject, AbstractNode[]> projectCache = new ConcurrentHashMap<>();

  private StructuredViewer viewer;

  /**
   * Constructor.
   */
  public DistContentProvider() {
    super();
    plugin = EOSGiPluginActivator.getDefault();
    if (plugin != null) {
      manager = plugin.getEOSGiManager();
    } else {
      throw new IllegalStateException("EOSGI plugin context not found!");
    }
  }

  @Override
  public void dispose() {
    for (Entry<IProject, AbstractNode[]> abstractNodeEntry : projectCache.entrySet()) {
      for (AbstractNode abstractNode : abstractNodeEntry.getValue()) {
        abstractNode.dispose();
        resolveNodesFor(abstractNodeEntry.getKey());
      }
    }
    projectCache = null;
    eosgiNodeCache = null;
  }

  @Override
  public Object[] getChildren(final Object parentElement) {
    Object[] children = NO_CHILDREN;
    if (parentElement == null) {
      children = NO_CHILDREN;
    } else if (parentElement instanceof IProject) {
      children = handleProject(parentElement);
    } else if (parentElement instanceof AbstractNode) {
      children = handleEosgiNode(parentElement);
    }

    return children;
  }

  @Override
  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  @Override
  public Object getParent(final Object element) {
    return null;
  }

  private Runnable getRefreshRunnable(final AbstractNode node) {
    return new Runnable() {
      @Override
      public void run() {
        viewer.refresh(node);
      }
    };
  }

  private Object[] handleEosgiNode(final Object parentElement) {
    if (eosgiNodeCache.containsKey(parentElement)) {
      return eosgiNodeCache.get(parentElement);
    } else {
      AbstractNode abstractNode = (AbstractNode) parentElement;
      AbstractNode[] eosgiNodes = abstractNode.getChildren();
      eosgiNodeCache.put(abstractNode, eosgiNodes);
      return eosgiNodes;
    }
  }

  private Object[] handleProject(final Object parentElement) {
    IProject project = (IProject) parentElement;
    if (!project.isOpen()) {
      resolveNodesFor(project);
      return NO_CHILDREN;
    }
    EOSGiContext context = manager.findOrCreate(project);
    if (context != null) {
      if (projectCache.containsKey(project)) {
        return projectCache.get(project);
      } else {
        RootNode[] nodes = new RootNode[] { new RootNode(context, this) };
        projectCache.put(project, nodes);
        return nodes;
      }
    } else {
      return NO_CHILDREN;
    }
  }

  // FIXME unused
  // @SuppressWarnings("unused")
  // private Runnable getUpdateRunnable(final AbstractNode resource) {
  // return new Runnable() {
  // @Override
  // public void run() {
  // viewer.update(resource, null);
  // }
  // };
  // }

  @Override
  public boolean hasChildren(final Object element) {
    if (element instanceof IProject) {
      return true;
    } else if (element instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) element;
      AbstractNode[] children = node.getChildren();
      return (children != null) && (children.length > 0);
    } else {
      return false;
    }
  }

  @Override
  public void inputChanged(final Viewer aviewer, final Object oldInput, final Object newInput) {
    if (aviewer instanceof StructuredViewer) {
      viewer = (StructuredViewer) aviewer;
    }
  }

  @Override
  public synchronized void nodeChanged(final EosgiNodeChangeEvent event) {
    if ((event != null) && (event.getNode() != null)) {
      AbstractNode node = event.getNode();

      eosgiNodeCache.remove(node);

      Control ctrl = viewer.getControl();
      if ((ctrl == null) || ctrl.isDisposed()) {
        return;
      }

      Runnable refreshRunnable = getRefreshRunnable(node);
      final Collection<Runnable> refreshRunnables = Arrays.asList(refreshRunnable);

      if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
        runUpdates(refreshRunnables);
      } else {
        ctrl.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            // Abort if this happens after disposes
            Control ctrl = viewer.getControl();
            if ((ctrl == null) || ctrl.isDisposed()) {
              return;
            }

            runUpdates(refreshRunnables);
          }
        });
      }
    }
  }

  private void releaseNode(final AbstractNode abstractNode) {
    AbstractNode[] children = abstractNode.getChildren();
    if (children != null) {
      for (AbstractNode child : children) {
        releaseNode(child);
      }
    }
    abstractNode.dispose();
    eosgiNodeCache.remove(abstractNode);
  }

  private void resolveNodesFor(final IProject project) {
    AbstractNode[] abstractNodes = projectCache.remove(project);
    if (abstractNodes != null) {
      for (AbstractNode abstractNode : abstractNodes) {
        releaseNode(abstractNode);
      }
    }
  }

  // FIXME rawtypes
  @SuppressWarnings("rawtypes")
  private void runUpdates(final Collection runnables) {
    Iterator runnableIterator = runnables.iterator();
    while (runnableIterator.hasNext()) {
      ((Runnable) runnableIterator.next()).run();
    }

  }
}
