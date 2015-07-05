/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link GradleSourceDirectory}.
 * 
 * @see GradleModel
 * 
 * @author Adam Wyłuda
 */
public class GradleSourceDirectoryBuilder implements GradleSourceDirectory, Serializable
{
   private String path = "";
   
   private GradleSourceDirectoryBuilder()
   {
   }
   
   public static GradleSourceDirectoryBuilder create()
   {
      return new GradleSourceDirectoryBuilder();
   }
   
   /**
    * Creates a copy of given source directory.
    */
   public static GradleSourceDirectoryBuilder create(GradleSourceDirectory sourceDirectory)
   {
      GradleSourceDirectoryBuilder builder = new GradleSourceDirectoryBuilder();
      
      builder.path = sourceDirectory.getPath();
      
      return builder;
   }
   
   /**
    * Performs deep copy of given source directories.
    */
   public static List<GradleSourceDirectory> deepCopy(List<GradleSourceDirectory> sourceDirs)
   {
      List<GradleSourceDirectory> list = new ArrayList<GradleSourceDirectory>();
      
      for (GradleSourceDirectory set : sourceDirs)
      {
         list.add(create(set));
      }
      
      return list;
   }

   @Override
   public String getPath()
   {
      return path;
   }
   
   public GradleSourceDirectoryBuilder setPath(String path)
   {
      this.path = path;
      return this;
   }
   
   @Override
   public String toString()
   {
      return String.format("srcDir '%s'", path);
   }
}
