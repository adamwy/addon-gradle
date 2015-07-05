/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects.model;

import org.jboss.forge.furnace.util.Streams;
import org.junit.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Adam Wyłuda
 */
public class GradleModelLoadUtilTest
{
   private static GradleModel model;

   @BeforeClass
   public static void init() throws IOException
   {
      String script = Streams.toString(GradleModelLoadUtilTest.class.getResourceAsStream("/loader/build.gradle"));
      String xmlOutput = Streams
               .toString(GradleModelLoadUtilTest.class.getResourceAsStream("/loader/forge-output.xml"));
      model = GradleModelLoadUtil.load(script, xmlOutput);
   }

   @Test
   public void testGroup()
   {
      assertEquals("groupgroup", model.getGroup());
   }

   @Test
   public void testName()
   {
      assertEquals("Gradle Test Project", model.getName());
   }

   @Test
   public void testVersion()
   {
      assertEquals("0.1-SNAPSHOT", model.getVersion());
   }

   @Test
   public void testProjectPath()
   {
      assertEquals(":", model.getProjectPath());
   }

   @Test
   public void testRootProjectPath()
   {
      // No idea for a better test
      assertTrue(model.getRootProjectPath().replace('\\', '/').endsWith("src/test/resources/loader"));
   }

   @Test
   public void testPackaging()
   {
      assertEquals("jar", model.getPackaging());
   }

   @Test
   public void testArchiveName()
   {
      assertEquals("Gradle Test Project-0.1-SNAPSHOT", model.getArchiveName());
   }

   @Test
   public void testArchivePath()
   {
      assertEquals("build/libs/Gradle Test Project-0.1-SNAPSHOT.jar", model.getArchivePath());
   }
   
   @Test
   public void testSourceAndTargetCompatibility()
   {
      assertEquals("1.6", model.getSourceCompatibility());
      assertEquals("1.7", model.getTargetCompatiblity());
   }

   @Test
   public void testEffectiveTasks()
   {
      assertTrue(model.hasEffectiveTask(GradleTaskBuilder.create().setName("zxyz")));

      boolean zxyzSet = false;
      for (GradleTask task : model.getEffectiveTasks())
      {
         if (task.getName().equals("zxyz"))
         {
            zxyzSet = true;
            List<GradleTask> dependsOnSet = task.getDependsOn();

            boolean buildSet = false, testSet = false;
            for (GradleTask dependsOn : dependsOnSet)
            {
               if (dependsOn.getName().equals("build"))
               {
                  buildSet = true;
               }
               else if (dependsOn.getName().equals("test"))
               {
                  testSet = true;
               }
            }
            assertTrue("No dependency on build", buildSet);
            assertTrue("No dependency on test", testSet);
         }
      }
      assertTrue("zxyz task not found", zxyzSet);
   }
   
   @Test
   public void testEffectiveDependencies()
   {
      assertTrue(model.hasEffectiveDependency(GradleDependencyBuilder.create()
               .setGroup("org.gradle")
               .setName("gradle-tooling-api")
               .setVersion("1.6")
               .setConfiguration(GradleDependencyConfiguration.COMPILE)
               .setClassifier("cl")
               .setPackaging("ear")));
      
      assertTrue(model.hasEffectiveDependency(GradleDependencyBuilder.create()
               .setGroup("junit")
               .setName("junit")
               .setVersion("4.11")
               .setConfiguration(GradleDependencyConfiguration.TEST_COMPILE)
               .setPackaging("pom")));
      
      assertTrue(model.hasEffectiveDependency(GradleDependencyBuilder.create()
               .setGroup("x")
               .setName("y")
               .setVersion("z")
               .setConfiguration(GradleDependencyConfiguration.TEST_RUNTIME)
               .setClassifier("clas")));

      boolean gradleToolingSet = false, junitSet = false, xSet = false;
      for (GradleDependency dep : model.getEffectiveDependencies())
      {
         if (dep.getName().equals("gradle-tooling-api"))
         {
            gradleToolingSet = true;
            assertEquals(GradleDependencyConfiguration.COMPILE, dep.getConfiguration());
            assertEquals("org.gradle", dep.getGroup());
            assertEquals("1.6", dep.getVersion());
            assertEquals("cl", dep.getClassifier());
            assertEquals("ear", dep.getPackaging());
            assertEquals(1, dep.getExcludedDependencies().size());
            GradleDependency excludedDep = dep.getExcludedDependencies().get(0);
            assertEquals("org.gradle", excludedDep.getGroup());
            assertEquals("gradle-core", excludedDep.getName());
         }
         else if (dep.getName().equals("junit"))
         {
            junitSet = true;
            assertEquals(GradleDependencyConfiguration.TEST_COMPILE, dep.getConfiguration());
            assertEquals("junit", dep.getGroup());
            assertEquals("4.11", dep.getVersion());
            assertEquals("pom", dep.getPackaging());
         }
         else if (dep.getName().equals("y"))
         {
            xSet = true;
            assertEquals(GradleDependencyConfiguration.TEST_RUNTIME, dep.getConfiguration());
            assertEquals("x", dep.getGroup());
            assertEquals("z", dep.getVersion());
            assertEquals("clas", dep.getClassifier());
         }
      }
      assertTrue("gradle-tooling-api dependency not found", gradleToolingSet);
      assertTrue("junit dependency not found", junitSet);
      assertTrue("x:y:z dependency not found", xSet);
   }

