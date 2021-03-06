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
package org.trimou.handlebars;

import org.trimou.engine.MustacheTagType;

/**
 *
 *
 * @author Martin Kouba
 */
public abstract class BasicValueHelper extends BasicHelper {

    protected static final MustacheTagType[] VALUE_TAG_TYPES = new MustacheTagType[] {
            MustacheTagType.VARIABLE, MustacheTagType.UNESCAPE_VARIABLE };

    @Override
    protected MustacheTagType[] allowedTagTypes() {
        return VALUE_TAG_TYPES;
    }

}
