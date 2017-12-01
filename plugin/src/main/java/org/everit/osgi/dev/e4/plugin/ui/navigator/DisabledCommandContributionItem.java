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

import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * A context menu item for a disabled eosgi command.
 */
public class DisabledCommandContributionItem extends CommandContributionItem {

  public DisabledCommandContributionItem(
      final CommandContributionItemParameter contributionParameters) {
    super(contributionParameters);
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

}
