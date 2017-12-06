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
package org.everit.osgi.dev.e4.plugin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

/**
 * Helper class that implements a functional interface to get the child of a dependency node. See
 * usage.
 */
public class DependencyNodeChildResolver
    implements Function<DependencyNode, List<DependencyNode>> {

  @Override
  public List<DependencyNode> apply(final DependencyNode dependencyNode) {
    List<DependencyNode> children = dependencyNode.getChildren();
    List<DependencyNode> result = new ArrayList<>();
    for (DependencyNode child : children) {
      // Only add if this node does not lose in conflicting with other node in the graph.
      if (!child.getData().containsKey(ConflictResolver.NODE_DATA_WINNER)) {
        result.add(child);
      }
    }

    return result;
  }

}
