package com.pat_eichler.config.processor;


import com.google.gson.stream.JsonWriter;
import com.pat_eichler.config.ConfigClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.pat_eichler.config.processor.ProcessConfig")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigProcessor  extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //TODO: Redundant code since we don't use the annotations field to specify the annotation
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ProcessConfig.class);
        for(Element e : elements){
            ProcessConfig pc = e.getAnnotation(ProcessConfig.class);

            if(!pc.defaultsFileName().isEmpty())
                processConfig(e, pc.defaultsFileName(), true);
            if(!pc.infoFileName().isEmpty())
                processConfig(e, pc.infoFileName(), false);

            if(pc.defaultsFileName().isEmpty() && pc.infoFileName().isEmpty())
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "No target file output set for Config Settings", e);
        }

        return true;
    }

    void processConfig(Element e, String fileName, boolean defaults){
        System.out.println("Processing " + (defaults ? "default" : "info") + " config");
        try {
            FileObject f = processingEnv.getFiler().createResource(defaults ? StandardLocation.CLASS_OUTPUT : StandardLocation.SOURCE_OUTPUT, e.asType().toString(), fileName);
            try(Writer writer = f.openWriter()){
                JsonWriter out = new JsonWriter(writer);
                out.setIndent("  ");
                out.beginObject();
                processClass(e, out, new LinkedList<>(), defaults);
                out.endObject();
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    final String[] supportedTypes = new String[]{"Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "String"};
    int getTypeCode(String type){
        for (int i = 0; i < supportedTypes.length; i++){
            if(supportedTypes[i].equals(type))
                return i;
        }
        return -1;
    }

    String getTypeDefaultValue(int typeCode, boolean array){
        if(array){
            return "[]";
        }else if(typeCode < 4){
            return "0";
        }else if(typeCode < 6){
            return "0.0";
        }else if(typeCode < 7) {
            return "false";
        }

        return "\"\"";
    }

    String[] getElementInfo(Element e){
        String[] s = new String[3];
        String type = e.asType().toString();

        ConfigProperty cp = e.getAnnotation(ConfigProperty.class);
        if(cp != null){
            String d = cp.defualtValue();
            if(d != null && !d.isEmpty()){
                if(type.equals("java.lang.String") && !d.startsWith("\""))
                    s[1] = "\"" + cp.defualtValue() + "\"";
                else
                    s[1] = cp.defualtValue();
            }

            s[2] = cp.comment();
        }

        if (!type.startsWith("java.lang")){
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unknown data type: " + type);
            return s;
        }

        type = type.replace("java.lang.", "");
        boolean array = type.endsWith("[]");

        int t = getTypeCode(array ? type.substring(0, type.length()-2) : type);

        if(t < 0) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unknown data type: " + type);
            return s;
        }

        s[0] = type;
        if(s[1] == null || s[1].isEmpty())
            s[1] = getTypeDefaultValue(t, array);

        return s;
    }

    void writeElement(Element e, JsonWriter out, boolean defaults) throws IOException {
        String[] s = getElementInfo(e);
        out.name(e.getSimpleName().toString());

        if(defaults){
            out.jsonValue(s[1]);
        }else {
            out.beginObject();
            out.name("type").value(s[0]);
            out.name("default").jsonValue(s[1]);
            if (s[2] != null && !s[2].isEmpty())
                out.name("comment").value(s[2]);
            out.endObject();
        }
    }

    final Set<Modifier> nonSerializableModifiers = new HashSet<>(Arrays.asList(Modifier.TRANSIENT, Modifier.STATIC));
    boolean isElementSerializable(Element e){
        return e.getModifiers().stream().noneMatch(nonSerializableModifiers::contains);
    }

    void processClass(Element cls, JsonWriter out, LinkedList<String> classList, boolean defaults){
        try {
            out.name(cls.getSimpleName().toString());
            out.beginObject();

            String clsName = cls.asType().toString();
            if(classList.contains(clsName)){
                String circularImport = String.join(" -> ", classList) + " -> " + classList.get(0);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Circular settings import: " + circularImport, cls);
                return;
            }

            LinkedList<String> newList = new LinkedList<String>(classList);
            newList.add(clsName);

            //Loop through the config settings elements
            for(Element field : cls.getEnclosedElements().stream().filter(f -> f.getKind() == ElementKind.FIELD).toList()) {
                if(!isElementSerializable(field))
                    continue;

                Element fieldCls = processingEnv.getTypeUtils().asElement(field.asType());
                if (fieldCls != null && fieldCls.getAnnotation(ConfigClass.class) != null)
                    processClass(fieldCls, out, newList, defaults);
                else if(fieldCls != null && fieldCls.getKind() == ElementKind.ENUM) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Can't currently process enums " + fieldCls.getSimpleName());
                    for(Element option : fieldCls.getEnclosedElements())
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Enum option " + option.getSimpleName() + ": " + option.getKind());
                }else
                    writeElement(field, out, defaults);
            }

            out.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //    boolean isConfig = cls.getAnnotation(ConfigClass.class) != null;
    //    if(!isConfig) {
    //        for (TypeMirror s : processingEnv.getTypeUtils().directSupertypes(cls.asType())) {
    //            if (processingEnv.getTypeUtils().asElement(s).getAnnotation(ConfigClass.class) != null) {
    //                isConfig = true;
    //
    //                break;
    //            }
    //        }
    //    }
}
