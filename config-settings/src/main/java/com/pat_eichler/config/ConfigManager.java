package com.pat_eichler.config;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class ConfigManager {

    /**
     * Load config from file.
     * @param configFile File of config to be loaded
     * @param cls Class of config to load
     * @return Returns loaded config object
     * @param <T> Type of config class to load
     */
    public static <T> T loadConfig(File configFile, Class<T> cls) {
        if(cls.getAnnotation(ConfigClass.class) == null)
            throw new RuntimeException("Can not load class without ConfigClass annotation");

        Gson gson = new Gson();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        T obj = gson.fromJson(reader, cls);
        if(obj == null)
            throw new RuntimeException("Config could not be loaded");

//        validateSettings(obj, new LinkedList<>(), new HashSet<String>(Arrays.asList(partialFields)));
        validateConfig(obj, new LinkedList<>());

        return obj;
    }

    /**
     * Load config from resources file.
     * @param fileName Name of resource file relative to the class resource
     * @param cls Class of config to load
     * @return Returns loaded config object
     * @param <T> Type of config class to load
     */
    public static <T> T loadConfigFromResources(String fileName, Class<T> cls){
        ClassLoader classLoader = cls.getClassLoader();
        URL r = classLoader.getResource(fileName);
        assert r != null;
        File f = new File(r.getFile());

        return loadConfig(f, cls);
    }

    /**
     * Save config to a file.
     * @param config Config settings to save
     * @param f File for save
     */
    public static void saveConfig(Object config, File f){
        Gson gson = new Gson();
        try {
            try(Writer w = new BufferedWriter(new FileWriter(f))){
                gson.toJson(config, w);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate loaded config to make sure all fields are not null. Validation will check for circular imports.
     * @param config Config settings to validate
     * @param classList List of validated classes
     */
    public static void validateConfig(Object config, LinkedList<Class<?>> classList){
        if(classList.contains(config.getClass()))
            throw new RuntimeException("Circular import for class: " + config.getClass());

        for(Field f : config.getClass().getDeclaredFields()) {
            //Ignore static or transient fields
            int m = f.getModifiers();
            if(Modifier.isStatic(m) || Modifier.isTransient(m))
                continue;

//            if(partialFields != null && !partialFields.contains(f.getName()))
//                continue;

            try {
                Object settingsField = f.get(config);
                if(settingsField == null)
                    throw new RuntimeException("Field not set: " + config.getClass().getCanonicalName() + "." + f.getName());
                //  else if(ConfigSettings.SubSettings.class.isAssignableFrom(f.get(this).getClass())){
                else if(settingsField.getClass().getAnnotation(ConfigClass.class) != null){
                    LinkedList<Class<?>> newList = new LinkedList<>(classList);
                    classList.add(config.getClass());

                    validateConfig(settingsField, newList);
                }
            } catch (IllegalAccessException ignored) { }
        }
    }
}
