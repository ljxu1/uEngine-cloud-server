/*
 * Copyright 2015-2015 Groupon, Inc
 * Copyright 2015-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.uengine.cloud.templates;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.util.Map;

public class MustacheTemplateEngine implements TemplateEngine {

    @Override
    public String executeTemplateText(final String templateText, final Map<String, Object> data) {
        final Template template = Mustache.compiler().nullValue("").compile(templateText);
        return template.execute(data);
    }
}
