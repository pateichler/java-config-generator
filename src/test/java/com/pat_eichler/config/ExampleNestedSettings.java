package com.pat_eichler.config;

import com.pat_eichler.config.ConfigClass;

@ConfigClass
public class ExampleNestedSettings {
    public A a;
    public B b;
    @ConfigClass
    public static class A{
        public String testString;
        public Integer testInt;

        public C c;

        @ConfigClass
        public static class C{
            public Double testDouble;
        }
    }

    @ConfigClass
    public static class B{
        public Integer testInt;

        public Boolean testBool;
    }
}
