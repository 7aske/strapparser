package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.generic.MavenDependency
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinMavenGenerator(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(ctx, dataTypeResolver) {

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "pom.xml"
    )

    override fun generate(): String = buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine(
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0" +
                " https://maven.apache.org/xsd/maven-4.0.0.xsd\">"
        )
        appendLine("    <modelVersion>4.0.0</modelVersion>")
        appendLine("    <parent>")
        appendLine("        <groupId>org.springframework.boot</groupId>")
        appendLine("        <artifactId>spring-boot-starter-parent</artifactId>")
        appendLine("        <version>${ctx.args.springVersion}</version>")
        appendLine("        <relativePath/>")
        appendLine("    </parent>")
        appendLine("    <groupId>${ctx.args.domain}</groupId>")
        appendLine("    <artifactId>${ctx.args.name}</artifactId>")
        appendLine("    <name>${ctx.args.name}</name>")
        appendLine("    <version>0.0.1-SNAPSHOT</version>")
        appendLine("    <packaging>${ctx.args.packaging}</packaging>")
        appendLine("    <description>Strap Application</description>")
        appendLine("    <properties>")
        appendLine("        <java.version>${ctx.args.javaVersion}</java.version>")
        appendLine("        <kotlin.version>${ctx.args.kotlinVersion}</kotlin.version>")
        appendLine("    </properties>")
        appendLine("    <dependencies>")
        appendLine("        <dependency>")
        appendLine("            <groupId>org.springframework.boot</groupId>")
        appendLine("            <artifactId>spring-boot-starter-web</artifactId>")
        appendLine("        </dependency>")
        for (dependency in ctx.dependencies) {
            appendLine(generateDependency(dependency))
        }
        appendLine(
            """
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib-jdk8</artifactId>
            <version>${'$'}{kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-reflect</artifactId>
            <version>${'$'}{kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test</artifactId>
            <version>${'$'}{kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        """
        )
        appendLine("        <dependency>")
        appendLine("            <groupId>org.springframework.boot</groupId>")
        appendLine("            <artifactId>spring-boot-starter-test</artifactId>")
        appendLine("            <scope>test</scope>")
        appendLine("        </dependency>")
        appendLine("    </dependencies>")
        appendLine("    <build>")
        appendLine("        <sourceDirectory>src/main/kotlin</sourceDirectory>")
        appendLine("        <plugins>")
        appendLine("            <plugin>")
        appendLine("                <groupId>org.springframework.boot</groupId>")
        appendLine("                <artifactId>spring-boot-maven-plugin</artifactId>")
        appendLine("            </plugin>")
        appendLine("            <plugin>")
        appendLine("                <groupId>org.jetbrains.kotlin</groupId>")
        appendLine("                <artifactId>kotlin-maven-plugin</artifactId>")
        appendLine("                <version>${'$'}{kotlin.version}</version>")
        appendLine("                <executions>")
        appendLine("                    <execution>")
        appendLine("                        <id>compile</id>")
        appendLine("                        <phase>compile</phase>")
        appendLine("                        <goals>")
        appendLine("                            <goal>compile</goal>")
        appendLine("                        </goals>")
        appendLine("                    </execution>")
        appendLine("                    <execution>")
        appendLine("                        <id>test-compile</id>")
        appendLine("                        <phase>test-compile</phase>")
        appendLine("                        <goals>")
        appendLine("                            <goal>test-compile</goal>")
        appendLine("                        </goals>")
        appendLine("                    </execution>")
        appendLine("                </executions>")
        appendLine("            <configuration>")
        appendLine(
            """
                <pluginOptions>
                    <option>all-open:annotation=jakarta.persistence.Entity</option>
                </pluginOptions>
                <compilerPlugins>
                    <plugin>noarg</plugin>
                    <plugin>allopen</plugin>
                    <plugin>spring</plugin>
                    <plugin>jpa</plugin>
                </compilerPlugins>
        """
        )
        appendLine(
            """
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-allopen</artifactId>
                        <version>${'$'}{kotlin.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-noarg</artifactId>
                        <version>${'$'}{kotlin.version}</version>
                    </dependency>
                </dependencies>
        """
        )
        appendLine("                <jvmTarget>${'$'}{java.version}</jvmTarget>")
        appendLine("            </configuration>")
        appendLine("            </plugin>")
        appendLine("        </plugins>")
        appendLine("    </build>")
        appendLine("</project>")
    }

    private fun generateDependency(dependency: String): String {
        val parts = dependency.split(":")
        return MavenDependency(parts[0], parts.getOrNull(1)).generate()
    }
}
