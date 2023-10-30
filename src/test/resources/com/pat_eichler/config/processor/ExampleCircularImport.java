import com.pat_eichler.config.ConfigClass;
import com.pat_eichler.config.processor.ConfigProperty;
import com.pat_eichler.config.processor.ProcessConfig;

@ConfigClass @ProcessConfig(defaultsFileName = "test.json")
class ExampleCircularImport{
    public Double test1;
    public String test2;
    public A a;
    @ConfigClass
    public static class A{
        public Integer test1;
        public B b;
        @ConfigClass
        public static class B{
            public Boolean test1;
            public ExampleCircularImport e;
        }
    }
}