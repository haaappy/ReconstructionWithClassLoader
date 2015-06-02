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
package org.jboss.system.server.profileservice.persistence.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * The SimpleValue/SimpleMetaType.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87425 $
 */
public class PersistedSimpleValue implements PersistedValue, PersistedElement
{
   /** The name. */
   private String name;
   
   /** The class name. */
   private String className;
   
   /** The value. */
   private String value;
   
   public PersistedSimpleValue()
   {
      //
   }
   
   public PersistedSimpleValue(String name)
   {
      this(name, null);
   }
   
   public PersistedSimpleValue(String name, String className)
   {
      this.name = name;
      this.className = className;
   }

   @XmlAttribute(name = "name")
   public String getName()
   {
      return this.name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlAttribute(name = "class-name")
   public String getClassName()
   {
      return this.className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }
   
   @XmlValue 
   public String getValue()
   {
      return value;
   }
   
   public void setValue(String value)
   {
      this.value = value;
   }
   
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(getClass().getSimpleName());
      builder.append('@').append(Integer.toHexString(System.identityHashCode(this)));
      builder.append("{name = ").append(getName());
      toString(builder);
      builder.append("}");
      return builder.toString();
   }
   
   protected void toString(StringBuilder builder)
   {
      builder.append(", value = ").append(getValue());
   }

   public void visit(PersistedValueVisitor visitor)
   {
      // nothing here
   }
   
}
