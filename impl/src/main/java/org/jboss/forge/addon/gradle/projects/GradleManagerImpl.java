/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects;

import org.gradle.jarjar.com.google.common.collect.Lists;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.jboss.forge.addon.gradle.model.GradleModel;
import org.jboss.forge.furnace.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Adam Wyłuda
 */
public class GradleManagerImpl implements GradleManager
{
   private static class ResultHolder
   {
      private volatile boolean result;
   }

   @Override
   public boolean runGradleBuild(String directory, String task, String... arguments)
   {
      String gradleHome = System.getenv("GRADLE_HOME");

      GradleConnector connector = GradleConnector.newConnector()
               .forProjectDirectory(new File(directory));
      if (!Strings.isNullOrEmpty(gradleHome))
      {
         connector = connector.useInstallation(new File(gradleHome));
      }

      ProjectConnection connection = null;
      final ResultHolder holder = new ResultHolder();

      try
      {
         connection = connector.connect();

         BuildLauncher launcher = connection.newBuild().forTasks(task);
         List<String> argList = Lists.newArrayList(arguments);

         launcher = launcher.withArguments(argList.toArray(new String[argList.size()]));

         // Workaround to hide Gradle output in shell
         PrintStream originalOut = System.out;
         final CountDownLatch latch = new CountDownLatch(1);
         try
         {
            System.setOut(new PrintStream(new OutputStream()
            {

               @Override
               public void write(int b) throws IOException
               {
               }
            }));

            launcher.run(new ResultHandler<Object>()
            {
               @Override
               public void onComplete(Object result)
               {
                  holder.result = true;
                  latch.countDown();
               }

               @Override
               public void onFailure(GradleConnectionException failure)
               {
                  holder.result = false;
                  latch.countDown();
               }
            });

            latch.await();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         finally
         {
            System.setOut(originalOut);
         }
      }
      finally
      {
         if (connection != null)
         {
            connection.close();
         }
      }

      return holder.result;
   }

   @Override
   public GradleModel buildModel(String directory, String forgeScriptLocation, String forgePluginVersion)
   {
      GradleConnector connector = GradleConnector.newConnector().newConnector()
               .forProjectDirectory(new File(directory));
      ProjectConnection connection = null;
      GradleModel model = null;

      try
      {
         connection = connector.connect();

         ModelBuilder<GradleModel> gradleModelBuilder = connection.model(GradleModel.class);
         gradleModelBuilder.withArguments(
                  "-DforgePluginVersion=" + forgePluginVersion,
                  "--init-script", forgeScriptLocation);

         model = gradleModelBuilder.get();
      }
      finally
      {
         if (connection != null)
         {
            connection.close();
         }
      }

      return model;
   }
}
