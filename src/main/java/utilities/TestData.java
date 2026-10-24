package utilities;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

public class TestData {  
    
    @DataProvider(name = "globalProvider")
    public static Object[][] data(Method method) {
        try {
        	String path = Paths.get("src", "test", "resources", "testData.json").toString();
        	return DataUtil.getTestData(path, method.getName());      	
            
        } catch (Exception e) {
            throw new SkipException("No data found for method: " + method.getName() + " → " + e.getMessage());
        }
    }
}
