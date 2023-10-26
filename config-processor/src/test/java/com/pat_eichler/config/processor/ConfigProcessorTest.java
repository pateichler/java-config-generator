package com.pat_eichler.config.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.StandardLocation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class ConfigProcessorTest {
    void checkIfMatchFile(Compilation compilation, String outputPackageName, String outputFileName, String targetFileName, boolean isDefault) throws IOException {
        URL u = ConfigProcessorTest.class.getResource(targetFileName);
        assert u != null;
        File targetFile = new File(u.getFile());
        String targetText = new String(Files.readAllBytes(targetFile.toPath()));

        assertThat(compilation)
                .generatedFile(isDefault ? StandardLocation.CLASS_OUTPUT : StandardLocation.SOURCE_OUTPUT, outputPackageName, outputFileName)
                .contentsAsUtf8String().isEqualTo(targetText);
    }

    @Test
    public void testDefaultExampleSettings() throws IOException {
        URL u = ConfigProcessorTest.class.getResource("targetDefaultExampleSettings.json");
        assert u != null;
        File targetFile = new File(u.getFile());
        String targetText = new String(Files.readAllBytes(targetFile.toPath()));

        Compilation compilation =
                javac()
                        .withProcessors(new ConfigProcessor())
                        .compile(JavaFileObjects.forResource(Objects.requireNonNull(ConfigProcessorTest.class.getResource("ExampleSettings.java"))));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "ExampleSettings", "testConfig.json")
                .contentsAsUtf8String().isEqualTo(targetText);
    }

    @Test
    public void testDefaultExampleInfoSettings() throws IOException {
        URL u = ConfigProcessorTest.class.getResource("targetInfoExampleSettings.json");
        assert u != null;
        File targetFile = new File(u.getFile());
        String targetText = new String(Files.readAllBytes(targetFile.toPath()));

        Compilation compilation =
                javac()
                        .withProcessors(new ConfigProcessor())
                        .compile(JavaFileObjects.forResource(Objects.requireNonNull(ConfigProcessorTest.class.getResource("ExampleInfoSettings.java"))));
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "ExampleSettings", "testConfig.json")
                .contentsAsUtf8String().isEqualTo(targetText);
    }

    @Test
    public void testExampleNestedSettings() throws IOException {


        Compilation compilation =
                javac()
                        .withProcessors(new ConfigProcessor())
                        .compile(JavaFileObjects.forResource(Objects.requireNonNull(ConfigProcessorTest.class.getResource("ExampleNestedSettings.java"))));
        assertThat(compilation).succeeded();

        checkIfMatchFile(compilation, "ExampleNestedSettings", "testConfig.json", "targetDefaultExampleNestedSettings.json", true);
        checkIfMatchFile(compilation, "ExampleNestedSettings", "testConfig.json", "targetInfoExampleNestedSettings.json", false);

    }
}
