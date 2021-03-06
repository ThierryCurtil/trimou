/*
 * Copyright 2016 Martin Kouba
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
package org.trimou.el;

import static org.trimou.engine.config.ConfigurationExtensions.registerHelper;

import org.trimou.engine.config.ConfigurationExtension;
import org.trimou.engine.config.DefaultConfigurationExtension;
import org.trimou.handlebars.HelpersBuilder;

/**
 *
 * @author Martin Kouba
 */
public class ELConfigurationExtension implements ConfigurationExtension {

    @Override
    public void register(ConfigurationExtensionBuilder builder) {
        registerHelper(builder, ELHelper.DEFAULT_NAME, new ELHelper());
        registerHelper(builder, HelpersBuilder.IF, new ELIfHelper());
        registerHelper(builder, HelpersBuilder.EACH, new ELEachHelper());
        registerHelper(builder, HelpersBuilder.SET, new ELSetHelper());
    }

    @Override
    public int getPriority() {
        // Make sure the ELIfHelper is registered before the default IfHelper
        return DefaultConfigurationExtension.DEFAULT_EXTENSION_PRIORITY + 1;
    }

}
