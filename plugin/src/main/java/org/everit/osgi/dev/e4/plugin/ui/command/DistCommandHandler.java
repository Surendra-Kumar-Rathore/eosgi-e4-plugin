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
package org.everit.osgi.dev.e4.plugin.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * IHandler implementation for dist creation.
 */
public class DistCommandHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    Object applicationContext = event.getApplicationContext();
    if (!(applicationContext instanceof IEvaluationContext)) {
      throw new IllegalArgumentException(
          "Parameter should be instance of IEvaluationContext: " + applicationContext);
    }
    Object singleSelection =
        CommandUtil.getSingleSelection((IEvaluationContext) applicationContext);

    if (!(singleSelection instanceof ExecutableEnvironment)) {
      throw new IllegalArgumentException(
          "Selected item should be instance of EOSGiProject: " + singleSelection);
    }

    final ExecutableEnvironment executableEnvironment = (ExecutableEnvironment) singleSelection;

    Job job = Job.create("Launching OSGi Environment", (IJobFunction) monitor -> {
      executableEnvironment.getEOSGiProject().dist(executableEnvironment, monitor);
      return Status.OK_STATUS;
    });
    job.schedule();

    return null;
  }
}
