/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.capedwarf.services;

import org.hibernate.search.cfg.EntityMapping;
import org.hibernate.search.cfg.Environment;
import org.hibernate.search.cfg.SearchMapping;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IndexingConfigurationBuilder;
import org.infinispan.configuration.cache.StoreAsBinaryConfigurationBuilder;

/**
 * Indexable configuration callback.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class IndexableConfigurationCallback extends AbstractConfigurationCallback {
    protected final CacheConfig config;
    protected final String appId;
    protected final ClassLoader classLoader;

    protected IndexableConfigurationCallback(CacheConfig config, String appId, ClassLoader classLoader) {
        this.config = config;
        this.appId = appId;
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    protected SearchMapping applyIndexing(ConfigurationBuilder builder) {
        final CacheIndexing ci = config.getIndexing();
        if (ci == null)
            throw new IllegalArgumentException("Missing cache indexing info: " + config);

        final IndexingConfigurationBuilder indexing = builder.indexing();
        indexing.addProperty("hibernate.search.default.indexBase", "./indexes_" + appId);
        final SearchMapping mapping = new SearchMapping();
        for (Class<?> clazz : ci.getClasses(classLoader)) {
            final EntityMapping entity = mapping.entity(clazz);
            entity.indexed().indexName(getIndexName(clazz.getName()));
        }
        indexing.setProperty(Environment.MODEL_MAPPING, mapping);

        // do we store as binary - e.g. Modules
        final StoreAsBinaryConfigurationBuilder storeAsBinaryConfigurationBuilder = builder.storeAsBinary();
        storeAsBinaryConfigurationBuilder.enabled(config.storeAsBinary());

        return mapping;
    }

    protected String getIndexName(String className) {
        return config.getName() + "_" + appId + "__" + className;
    }
}
