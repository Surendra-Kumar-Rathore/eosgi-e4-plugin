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
package org.everit.osgi.dev.e4.plugin.m2e.xml;

import java.util.List;
import java.util.Map;

import org.everit.osgi.dev.e4.plugin.m2e.model.BundleSettings;

/**
 * DTO class for an environment.
 */
public class EnvironmentDTO {
  public BundleSettings bundleSettings;

  public String framework;

  public String id;

  public Map<String, String> systemProperties;

  public List<String> vmOptions;

  public EnvironmentDTO bundleSettings(final BundleSettings bundleSettings) {
    this.bundleSettings = bundleSettings;
    return this;
  }

  public EnvironmentDTO framework(final String framework) {
    this.framework = framework;
    return this;
  }

  public EnvironmentDTO id(final String id) {
    this.id = id;
    return this;
  }

  public EnvironmentDTO systemProperties(final Map<String, String> systemProperties) {
    this.systemProperties = systemProperties;
    return this;
  }

  @Override
  public String toString() {
    return "EnvironmentDTO [bundleSettings=" + bundleSettings + ", framework=" + framework + ", id="
        + id + ", systemProperties=" + systemProperties + ", vmOptions=" + vmOptions + "]";
  }

  public EnvironmentDTO vmOptions(final List<String> vmOptions) {
    this.vmOptions = vmOptions;
    return this;
  }

}
