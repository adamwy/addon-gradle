/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.projects.model;

import org.gradle.jarjar.com.google.common.collect.Lists;
import org.gradle.jarjar.com.google.common.collect.Maps;
import org.jboss.forge.addon.gradle.model.GradleDependency;
import org.jboss.forge.addon.gradle.model.GradleDependencyBuilder;
import org.jboss.forge.addon.gradle.model.GradleModel;
import org.jboss.forge.addon.gradle.model.GradleModelBuilder;
import org.jboss.forge.addon.gradle.model.GradlePlugin;
import org.jboss.forge.addon.gradle.model.GradlePluginBuilder;
import org.jboss.forge.addon.gradle.model.GradleRepository;
import org.jboss.forge.addon.gradle.model.GradleRepositoryBuilder;
import org.jboss.forge.addon.gradle.model.GradleSourceDirectory;
import org.jboss.forge.addon.gradle.model.GradleSourceDirectoryBuilder;
import org.jboss.forge.addon.gradle.model.GradleSourceSet;
import org.jboss.forge.addon.gradle.model.GradleSourceSetBuilder;
import org.jboss.forge.addon.gradle.model.GradleTask;
import org.jboss.forge.addon.gradle.model.GradleTaskBuilder;
import org.jboss.forge.addon.gradle.parser.GradleSourceUtil;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Wyłuda
 */
public class GradleModelLoadUtil
{
   private GradleModelLoadUtil()
   {
   }

   /**
    * Loads only direct model from given script.
    */
   public static GradleModel load(String script)
   {
      GradleModelBuilder modelBuilder = GradleModelBuilder.create();
      loadDirectModel(modelBuilder, script);
      return modelBuilder;
   }

   /**
    * Loads both direct and effective model from given scripts and Gradle xml output.
    */
   public static GradleModel load(String script, String xmlOutput)
   {
      Node root = XMLParser.parse(xmlOutput);

      GradleModelBuilder modelBuilder = GradleModelBuilder.create();
      loadEffectiveModel(modelBuilder, root.getSingle("project"));
      loadDirectModel(modelBuilder, script);

      return modelBuilder;
   }

   private static void loadDirectModel(GradleModelBuilder builder, String script)
   {
      builder.setDependencies(depsFromScript(script));
      builder.setManagedDependencies(managedDepsFromScript(script));
      builder.setPlugins(pluginsFromScript(script));
      builder.setRepositories(reposFromScript(script));
      builder.setProperties(propertiesFromScript(script));
   }

   private static List<GradleDependency> depsFromScript(String script)
   {
      List<GradleDependency> deps = Lists.newArrayList();
      deps.addAll(GradleSourceUtil.getDependencies(script));
      deps.addAll(GradleSourceUtil.getDirectDependencies(script));
      return deps;
   }

   private static List<GradleDependency> managedDepsFromScript(String script)
   {
      return GradleSourceUtil.getManagedDependencies(script);
   }

   private static List<GradlePlugin> pluginsFromScript(String script)
   {
      return GradleSourceUtil.getPlugins(script);
   }

   private static List<GradleRepository> reposFromScript(String script)
   {
      return GradleSourceUtil.getRepositories(script);
   }

   private static Map<String, String> propertiesFromScript(String script)
   {
      return GradleSourceUtil.getDirectProperties(script);
   }

   private static void loadEffectiveModel(GradleModelBuilder builder,
            Node projectNode)
   {
      builder.setGroup(groupFromNode(projectNode));
      builder.setName(nameFromNode(projectNode));
      builder.setVersion(versionFromNode(projectNode));
      builder.setPackaging(packagingFromNode(projectNode));
      builder.setArchivePath(archivePathFromNode(projectNode));
      builder.setArchiveName(archiveNameFromPath(builder.getArchivePath()));
      builder.setSourceCompatibility(sourceCompatibilityFromNode(projectNode));
      builder.setTargetCompatibility(targetCompatibilityFromNode(projectNode));
      builder.setProjectPath(projectPathFromNode(projectNode));
      builder.setRootProjectPath(rootProjectPathFromNode(projectNode));
      builder.setEffectiveTasks(tasksFromNode(projectNode));
      builder.setEffectiveDependencies(depsFromNode(projectNode));
      builder.setEffectiveManagedDependencies(managedDepsFromNode(projectNode));
      builder.setEffectivePlugins(pluginsFromNode(projectNode));
      builder.setEffectiveRepositories(reposFromNode(projectNode));
      builder.setEffectiveSourceSets(sourceSetsFromNode(projectNode));
      builder.setEffectiveProperties(propertiesFromNode(projectNode));
   }

