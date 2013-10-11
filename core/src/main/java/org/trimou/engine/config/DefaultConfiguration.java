/*
 * Copyright 2013 Martin Kouba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trimou.engine.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.listener.MustacheListener;
import org.trimou.engine.locale.LocaleSupport;
import org.trimou.engine.locale.LocaleSupportFactory;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.engine.priority.HighPriorityComparator;
import org.trimou.engine.resolver.Resolver;
import org.trimou.engine.text.TextSupport;
import org.trimou.engine.text.TextSupportFactory;
import org.trimou.exception.MustacheException;
import org.trimou.exception.MustacheProblem;
import org.trimou.util.SecurityActions;
import org.trimou.util.Strings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 *
 * @author Martin Kouba
 */
class DefaultConfiguration implements Configuration {

    private static final String RESOURCE_FILE = "/trimou.properties";

    private List<TemplateLocator> templateLocators = null;

    private List<Resolver> resolvers = null;

    private Map<String, Object> globalData = null;

    private TextSupport textSupport;

    private LocaleSupport localeSupport;

    private Map<String, Object> properties;

    private List<MustacheListener> mustacheListeners;

    /**
     *
     * @param builder
     */
    DefaultConfiguration(MustacheEngineBuilder builder) {

        if (!builder.isOmitServiceLoaderConfigurationExtensions()) {
            // Process configuration extensions
            for (Iterator<ConfigurationExtension> iterator = ServiceLoader
                    .load(ConfigurationExtension.class).iterator(); iterator
                    .hasNext();) {
                iterator.next().register(builder);
            }
        }
        identifyResolvers(builder);
        identifyTextSupport(builder);
        identifyLocaleSupport(builder);
        identifyTemplateLocators(builder);
        List<MustacheListener> listeners = builder.buildMustacheListeners();
        if (!listeners.isEmpty()) {
            this.mustacheListeners = listeners;
        }
        Map<String, Object> globalData = builder.buildGlobalData();
        if (!globalData.isEmpty()) {
            this.globalData = globalData;
        }
        initializeProperties(builder);
        initializeConfigurationAwareComponents();
    }

    @Override
    public List<Resolver> getResolvers() {
        return resolvers;
    }

    @Override
    public Map<String, Object> getGlobalData() {
        return globalData;
    }

    @Override
    public List<TemplateLocator> getTemplateLocators() {
        return templateLocators;
    }

    @Override
    public TextSupport getTextSupport() {
        return textSupport;
    }

    @Override
    public LocaleSupport getLocaleSupport() {
        return localeSupport;
    }

    @Override
    public List<MustacheListener> getMustacheListeners() {
        return mustacheListeners;
    }

    @Override
    public <T extends ConfigurationKey> Long getLongPropertyValue(
            T configurationKey) {

        Long value = (Long) properties.get(configurationKey.get());

        if (value == null) {
            value = (Long) configurationKey.getDefaultValue();
        }
        return value;
    }

    @Override
    public <T extends ConfigurationKey> Integer getIntegerPropertyValue(
            T configurationKey) {

        Integer value = (Integer) properties.get(configurationKey.get());

        if (value == null) {
            value = (Integer) configurationKey.getDefaultValue();
        }
        return value;
    }

    @Override
    public <T extends ConfigurationKey> String getStringPropertyValue(
            T configurationKey) {

        Object value = properties.get(configurationKey.get());

        if (value == null) {
            value = configurationKey.getDefaultValue();
        }
        return value.toString();
    }

    @Override
    public <T extends ConfigurationKey> Boolean getBooleanPropertyValue(
            T configurationKey) {

        Boolean value = (Boolean) properties.get(configurationKey.get());

        if (value == null) {
            value = (Boolean) configurationKey.getDefaultValue();
        }
        return value;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("----------");
        builder.append(Strings.LINE_SEPARATOR);
        builder.append("[");
        builder.append(this.getClass().getName());
        builder.append("]");
        if (templateLocators != null) {
            builder.append(Strings.LINE_SEPARATOR);
            builder.append("----------");
            builder.append(Strings.LINE_SEPARATOR);
            builder.append("[Template locators]");
            for (TemplateLocator locator : templateLocators) {
                builder.append(Strings.LINE_SEPARATOR);
                builder.append(locator.toString());
            }
        }
        if (resolvers != null) {
            builder.append(Strings.LINE_SEPARATOR);
            builder.append("----------");
            builder.append(Strings.LINE_SEPARATOR);
            builder.append("[Resolvers]");
            for (Resolver resolver : resolvers) {
                builder.append(Strings.LINE_SEPARATOR);
                builder.append(resolver.toString());
            }
        }
        builder.append(Strings.LINE_SEPARATOR);
        builder.append("----------");
        builder.append(Strings.LINE_SEPARATOR);
        builder.append("[Properties]");
        for (Entry<String, Object> entry : properties.entrySet()) {
            builder.append(Strings.LINE_SEPARATOR);
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }
        return builder.toString();
    }

