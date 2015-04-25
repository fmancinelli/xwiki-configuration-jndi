/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.xwiki.contrib.configuration.jndi.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.properties.ConverterManager;

/**
 * This configuration source gets its property values from JNDI. By default the JNDI context is
 * 'java:comp/env/xwiki/config'. It can be changed by using the environment variable 'xwikiJNDIConfigContext'.
 *
 * @version $Id$
 */
@Component
@Named("jndi")
@Singleton
public class JNDIConfigurationSource implements ConfigurationSource, Initializable
{
    private static final String JNDI_CONFIG_CONTEXT_PROPERTY = "xwikiJNDIConfigContext";

    private static final String DEFAULT_JNDI_CONFIG_CONTEXT = "java:comp/env/xwiki/config";

    @Inject
    private Logger logger;

    @Inject
    private ConverterManager converter;

    private Context xwikiConfigContext;

    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        if (xwikiConfigContext == null) {
            return null;
        }

        try {
            Object jndiValue = xwikiConfigContext.lookup(key);
            if (!(jndiValue instanceof java.lang.String)) {
                return null;
            }

            T value = convert((String) jndiValue, (Class<T>) defaultValue.getClass(), defaultValue);

            return value;
        } catch (NamingException e) {
            return defaultValue;
        }
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        if (xwikiConfigContext == null) {
            return null;
        }

        try {
            Object jndiValue = xwikiConfigContext.lookup(key);
            if (!(jndiValue instanceof java.lang.String)) {
                return null;
            }

            T value = convert((String) jndiValue, valueClass, null);

            return value;
        } catch (NamingException e) {
            return null;
        }
    }

    @Override
    public <T> T getProperty(String key)
    {
        if (xwikiConfigContext == null) {
            return null;
        }

        try {
            Object jndiValue = xwikiConfigContext.lookup(key);
            if (!(jndiValue instanceof java.lang.String)) {
                return null;
            }

            T value = (T) StringUtils.trim((String) jndiValue);

            return value;
        } catch (NamingException e) {
            return null;
        }
    }

    @Override
    public List<String> getKeys()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean containsKey(String key)
    {
        try {
            xwikiConfigContext.lookup(key);
            return true;
        } catch (NamingException e) {
            return false;
        }
    }

    @Override
    public boolean isEmpty()
    {
        if (xwikiConfigContext == null) {
            return true;
        }

        return false;
    }

    @Override
    public void initialize() throws InitializationException
    {
        try {
            String contextString = System.getenv(JNDI_CONFIG_CONTEXT_PROPERTY);
            if (contextString == null) {
                contextString = DEFAULT_JNDI_CONFIG_CONTEXT;
            }

            this.xwikiConfigContext = (Context) new InitialContext().lookup(contextString);

            logger.info(String.format("Initial JNDI context initialized with context %s.", contextString));
        } catch (NamingException e) {
            logger.warn(
                    String.format("Unable to get initial JNDI context %s. This configuration source will be empty", e));
        }
    }

    private <T> T convert(String value, Class<T> targetClass, T defaultValue)
    {
        try {
            if (targetClass == String[].class) {
                // Retro compatibility from old XWikiConfig class
                return (T) StringUtils.split(value, " ,");
            } else {
                return this.converter.convert(targetClass, value);
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
