[![Maven](https://img.shields.io/maven-central/v/com.github.subchen/java-formatter-maven-plugin.svg?style=flat&label=java-formatter-maven-plugin)](https://search.maven.org/search?q=a:java-formatter-maven-plugin)
[![Build Status](https://travis-ci.org/subchen/java-formatter-maven-plugin.svg?branch=master)](https://travis-ci.org/subchen/java-formatter-maven-plugin)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/subchen-java-formatter-maven-plugin/badge.svg?flat=1)](https://scan.coverity.com/projects/subchen-java-formatter-maven-plugin)
[![JDK](http://img.shields.io/badge/JDK-v8.0+-yellow.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![License](http://img.shields.io/badge/License-Apache_2-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# java-formatter-maven-plugin

Formats your java code using maven

## Usage

To have your sources automatically formatted on each build, add to your `pom.xml`:

```xml
<plugin>
    <groupId>com.github.subchen</groupId>
    <artifactId>java-formatter-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>process-sources</phase>
            <goals>
                <goal>format</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Command line

```bash
mvn java-formatter:format

or

mvn process-sources
```

## Eclipse & IntelliJ IDEA

Download & Import config file into Eclipse IDE. If you are using IntelliJ IDEA, please install `Eclipse Code Formmater` plugin.

https://github.com/subchen/java-formatter-maven-plugin/blob/master/src/main/resources/java-formatter.xml



