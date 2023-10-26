import com.pat_eichler.config.ConfigClass;
import com.pat_eichler.config.processor.ConfigProperty;
import com.pat_eichler.config.processor.ProcessConfig;

@ConfigClass @ProcessConfig(defaultsFileName = "testConfig.json", infoFileName = "testConfig.json")
public class ExampleNestedSettings {
    public A a;
    public B b;
    public static ExampleNestedSettings _instance;

    @ConfigClass
    public static class A{
        public String testString;
        public Integer testInt;

        public C c;

        @ConfigClass
        public static class C{
            @ConfigProperty(defualtValue = "3.1415", comment = "This is a test Double.")
            public Double testDouble;
        }
    }

    @ConfigClass
    public static class B{
        public Integer testInt;

        @ConfigProperty(defualtValue = "true", comment = "This is a test Boolean.")
        public Boolean testBool;
    }
}
