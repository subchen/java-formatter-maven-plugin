/**
 * Copyright 2018-2019 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 *   Author: Guoqiang Chen
 *    Email: subchen@gmail.com
 *   WebURL: https://github.com/subchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.subchen.maven.plugin.javafmt;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

import com.google.googlejavaformat.java.RemoveUnusedImports;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@UtilityClass
class ImportUtils {
    private static final String LINE_SEPARATOR = JavaFormatter.LINE_SEPARATOR;
    private static final Pattern IMPORT_LINE_PATTERN = Pattern.compile("^import .+;$", Pattern.MULTILINE);

    public String format(String code) {
        return reorderImports(removeUnusedImports(code));
    }

    /**
     * Using google-java-format to remove unused imports
     */
    @SneakyThrows
    private String removeUnusedImports(String code) {
        return RemoveUnusedImports.removeUnusedImports(code);
    }

    /**
     * Reorder import statements using customize groups.
     */
    private String reorderImports(String code) {
        var start = 0;
        var end = 0;

        var javaSet = new TreeSet<>();
        var othersSet = new TreeSet<>();
        Set<String> staticSet = null;
        Set<String> lombokSet = null;

        val m = IMPORT_LINE_PATTERN.matcher(code);
        while (m.find()) {
            if (start <= 0) {
                start = m.start();
            }
            end = m.end();

            val line = m.group();
            if (line.startsWith("import java.") || line.startsWith("import javax.")) {
                javaSet.add(line);
            } else if (line.startsWith("import lombok.")) {
                if (lombokSet == null) {
                    lombokSet = new TreeSet<>();
                }
                lombokSet.add(line);
            } else if (line.startsWith("import static ")) {
                if (staticSet == null) {
                    staticSet = new TreeSet<>();
                }
                staticSet.add(line);
            } else {
                othersSet.add(line);
            }
        }

        if (end == 0) {
            // no imports found
            return code;
        }

        val sb = new StringBuilder(end - start + 64);
        if (lombokSet != null && lombokSet.size() > 0) {
            lombokSet.forEach(line -> sb.append(line).append(LINE_SEPARATOR));
        }
        if (othersSet.size() > 0) {
            if (sb.length() > 0) {
                sb.append(LINE_SEPARATOR);
            }
            othersSet.forEach(line -> sb.append(line).append(LINE_SEPARATOR));
        }
        if (javaSet.size() > 0) {
            if (sb.length() > 0) {
                sb.append(LINE_SEPARATOR);
            }
            javaSet.forEach(line -> sb.append(line).append(LINE_SEPARATOR));
        }
        if (staticSet != null) {
            if (sb.length() > 0) {
                sb.append(LINE_SEPARATOR);
            }
            staticSet.forEach(line -> sb.append(line).append(LINE_SEPARATOR));
        }

        return code.substring(0, start) + sb.toString().trim() + code.substring(end);
    }
}
