/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects.model;

import static org.junit.Assert.*;

import org.jboss.forge.addon.gradle.model.GradleDependency;
import org.jboss.forge.addon.gradle.model.GradleDependencyBuilder;
import org.jboss.forge.addon.gradle.model.GradleDependencyConfiguration;
import org.junit.Test;

/**
 * @author Adam Wyłuda
 */
public class GradleDependencyBuilderTest
{
   @Test
   public void testCreateFromString()
   {
      GradleDependency dep = GradleDependencyBuilder
               .create("compile", "group:name:version");

      assertEquals("compile", dep.getConfigurationName());
      assertEquals("group", dep.getGroup());
      assertEquals("name", dep.getName());
      assertEquals("version", dep.getVersion());
      assertEquals("", dep.getClassifier());
      assertEquals("jar", dep.getPackaging());
   }

   @Test
   public void testCreateFromStringWithClassifier()
   {
      GradleDependency dep = GradleDependencyBuilder
               .create("compile", "group:name:version:classifier");

      assertEquals("compile", dep.getConfigurationName());
      assertEquals("group", dep.getGroup());
      assertEquals("name", dep.getName());
      assertEquals("version", dep.getVersion());
      assertEquals("classifier", dep.getClassifier());
      assertEquals("jar", dep.getPackaging());
   }

   @Test
   public void testCreateFromStringWithPackaging()
   {
      GradleDependency dep = GradleDependencyBuilder
               .create("compile", "group:name:version@packaging");

      assertEquals("compile", dep.getConfigurationName());
      assertEquals("group", dep.getGroup());
      assertEquals("name", dep.getName());
      assertEquals("version", dep.getVersion());
      assertEquals("", dep.getClassifier());
      assertEquals("packaging", dep.getPackaging());
   }

   @Test
   public void testCreateFromStringWithClassifierAndPackaging()
   {
      GradleDependency dep = GradleDependencyBuilder
               .create("compile", "group:name:version:classifier@packaging");

      assertEquals("compile", dep.getConfigurationName());
      assertEquals("group", dep.getGroup());
      assertEquals("name", dep.getName());
      assertEquals("version", dep.getVersion());
      assertEquals("classifier", dep.getClassifier());
      assertEquals("packaging", dep.getPackaging());
   }

   @Test
   public void testToGradleString()
   {
      assertEquals("x:y:z", GradleDependencyBuilder.create()
               .setGroup("x").setName("y").setVersion("z").toGradleString());
   }

   @Test
   public void testToGradleStringWithClassifier()
   {
      assertEquals("x:y:z:a", GradleDependencyBuilder.create()
               .setGroup("x").setName("y").setVersion("z").setClassifier("a").toGradleString());
   }

   @Test
   public void testToGradleStringWithPackaging()
   {
      assertEquals("x:y:z@pom", GradleDependencyBuilder.create()
               .setGroup("x").setName("y").setVersion("z").setPackaging("pom").toGradleString());
   }

   @Test
   public void testToGradleStringWithClassifierAndPackaging()
   {
      assertEquals("x:y:z:a@pom", GradleDependencyBuilder.create()
               .setGroup("x").setName("y").setVersion("z").setClassifier("a").setPackaging("pom").toGradleString());
   }

   @Test
   public void testConfigurationNameFromEnum()
   {
      GradleDependency dep = GradleDependencyBuilder.create()
               .setConfiguration(GradleDependencyConfiguration.RUNTIME);

      assertEquals(GradleDependencyConfiguration.RUNTIME.getName(), dep.getConfigurationName());
   }

   @Test
   public void testConfigurationEnumFromName()
   {
      GradleDependency dep = GradleDependencyBuilder.create()
               .setConfigurationName(GradleDependencyConfiguration.RUNTIME.getName());

      assertEquals(GradleDependencyConfiguration.RUNTIME, dep.getConfiguration());
   }
}
