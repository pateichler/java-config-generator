import com.pat_eichler.config.ConfigClass;
import com.pat_eichler.config.processor.ConfigProperty;
import com.pat_eichler.config.processor.ProcessConfig;

@ConfigClass @ProcessConfig(infoFileName = "testConfig.json")
class ExampleSettings{
    @ConfigProperty(defualtValue = "5", comment = "This is a test variable.")
    public Integer testVar;

    @ConfigProperty(comment = "This is another test variable.")
    public Boolean testVar2;

    private static final boolean testBool = true;
}
