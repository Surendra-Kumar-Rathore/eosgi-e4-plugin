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
package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentsNodeDTO;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environments.
 */
public class EnvironmentsNode extends AbstractNode {

  private static final String OUTDATED_LABEL = " (outdated)";

  private EOSGiContext context;

  /**
   * Constructor.
   *
   * @param context
   *          relevant {@link EOSGiContext} reference.
   * @param listener
   *          listener for changes.
   */
  public EnvironmentsNode(final EOSGiContext context, final EosgiNodeChangeListener listener) {
    super("Environments", listener, null);
    this.context = context;
    outdated = true;
    context.delegateObserver(this);
  }

  @Override
  public void dispose() {
    context = null;
    super.dispose();
  }

  private void disposeChildren() {
    if (children.length > 0) {
      for (AbstractNode abstractNode : children) {
        abstractNode.dispose();
      }
      children = NO_CHILDREN;
    }
  }

  private List<EnvironmentNode> generateChildNodes() {
    List<EnvironmentNode> nodes = new ArrayList<>();

    context.fetchEnvironments().forEach((environmentDTO) -> {
      EnvironmentNode node = new EnvironmentNode(context, environmentDTO.id, getListener());
      environmentDTO.observable.addObserver(node);
      if (environmentDTO.outdated) {
        node.setLabel(OUTDATED_LABEL);
      }
      nodes.add(node);
    });

    return nodes;
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      disposeChildren();
      children = generateChildNodes()
          .toArray(new EnvironmentNode[] {});
      outdated = false;
    }
    return children.clone();
  }

  @Override
  public String getIcon() {
    return "icons/console_view.gif";
  }

  @Override
  public String getText() {
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
  }

  @Override
  public String toString() {
    return "EnvironmentsNode [context=" + context + "]";
  }

  @Override
  public void update(final Observable o, final Object arg) {
    if (arg != null && arg instanceof EnvironmentsNodeDTO) {
      EnvironmentsNodeDTO environmentsNodeDTO = (EnvironmentsNodeDTO) arg;
      if (this.context.equals(environmentsNodeDTO.context)) {
        outdated = true;
        changed(new EosgiNodeChangeEvent(this));
      }
    }
  }

}
