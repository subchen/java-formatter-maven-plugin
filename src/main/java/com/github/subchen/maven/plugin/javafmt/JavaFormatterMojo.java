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

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResource;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java Formatter for maven plugin
 *
 * @author guoqiang.chen
 */
@Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public final class JavaFormatterMojo extends AbstractMojo {
    private static final String[] DEFAULT_INCLUDES = new String[] { "**/*.java" };
    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/generated/**" };

    private static final String MD5_CACHE_FILENAME = "java-formatter-cache.md5";

    /**
     * Project's base directory.
     */
    @Parameter(property = "basedir", defaultValue = "${basedir}", readonly = true)
    private File basedir;

    /**
     * Project's target directory as specified in the POM.
     */
    @Parameter(property = "targetDirectory", defaultValue = "${project.build.directory}", readonly = true)
    private File targetDirectory;

    /**
     * The file encoding used to read and write source files. When not specified and
     * sourceEncoding also not set, default is platform file encoding.
     */
    @Parameter(property = "sourceEncoding", defaultValue = "${project.build.sourceEncoding}")
    private String sourceEncoding;

    /**
     * Java compiler source version.
     */
    @Parameter(property = "compilerSource", defaultValue = "${maven.compiler.source}")
    private String compilerSource;

    @Override
    public void execute() {
        long startClock = System.currentTimeMillis();

        val fileList = collectJavaSourceFileList();
        if (fileList.isEmpty()) {
            getLog().info("No java source found");
            return;
        }

        val md5Cache = loadMd5Cache();
        val javaFormatter = new JavaFormatter(basedir, compilerSource);
        val rc = new ResultCollector();

        fileList.forEach(file -> {
            val originalCode = readFileToString(file, Charset.forName(sourceEncoding));
            val originalMd5 = DigestUtils.md5Hex(originalCode);

            if (md5Cache.contains(originalMd5)) {
                rc.skippedCount.increment();
                return;
            }

            // format
            String formattedCode;
            try {
                formattedCode = javaFormatter.format(originalCode);
            } catch (Exception e) {
                getLog().warn("Code cannot be formatted: " + file, e);
                rc.failCount.increment();
                return;
            }

            val formattedMd5 = DigestUtils.md5Hex(formattedCode);

            // update cache
            md5Cache.add(formattedMd5);

            if (Objects.equals(formattedMd5, originalMd5)) {
                rc.skippedCount.increment();
                return;
            }

            writeStringToFile(file, formattedCode, Charset.forName(sourceEncoding));
            rc.successCount.increment();
        });

        if (rc.successCount.intValue() > 0) {
            saveMd5Cache(md5Cache);
        }

        getLog().info("Successfully formatted: " + rc.successCount.intValue() + " file(s)");
        getLog().info("Fail to format        : " + rc.failCount.intValue() + " file(s)");
        getLog().info("Skipped               : " + rc.skippedCount.intValue() + " file(s)");
        getLog().info("Approximate time taken: " + ((System.currentTimeMillis() - startClock) / 1000) + " (s)");
    }

    @SneakyThrows
    private List<File> collectJavaSourceFileList() {
        val search = new PlexusIoFileResourceCollection();

        // includes & excludes
        search.setIncludingEmptyDirectories(false);
        search.setIncludes(DEFAULT_INCLUDES);
        search.setExcludes(DEFAULT_EXCLUDES);

        IncludeExcludeFileSelector fileSelector = new IncludeExcludeFileSelector();
        fileSelector.setIncludes(DEFAULT_INCLUDES);
        fileSelector.setExcludes(DEFAULT_EXCLUDES);
        search.setFileSelectors(new FileSelector[] { fileSelector });

        // source base dir
        search.setBaseDir(new File(basedir, "src"));

        // get search results
        val resources = search.getResources();
        val files = new ArrayList<File>(128);
        while (resources.hasNext()) {
            val resource = (PlexusIoFileResource) resources.next();
            files.add(resource.getFile());
        }

        getLog().info("Number of files to be formatted: " + files.size());
        return files;
    }

    private Set<String> loadMd5Cache() {
        val file = new File(targetDirectory, MD5_CACHE_FILENAME);
        if (!file.exists()) {
            return new HashSet<>(128);
        }

        val lines = readFileToString(file, StandardCharsets.UTF_8);
        return Stream.of(lines.split("\n")).collect(Collectors.toSet());
    }

    @SneakyThrows
    private void saveMd5Cache(Set<String> md5Cache) {
        val file = new File(targetDirectory, MD5_CACHE_FILENAME);
        val lines = String.join("\n", md5Cache);

        writeStringToFile(file, lines, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private static String readFileToString(File file, Charset encoding) {
        return FileUtils.readFileToString(file, encoding);
    }

    @SneakyThrows
    private static void writeStringToFile(File file, String data, Charset encoding) {
        FileUtils.writeStringToFile(file, data, encoding);
    }

    @Getter
    static class ResultCollector {
        final MutableInt skippedCount = new MutableInt();
        final MutableInt successCount = new MutableInt();
        final MutableInt failCount = new MutableInt();
    }

}
