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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;

import com.xpn.xwiki.internal.XWikiCfgConfigurationSource;

/**
 * This configurations source wraps the {@link XWikiCfgConfigurationSource} and allows to override settings in the
 * xwiki.cfg file using JNDI. If a property is found in the JNDI context, then its value is used, otherwise the value
 * from the xwiki.cfg is used.
 *
 * @version $Id$
 */
@Component
@Named(XWikiCfgConfigurationSource.ROLEHINT)
@Singleton
public class XWikiCfgJNDIConfigurationSource extends XWikiCfgConfigurationSource implements Initializable
{
    /* Use a different name from the private one in the superclass, otherwise component manager will complain. */
    @Inject
    private Logger logger2;

    @Inject
    @Named("jndi")
    private ConfigurationSource jndiConfigurationSource;

    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        /**
         * Don't use the corresponding {@link ConfigurationSource#getProperty(String, Object)} method which will return
         * a default value if the property is not found. We need to know when the property is null in order to delegate
         * to the original configuration source.
         */
        T jndiValue = jndiConfigurationSource.getProperty(key);
        if (jndiValue != null) {
            logOverride(key, jndiValue);
            return jndiValue;
        }

        return super.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        T jndiValue = jndiConfigurationSource.getProperty(key, valueClass);
        if (jndiValue != null) {
            logOverride(key, jndiValue);
            return jndiValue;
        }

        return super.getProperty(key, valueClass);
    }

    @Override
    public <T> T getProperty(String key)
    {
        T jndiValue = jndiConfigurationSource.getProperty(key);
        if (jndiValue != null) {
            logOverride(key, jndiValue);
            return jndiValue;
        }

        return super.getProperty(key);
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();
        logger2.info("xwiki.cfg JNDI override installed");
    }

    @Override
    public boolean containsKey(String key)
    {
        boolean isKeyInJNDI = jndiConfigurationSource.containsKey(key);

        if (isKeyInJNDI) {
            return true;
        }

        return super.containsKey(key);
    }

    private void logOverride(String key, Object value) {
        logger2.info(String.format("Property %s found in JNDI. Overriding with value: %s", key, value));
    }
}