   private static String groupFromNode(Node projectNode)
   {
      return projectNode.getSingle("group").getText().trim();
   }

   private static String nameFromNode(Node projectNode)
   {
      return projectNode.getSingle("name").getText().trim();
   }

   private static String versionFromNode(Node projectNode)
   {
      return projectNode.getSingle("version").getText().trim();
   }

   private static String projectPathFromNode(Node projectNode)
   {
      return projectNode.getSingle("projectPath").getText().trim();
   }

   private static String rootProjectPathFromNode(Node projectNode)
   {
      return projectNode.getSingle("rootProjectDirectory").getText().trim();
   }

   private static String packagingFromNode(Node projectNode)
   {
      return projectNode.getSingle("packaging").getText().trim();
   }

   private static String archivePathFromNode(Node projectNode)
   {
      return projectNode.getSingle("archivePath").getText().trim();
   }

   private static String archiveNameFromPath(String archivePath)
   {
      if (!Strings.isNullOrEmpty(archivePath))
      {
         return archivePath.substring(archivePath.lastIndexOf("/") + 1, archivePath.lastIndexOf("."));
      }
      else
      {
         return "";
      }
   }

   private static String sourceCompatibilityFromNode(Node projectNode)
   {
      return projectNode.getSingle("sourceCompatibility").getText().trim();
   }

   private static String targetCompatibilityFromNode(Node projectNode)
   {
      return projectNode.getSingle("targetCompatibility").getText().trim();
   }

   private static List<GradleTask> tasksFromNode(Node projectNode)
   {
      List<GradleTask> tasks = new ArrayList<>();
      Map<GradleTask, List<String>> taskDepsMap = new HashMap<>();
      Map<String, GradleTask> taskByNameMap = new HashMap<>();

      for (Node taskNode : projectNode.getSingle("tasks").get("task"))
      {
         String name = taskNode.getSingle("name").getText().trim();
         List<String> taskDeps = new ArrayList<>();
         for (Node dependsOnNode : taskNode.getSingle("dependsOn").get("task"))
         {
            String text = dependsOnNode.getText().trim();
            taskDeps.add(text);
         }
         GradleTask task = GradleTaskBuilder.create().setName(name);
         tasks.add(task);
         taskDepsMap.put(task, taskDeps);
         taskByNameMap.put(task.getName(), task);
      }

      // We couldn't get full set of dependencies so we will complete it now
      for (GradleTask task : tasks)
      {
         List<String> deps = taskDepsMap.get(task);
         for (String depName : deps)
         {
            task.getDependsOn().add(taskByNameMap.get(depName));
         }
      }

      return tasks;
   }

   private static List<GradleDependency> depsFromNode(Node projectNode)
   {
      // Gradle string -> Best dependency
      // (one which has the biggest priority, determined by overrides relationship)
      Map<String, GradleDependency> depByString = new HashMap<>();

      for (Node depNode : projectNode.getSingle("dependencies").get("dependency"))
      {
         GradleDependency gradleDep = depFromNode(depNode);
         String gradleString = gradleDep.toGradleString();
         if (!depByString.containsKey(gradleString))
         {
            depByString.put(gradleString, gradleDep);
         }
         else
         {
            GradleDependency olderDep = depByString.get(gradleString);
            if (gradleDep.getConfiguration().overrides(olderDep.getConfiguration()))
            {
               depByString.put(gradleString, gradleDep);
            }
         }
      }

      List<GradleDependency> deps = new ArrayList<>();
      deps.addAll(depByString.values());
      return deps;
   }

   private static List<GradleDependency> managedDepsFromNode(Node projectNode)
   {
      List<GradleDependency> deps = new ArrayList<>();
      for (Node depNode : projectNode.getSingle("managedDependencies").get("dependency"))
      {
         deps.add(depFromNode(depNode));
      }
      return deps;
   }

   private static GradleDependency depFromNode(Node depNode)
   {
      String group = depNode.getSingle("group").getText().trim();
      String name = depNode.getSingle("name").getText().trim();
      String version = depNode.getSingle("version").getText().trim();
      String config = depNode.getSingle("configuration").getText().trim();

      GradleDependencyBuilder depBuilder = GradleDependencyBuilder.create()
               .setGroup(group)
               .setName(name)
               .setVersion(version)
               .setConfigurationName(config);

      depBuilder = loadClassifierAndPackagingFromNode(depBuilder, depNode);
      depBuilder = loadExcludedDependenciesFromNode(depBuilder, depNode);

      return depBuilder;
   }

