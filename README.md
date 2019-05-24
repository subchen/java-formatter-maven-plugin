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
```