    private void initializeConfigurationAwareComponents() {
        for (ConfigurationAware component : getConfigurationAwareComponents()) {
            component.init(this);
        }
    }

    private void identifyResolvers(MustacheEngineBuilder builder) {
        Set<Resolver> builderResolvers = builder.buildResolvers();
        if (!builderResolvers.isEmpty()) {
            resolvers = new ArrayList<Resolver>();
            resolvers.addAll(builderResolvers);
            Collections.sort(resolvers, new HighPriorityComparator());
            resolvers = ImmutableList.copyOf(resolvers);
        }
    }

    private void initializeProperties(MustacheEngineBuilder engineBuilder) {

        Set<ConfigurationKey> keysToProcess = getConfigurationKeysToProcess();

        ImmutableMap.Builder<String, Object> builder = ImmutableMap
                .builder();
        Map<String, Object> builderProperties = engineBuilder.buildProperties();
        Properties resourceProperties = new Properties();

        try {
            InputStream in = this.getClass().getResourceAsStream(RESOURCE_FILE);
            if (in != null) {
                try {
                    resourceProperties.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            // No-op, file is optional
        }

        for (ConfigurationKey configKey : keysToProcess) {

            String key = configKey.get();

            // Manually set properties
            Object value = builderProperties.get(key);

            if (value == null) {
                // System properties
                value = SecurityActions.getSystemProperty(key);
                if (value == null) {
                    // Resource properties
                    value = resourceProperties.getProperty(key);
                }
            }

            if (value != null) {
                try {
                    value = ConfigurationProperties.convertConfigValue(
                            configKey.getDefaultValue().getClass(), value);
                } catch (Exception e) {
                    throw new MustacheException(
                            MustacheProblem.CONFIG_PROPERTY_INVALID_VALUE, e);
                }
            } else {
                value = configKey.getDefaultValue();
            }
            builder.put(key, value);
        }
        this.properties = builder.build();
    }

    private Set<ConfigurationKey> getConfigurationKeysToProcess() {
        Set<ConfigurationKey> keys = new HashSet<ConfigurationKey>();
        // Global keys
        for (ConfigurationKey key : EngineConfigurationKey.values()) {
            keys.add(key);
        }
        for (ConfigurationAware component : getConfigurationAwareComponents()) {
            keys.addAll(component.getConfigurationKeys());
        }
        return keys;
    }

    private void identifyTextSupport(MustacheEngineBuilder builder) {
        if (builder.getTextSupport() != null) {
            textSupport = builder.getTextSupport();
        } else {
            textSupport = new TextSupportFactory().createTextSupport();
        }
    }

    private void identifyLocaleSupport(MustacheEngineBuilder builder) {
        if (builder.getLocaleSupport() != null) {
            localeSupport = builder.getLocaleSupport();
        } else {
            localeSupport = new LocaleSupportFactory().createLocateSupport();
        }
    }

    private void identifyTemplateLocators(MustacheEngineBuilder builder) {
        Set<TemplateLocator> builderTemplateLocators = builder
                .buildTemplateLocators();
        if (!builderTemplateLocators.isEmpty()) {
            List<TemplateLocator> locators = new ArrayList<TemplateLocator>(
                    builder.buildTemplateLocators());
            Collections.sort(locators, new HighPriorityComparator());
            this.templateLocators = ImmutableList.copyOf(locators);
        }
    }

    private Set<ConfigurationAware> getConfigurationAwareComponents() {
        Set<ConfigurationAware> components = new HashSet<ConfigurationAware>();
        components.addAll(resolvers);
        if (templateLocators != null) {
            components.addAll(templateLocators);
        }
        if (mustacheListeners != null) {
            components.addAll(mustacheListeners);
        }
        components.add(localeSupport);
        components.add(textSupport);
        return components;
    }

}