   @Test
   public void testEffectiveManagedDependencies()
   {
      assertTrue(model.hasEffectiveManagedDependency(GradleDependencyBuilder.create()
               .setGroup("com.google.guava").setName("guava").setVersion("14.0.1").setConfigurationName("compile")
               .setClassifier("forge").setPackaging("dll")));

      assertEquals(1, model.getEffectiveManagedDependencies().size());
      GradleDependency dep = model.getEffectiveManagedDependencies().get(0);
      assertEquals("com.google.guava", dep.getGroup());
      assertEquals("guava", dep.getName());
      assertEquals("14.0.1", dep.getVersion());
      assertEquals("compile", dep.getConfigurationName());
      assertEquals(GradleDependencyConfiguration.COMPILE, dep.getConfiguration());
      assertEquals("forge", dep.getClassifier());
      assertEquals("dll", dep.getPackaging());
      
      List<GradleDependency> exclusions = dep.getExcludedDependencies();
      assertEquals(1, exclusions.size());
      GradleDependency excludedDep = exclusions.get(0);
      assertEquals("org.abc", excludedDep.getGroup());
      assertEquals("xyz", excludedDep.getName());
   }

   @Test
   public void testEffectivePlugins()
   {
      assertTrue("There is no java plugin",
               model.hasEffectivePlugin(GradlePluginBuilder.create().setClazz(GradlePluginType.JAVA.getClazz())));
      assertTrue("There is no groovy plugin",
               model.hasEffectivePlugin(GradlePluginBuilder.create().setClazz(GradlePluginType.GROOVY.getClazz())));
      assertTrue("There is no eclipse plugin",
               model.hasEffectivePlugin(GradlePluginBuilder.create().setClazz(GradlePluginType.ECLIPSE.getClazz())));
   }

   @Test
   public void testEffectiveRepositories()
   {
      assertTrue(
               "There is no Gradle repository",
               model.hasEffectiveRepository(GradleRepositoryBuilder.create()
                        .setUrl("http://repo.gradle.org/gradle/libs-releases-local/")));
   }

   @Test
   public void testEffectiveSourceSets()
   {
      assertEquals("There are more or less than two source sets", 2, model.getEffectiveSourceSets().size());
      boolean mainSetSet = false, testSetSet = false;
      for (GradleSourceSet sourceSet : model.getEffectiveSourceSets())
      {
         if (sourceSet.getName().equals("main"))
         {
            mainSetSet = true;

            assertEquals(2, sourceSet.getJavaDirectories().size());
            boolean javaSet = false, alterSet = false;
            for (GradleSourceDirectory dir : sourceSet.getJavaDirectories())
            {
               if (dir.getPath().equals("src/main/java"))
               {
                  javaSet = true;
               }
               if (dir.getPath().equals("src/main/alter"))
               {
                  alterSet = true;
               }
            }
            assertTrue("Java directory set not found", javaSet);
            assertTrue("Alter directory set not found", alterSet);

            assertEquals(1, sourceSet.getResourceDirectories().size());
            assertEquals("src/main/resources", sourceSet.getResourceDirectories().get(0).getPath());
         }
         if (sourceSet.getName().equals("test"))
         {
            testSetSet = true;

            assertEquals(1, sourceSet.getJavaDirectories().size());
            assertEquals("src/test/java", sourceSet.getJavaDirectories().get(0).getPath());

            assertEquals(1, sourceSet.getResourceDirectories().size());
            assertEquals("src/test/resources", sourceSet.getResourceDirectories().get(0).getPath());
         }
      }
      assertTrue("main source set not found", mainSetSet);
      assertTrue("test source set not found", testSetSet);
   }

   @Test
   public void testEffectiveProperties()
   {
      Map<String, String> props = model.getEffectiveProperties();

      assertEquals("testMe", props.get("someProperty"));
      assertEquals("xyz", props.get("otherProperty"));
   }
}
