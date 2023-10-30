package com.pat_eichler.config;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigManagerTest {

    /**
     * Test simple config load
     */
    @Test
    public void testLoadConfig(){
        ExampleSettings ex = ConfigManager.loadConfigFromResources("example.json", ExampleSettings.class);

        assertEquals(ex.testVar, 4);
        assertEquals(ex.testVar2, true);
    }

    /**
     * Test partial simple config load with expected error
     */
    @Test
    public void testLoadPartialConfig(){
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ConfigManager.loadConfigFromResources("examplePartial.json", ExampleSettings.class));
        assertEquals("Field not set: com.pat_eichler.config.ExampleSettings.testVar2", exception.getMessage());
    }

    /**
     * Test nested config load
     */
    @Test
    public void testLoadNestedConfig(){
        ExampleNestedSettings ex = ConfigManager.loadConfigFromResources("exampleNested.json", ExampleNestedSettings.class);

        assertEquals(ex.a.testString, "test");
        assertEquals(ex.a.testInt, 2);
        assertEquals(ex.b.testInt, 3);
        assertEquals(ex.b.testBool, true);
    }

    /**
     * Test two config loads and make sure static classes don't interfere with each other
     */
    @Test
    public void testLoadNestedConfigStatic(){
        ExampleNestedSettings ex = ConfigManager.loadConfigFromResources("exampleNested.json", ExampleNestedSettings.class);
        ex.a.testInt = 10;

        assertEquals(ex.a.testInt, 10);

        ExampleNestedSettings ex2 = ConfigManager.loadConfigFromResources("exampleNested.json", ExampleNestedSettings.class);
        assertEquals(ex2.a.testInt, 2);
        assertEquals(ex.a.testInt, 10);
    }

    /**
     * Test partial nested config load with expected error
     */
    @Test
    public void testLoadNestedPartialConfig(){
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ConfigManager.loadConfigFromResources("exampleNestedPartial.json", ExampleNestedSettings.class));
        assertEquals("Field not set: com.pat_eichler.config.ExampleNestedSettings.B.testBool", exception.getMessage());
    }

    /**
     * Test config load and then save and make sure they are equivalent objects.
     */
    @Test
    public void testLoadAndSaveConfig(){
        ExampleNestedSettings ex = ConfigManager.loadConfigFromResources("exampleNested.json", ExampleNestedSettings.class);
        ExampleNestedSettings ex2;

        try {
            File f = File.createTempFile("testExampleConfig", "json");
            ConfigManager.saveConfig(ex, f);
            ex2 = ConfigManager.loadConfig(f, ExampleNestedSettings.class);
            f.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(ex2.a.testInt, ex.a.testInt);
        assertEquals(ex2.a.testString, ex.a.testString);
        assertEquals(ex2.b.testInt, ex.b.testInt);
        assertEquals(ex2.b.testBool, ex.b.testBool);
    }
}
