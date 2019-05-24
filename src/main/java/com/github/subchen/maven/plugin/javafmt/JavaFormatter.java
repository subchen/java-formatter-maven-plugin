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
import lombok.val;

import org.dom4j.io.SAXReader;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Format Single Java Source
 *
 * @author guoqiang.chen
 */
public final class JavaFormatter {
    public static final String LINE_SEPARATOR = "\n";

    private static final String JAVA_FORMATTER_PREFS_FILE = "java-formatter.xml";

    private final CodeFormatter formatter;

    public JavaFormatter(File basedir, String compilerSource) {
        val options = getFormattingOptions(basedir);

        // override by compile source
        options.put(JavaCore.COMPILER_SOURCE, compilerSource);
        options.put(JavaCore.COMPILER_COMPLIANCE, compilerSource);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compilerSource);

        formatter = ToolFactory.createCodeFormatter(options);
    }

    public String format(String code) throws Exception {
        return ImportUtils.format(formatCode(code));
    }

    private String formatCode(String code) throws BadLocationException {
        int options = CodeFormatter.K_COMPILATION_UNIT + CodeFormatter.F_INCLUDE_COMMENTS;
        val testEdit = formatter.format(options, code, 0, code.length(), 0, LINE_SEPARATOR);

        val doc = new Document(code);
        testEdit.apply(doc);
        return doc.get();
    }

    @SneakyThrows
    private Properties getFormattingOptions(File basedir) {
        val file = findup(basedir, JAVA_FORMATTER_PREFS_FILE);

        val fileStream = (file != null)
            ? new FileInputStream(file)
            : getClass().getResourceAsStream("/" + JAVA_FORMATTER_PREFS_FILE);

        val options = new Properties();

        new SAXReader().read(fileStream)
            .getRootElement()
            .element("profile")
            .elements()
            .stream()
            .forEach(el -> options.setProperty(el.attributeValue("id"), el.attributeValue("value")));

        return options;
    }

    private File findup(File basedir, String fileName) {
        File file = new File(basedir, fileName);
        if (file.exists()) {
            return file;
        }
        if (basedir.getParentFile() == null) {
            return null;
        }
        return findup(basedir.getParentFile(), fileName);
    }
}
