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

    Compilation compileFile(String fileName){
        return  javac()
                        .withProcessors(new ConfigProcessor())
                        .compile(JavaFileObjects.forResource(Objects.requireNonNull(ConfigProcessorTest.class.getResource(fileName))));
    }
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
        Compilation compilation = compileFile("ExampleSettings.java");
        assertThat(compilation).succeeded();

        checkIfMatchFile(compilation, "ExampleSettings", "testConfig.json", "targetDefaultExampleSettings.json", true);
    }

    @Test
    public void testDefaultExampleInfoSettings() throws IOException {
        Compilation compilation = compileFile("ExampleInfoSettings.java");
        assertThat(compilation).succeeded();

        checkIfMatchFile(compilation, "ExampleInfoSettings", "testConfig.json", "targetInfoExampleSettings.json", false);
    }

    @Test
    public void testExampleNestedSettings() throws IOException {
        Compilation compilation = compileFile("ExampleNestedSettings.java");
        assertThat(compilation).succeeded();

        checkIfMatchFile(compilation, "ExampleNestedSettings", "testConfig.json", "targetDefaultExampleNestedSettings.json", true);
        checkIfMatchFile(compilation, "ExampleNestedSettings", "testConfig.json", "targetInfoExampleNestedSettings.json", false);

    }
}
