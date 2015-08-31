package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiModelChangeListener;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class BundlesNode extends AbstractNode implements EosgiModelChangeListener {

  private static final String NODE_NAME = "Bundle projects";

  private IProject project;

  public BundlesNode(final IProject project, final EosgiNodeChangeListener listener) {
    this.project = project;
    setListener(listener);
    setName(NODE_NAME);
    Activator.getDefault().getEosgiManager().addModelChangeListener(this);
    outdated = true;
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      List<String> bundleInfos = eosgiManager.fetchBundlesBy(project);
      List<LeafNode> leafNodes = new ArrayList<>();
      for (String bundleInfo : bundleInfos) {
        LeafNode leafNode = new LeafNode();
        leafNode.setName(bundleInfo);
        leafNode.setIcon("icons/bundle_obj.gif");
        leafNodes.add(leafNode);
      }
      children = leafNodes.toArray(new LeafNode[] {});
      outdated = false;
    }
    if (children == null || children.length == 0) {
      setLabel(" (No relevant bundle project found)");
    } else {
      setLabel(null);
    }
    return children;
  }

  @Override
  public String getIcon() {
    return "icons/osgi-logo.ico";
  }

  public IProject getProject() {
    return project;
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
  public void modelChanged(final Object object) {
    if (object instanceof IProject) {
      IProject proj = (IProject) object;
      if (getListener() != null) {
        outdated = true;
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }

  @Override
  public String toString() {
    return "BundlesNode [project=" + project + ", children=" + Arrays.toString(children) + ", icon="
        + icon + ", label=" + label + ", name=" + name + ", outdated=" + outdated + ", value="
        + value + "]";
  }

}