   private static GradleDependencyBuilder loadClassifierAndPackagingFromNode(
            GradleDependencyBuilder depBuilder,
            Node depNode)
   {
      Node artifactsNode = depNode.getSingle("artifacts");
      Node artifactNode = artifactsNode != null ? artifactsNode.getSingle("artifact") : null;

      if (artifactNode != null)
      {
         String classifier = artifactNode.getSingle("classifier").getText().trim();
         String type = artifactNode.getSingle("type").getText().trim();

         if (!Strings.isNullOrEmpty(classifier))
         {
            depBuilder = depBuilder.setClassifier(classifier);
         }

         if (!Strings.isNullOrEmpty(type))
         {
            depBuilder = depBuilder.setPackaging(type);
         }
      }

      return depBuilder;
   }

   private static GradleDependencyBuilder loadExcludedDependenciesFromNode(
            GradleDependencyBuilder depBuilder,
            Node depNode)
   {
      Node excludeRulesNode = depNode.getSingle("excludeRules");

      if (excludeRulesNode == null)
      {
         return depBuilder;
      }

      List<GradleDependency> excludedDependencies = Lists.newArrayList();
      for (Node excludeRuleNode : excludeRulesNode.get("excludeRule"))
      {
         String group = excludeRuleNode.getSingle("group").getText().trim();
         String module = excludeRuleNode.getSingle("module").getText().trim();
         excludedDependencies.add(GradleDependencyBuilder.create()
                  .setGroup(group).setName(module));
      }
      depBuilder.setExcludedDependencies(excludedDependencies);

      return depBuilder;
   }

   private static List<GradlePlugin> pluginsFromNode(Node projectNode)
   {
      List<GradlePlugin> plugins = new ArrayList<>();
      for (Node pluginNode : projectNode.getSingle("plugins").get("plugin"))
      {
         plugins.add(pluginFromNode(pluginNode));
      }
      return plugins;
   }

   private static GradlePlugin pluginFromNode(Node pluginNode)
   {
      String clazz = pluginNode.getSingle("class").getText().trim();
      return GradlePluginBuilder.create()
               .setClazz(clazz);
   }

   private static List<GradleRepository> reposFromNode(Node projectNode)
   {
      List<GradleRepository> repos = new ArrayList<>();
      for (Node repoNode : projectNode.getSingle("repositories").get("repository"))
      {
         String name = repoNode.getSingle("name").getText().trim();
         String url = repoNode.getSingle("url").getText().trim();
         repos.add(GradleRepositoryBuilder.create()
                  .setName(name)
                  .setUrl(url));
      }
      return repos;
   }

   private static List<GradleSourceSet> sourceSetsFromNode(Node projectNode)
   {
      List<GradleSourceSet> sourceSets = new ArrayList<>();
      for (Node sourceSetNode : projectNode.getSingle("sourceSets").get("sourceSet"))
      {
         sourceSets.add(sourceSetFromNode(sourceSetNode));
      }
      return sourceSets;
   }

   private static GradleSourceSet sourceSetFromNode(Node sourceSetNode)
   {
      String name = sourceSetNode.getSingle("name").getText().trim();
      List<GradleSourceDirectory> javaSourceDirs = new ArrayList<>();
      for (Node directoryNode : sourceSetNode.getSingle("java").get("directory"))
      {
         javaSourceDirs.add(sourceDirectoryFromNode(directoryNode));
      }
      List<GradleSourceDirectory> resourceSourceDirs = new ArrayList<>();
      for (Node directoryNode : sourceSetNode.getSingle("resources").get("directory"))
      {
         resourceSourceDirs.add(sourceDirectoryFromNode(directoryNode));
      }
      return GradleSourceSetBuilder.create()
               .setName(name)
               .setJavaDirectories(javaSourceDirs)
               .setResourceDirectories(resourceSourceDirs);
   }

   private static GradleSourceDirectory sourceDirectoryFromNode(Node directoryNode)
   {
      String path = directoryNode.getText().trim();
      return GradleSourceDirectoryBuilder.create()
               .setPath(path);
   }

   private static Map<String, String> propertiesFromNode(Node projectNode)
   {
      Map<String, String> properties = Maps.newHashMap();
      for (Node propertyNode : projectNode.getSingle("properties").get("property"))
      {
         String key = propertyNode.getSingle("key").getText().trim();
         String value = propertyNode.getSingle("value").getText().trim();
         properties.put(key, value);
      }
      return properties;
   }
}
