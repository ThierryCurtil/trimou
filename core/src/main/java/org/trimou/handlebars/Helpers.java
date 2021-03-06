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
package org.trimou.handlebars;

import org.trimou.engine.MustacheTagType;
import org.trimou.handlebars.HelperDefinition.ValuePlaceholder;

/**
 *
 * @author Martin Kouba
 * @since 2.0
 */
public class Helpers {

    private Helpers() {
    }

    /**
     *
     * @param options
     * @return <code>true</code> if the underlying tag represents a section,
     *         <code>false</code> otherwise
     */
    public static boolean isSection(Options options) {
        return options.getTagInfo().getType().equals(MustacheTagType.SECTION);
    }

    /**
     *
     * @param options
     * @return <code>true</code> if the underlying tag represents a variable,
     *         <code>false</code> otherwise
     */
    public static boolean isVariable(Options options) {
        return options.getTagInfo().getType().equals(MustacheTagType.VARIABLE)
                || isUnescapeVariable(options);
    }

    /**
     *
     * @param options
     * @return <code>true</code> if the underlying tag represents a variable
     *         which shouldn't be escaped, <code>false</code> otherwise
     */
    public static boolean isUnescapeVariable(Options options) {
        return options.getTagInfo().getType()
                .equals(MustacheTagType.UNESCAPE_VARIABLE);
    }

    /**
     *
     * @param options
     * @param key
     * @return the {@link Integer} value for the given key or null if not
     *         specified
     */
    public static Integer initIntHashEntry(Options options, String key) {
        Object value = options.getHash().get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return Integer.valueOf(value.toString());
        }
    }

    /**
     *
     * @param options
     * @param key
     * @param defaultValue
     * @return the {@link Integer} value for the given key or default value if not
     *         specified
     */
    public static Integer initIntHashEntry(Options options, String key, Integer defaultValue) {
        Integer value = initIntHashEntry(options, key);
        return value != null ? value : defaultValue;
    }

    /**
     *
     * @param value
     * @return <code>true</code> if the given value represents a {@link ValuePlaceholder}, <code>false</code> otherwise
     */
    public static boolean isValuePlaceholder(Object value) {
        return value instanceof ValuePlaceholder;
    }

}
