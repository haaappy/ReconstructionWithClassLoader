/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.classloader.spi.translator;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.ListIterator;

import org.jboss.logging.Logger;
import org.jboss.util.loading.Translator;

/**
 * The translator utils/helper.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TranslatorUtils
{
   /** The log */
   private static final Logger log = Logger.getLogger(TranslatorUtils.class);

   /**
    * Apply transformers on transform.
    *
    * @param translators the translators
    * @param classLoader the classloader
    * @param className the class name
    * @param byteCode the byte code
    * @param protectionDomain the protection domain
    * @return trabsformed bytes
    * @throws Exception for any error
    */
   public static byte[] applyTranslatorsOnTransform(List<Translator> translators, ClassLoader classLoader, String className, byte[] byteCode, ProtectionDomain protectionDomain) throws Exception
   {
      if (translators == null || translators.isEmpty())
         return byteCode;

      boolean trace = log.isTraceEnabled();

      byte[] result = byteCode;
      for (Translator translator : translators)
      {
         // sanity check
         if (translator == null)
         {
            if (trace)
               log.trace("Null translator, classLoader: " + classLoader + ", className: " + className);

            continue;
         }

         result = translator.transform(classLoader, className, null, protectionDomain, result);
      }
      return result;
   }

   /**
    * Apply translators on classloader unregister / policy shutdown.
    *
    * @param translators the translators
    * @param classLoader the policy's classloader
    */
   public static void applyTranslatorsAtUnregister(List<Translator> translators, ClassLoader classLoader)
   {
      if (translators != null && translators.isEmpty() == false)
      {
         boolean trace = log.isTraceEnabled();

         // go in reverse order
         ListIterator<Translator> iter = translators.listIterator(translators.size() - 1);
         while(iter.hasPrevious())
         {
            Translator translator = iter.previous();
            // sanity check
            if (translator == null)
            {
               if (trace)
                  log.trace("Null translator, classLoader: " + classLoader);

               continue;
            }

            try
            {
               translator.unregisterClassLoader(classLoader);
            }
            catch (Exception e)
            {
               log.warn("Exception for translator " + translator + " on CL unregister: " + e);
            }
         }
      }
   }
}
