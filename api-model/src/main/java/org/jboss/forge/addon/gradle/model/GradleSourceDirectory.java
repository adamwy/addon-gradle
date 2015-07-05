/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.gradle.model;

/**
 * Represents Gradle source directory.
 * 
 * @see GradleSourceSet
 * @see GradleModel
 * 
 * @author Adam Wyłuda
 */
public interface GradleSourceDirectory
{
   /**
    * Returns path of the source directory.
    */
   String getPath();
}
