/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.injection;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;

import org.hibernate.Session;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.entity.ExtendedEntityManager;
import org.jboss.ejb3.entity.hibernate.ExtendedSessionInvocationHandler;
import org.jboss.ejb3.entity.hibernate.TransactionScopedSessionInvocationHandler;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.jpa.deployment.ManagedEntityManagerFactory;
import org.jboss.jpa.tx.TransactionScopedEntityManager;
import org.jboss.util.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 79976 $
 */
public class PcEncInjector implements EncInjector
{
   private static final Class[] SESS_PROXY_INTERFACES = new Class[] {
         org.hibernate.classic.Session.class,
         org.hibernate.engine.SessionImplementor.class,
         org.hibernate.jdbc.JDBCContext.Context.class,
         org.hibernate.event.EventSource.class
   };

   private String encName;
   private String unitName;
   private PersistenceContextType type;
   private Class injectionType;
   private String error;

   public PcEncInjector(String encName, String unitName, PersistenceContextType type, Class injectionType, String error)
   {
      this.encName = encName;
      this.unitName = unitName;
      this.type = type;
      this.injectionType = injectionType;
      this.error = error;
   }

   public void inject(InjectionContainer container)
   {
      String error1 = error;
      ManagedEntityManagerFactory factory = null;
      try
      {
         factory = PersistenceUnitHandler.getManagedEntityManagerFactory(
                 container, unitName);
      }
      catch (NameNotFoundException e)
      {
         error1 += " " + e.getMessage();
      }
      if (factory == null)
      {
         throw new RuntimeException(error1);
      }
      if (type == PersistenceContextType.EXTENDED)
      {
         if (!(container instanceof StatefulContainer))
            throw new RuntimeException("It is illegal to inject an EXTENDED PC into something other than a SFSB");
         container.getInjectors().add(0, new ExtendedPersistenceContextInjector(factory));
         Object extendedPc;
         if (injectionType == null
                 || injectionType.getName().equals(EntityManager.class.getName()))
         {
            extendedPc = new ExtendedEntityManager(factory.getKernelName());
         }
         else
         {
            ExtendedSessionInvocationHandler handler = new ExtendedSessionInvocationHandler(factory.getKernelName());
            extendedPc = Proxy.newProxyInstance(
                  Session.class.getClassLoader(), //use the Hibernate classloader so the proxy has the same scope as Hibernate
                  SESS_PROXY_INTERFACES,
                  handler
            );
         }
         try
         {
            Util.rebind(container.getEnc(), encName, extendedPc);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(error1, e);
         }
      }
      else
      {
         Object entityManager;
         if (injectionType == null
                 || injectionType.getName().equals(EntityManager.class.getName()))
         {
            entityManager = new TransactionScopedEntityManager(factory);
         }
         else
         {
            TransactionScopedSessionInvocationHandler handler = new TransactionScopedSessionInvocationHandler(factory);
            entityManager = Proxy.newProxyInstance(
                  Session.class.getClassLoader(), //use the Hibernate classloader so the proxy has the same scope as Hibernate
                  SESS_PROXY_INTERFACES,
                  handler
            );
         }
         try
         {
            Util.rebind(container.getEnc(), encName, entityManager);
            
         // *******************************************************************//
          //   add the code about JNDI put injection (by lhc 2012.12.13)
            
            String routeName = ((EJBContainer)container).getDeploymentUnit().getUrl().toString();
            String unitName = factory.getKernelName();
            
            if (DeployersImpl.jndiProperties == null){
            	DeployersImpl.jndiProperties = new Hashtable<String, String>();
            	DeployersImpl.jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
            	DeployersImpl.jndiProperties.put(Context.PROVIDER_URL,"jnp://localhost");
            	DeployersImpl.context = new InitialContext(DeployersImpl.jndiProperties);
            }
           
            try{
            	HashMap<String, String> hm = (HashMap<String, String>) DeployersImpl.context.lookup("AEJBConUrlUnitNameMap");
            	hm.put(routeName, unitName);
            	DeployersImpl.context.rebind("AEJBConUrlUnitNameMap", hm);
            	}
            catch(Exception e){
            	HashMap<String, String> aejbConMap = new HashMap<String, String>();
                aejbConMap.put(routeName, unitName);
                DeployersImpl.context.bind("AEJBConUrlUnitNameMap", aejbConMap);
            }
            
            // ************************************************************
                
         }
         catch (NamingException e)
         {
            throw new RuntimeException(error1, e);
         }
      }
   }
}
