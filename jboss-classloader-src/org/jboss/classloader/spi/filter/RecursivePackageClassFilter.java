/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.classloader.spi.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.classloader.plugins.filter.PatternClassFilter;

/**
 * A class filter using regular expressions that includes subpackages
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class RecursivePackageClassFilter extends PatternClassFilter
{
   private static final String[] EVERYTHING_PATTERN = { ".*" };
   
   /** The patterns as regular expressions */
   private String[] packageNames;
   
   /**
    * Convert package names to class patterns
    * 
    * @param packageNames the package names
    * @return the patterns
    */
   private static String[] convertPackageNamesToClassPatterns(String[] packageNames)
   {
      if (packageNames == null)
         throw new IllegalArgumentException("Null package names");
      
      String[] patterns = new String[packageNames.length];
      for (int i = 0; i < packageNames.length; ++i)
      {
         if (packageNames[i] == null)
            throw new IllegalArgumentException("Null package name in " + Arrays.asList(packageNames));

         if (packageNames[i].length() == 0)
            // Base package - match everything
            return EVERYTHING_PATTERN;
         else
            // Escape the dots in the package and match anything that starts with a dot
            patterns[i] = packageNames[i].replace(".", "\\.") + "\\..+";
      }
      return patterns;
   }
   
   /**
    * Convert package names to resource patterns
    * 
    * @param packageNames the package names
    * @return the patterns
    */
   private static String[] convertPackageNamesToResourcePatterns(String[] packageNames)
   {
      if (packageNames == null)
         throw new IllegalArgumentException("Null package names");
      
      String[] patterns = new String[packageNames.length];
      for (int i = 0; i < packageNames.length; ++i)
      {
         if (packageNames[i] == null)
            throw new IllegalArgumentException("Null package name in " + Arrays.asList(packageNames));

         if (packageNames[i].length() == 0)
            // Base package - match everything
            return EVERYTHING_PATTERN;
         else
            // Replace the dots with slashs and match anything that begins with a slash
            patterns[i] = packageNames[i].replace(".", "/") + "/.+";
      }
      return patterns;
   }
   
   /**
    * Convert package names to package patterns
    * 
    * @param packageNames the package names
    * @return the patterns
    */
   private static String[] convertPackageNamesToPackagePatterns(String[] packageNames)
   {
      if (packageNames == null)
         throw new IllegalArgumentException("Null package names");
      
      String[] patterns = new String[2 * packageNames.length];
      int j = 0;
      for (int i = 0; i < packageNames.length; ++i)
      {
         if (packageNames[i] == null)
            throw new IllegalArgumentException("Null package name in " + Arrays.asList(packageNames));

         if (packageNames[i].length() == 0)
         {
            // Base package - match everything
            return EVERYTHING_PATTERN;
         }
         else
         {
            // Use the package name
            patterns[j++] = packageNames[i];
            // And anything that begins with a dot
            patterns[j++] = packageNames[i] + "\\..*";
         }
      }
      return patterns;
   }

   /**
    * Create a recursive package class filter<p>
    * 
    * Creates the filter from a comma seperated list
    * 
    * @param string the string
    * @return the filter
    */
   public static RecursivePackageClassFilter createRecursivePackageClassFilterFromString(String string)
   {
      StringTokenizer tokenizer = new StringTokenizer(string, ",");
      List<String> packages = new ArrayList<String>();
      while (tokenizer.hasMoreTokens())
         packages.add(tokenizer.nextToken());
      return createRecursivePackageClassFilter(packages);
   }
   
   /**
    * Create a new recursive package class filter
    * 
    * @param packageNames the package names
    * @return the filter
    * @throws IllegalArgumentException for null packageNames
    */
   public static RecursivePackageClassFilter createRecursivePackageClassFilter(String... packageNames)
   {
      return new RecursivePackageClassFilter(packageNames);
   }
   
   /**
    * Create a new recursive package class filter
    * 
    * @param packageNames the package names
    * @return the filter
    * @throws IllegalArgumentException for null packageNames
    */
   public static RecursivePackageClassFilter createRecursivePackageClassFilter(List<String> packageNames)
   {
      String[] packages;
      if (packageNames == null)
         packages = new String[0];
      else
         packages = packageNames.toArray(new String[packageNames.size()]);
      return new RecursivePackageClassFilter(packages);
   }
   
   /**
    * Create a new RecursivePackageClassFilter.
    * 
    * @param packageNames the packageNames
    * @throws IllegalArgumentException for null packageNames
    */
   public RecursivePackageClassFilter(String[] packageNames)
   {
      super(convertPackageNamesToClassPatterns(packageNames), 
            convertPackageNamesToResourcePatterns(packageNames),
            convertPackageNamesToPackagePatterns(packageNames));
      this.packageNames = packageNames;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(Arrays.asList(packageNames));
      if (isIncludeJava())
         builder.append(" <INCLUDE_JAVA>");
      return builder.toString();
   }
}
