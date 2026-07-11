package workflows;

import org.testcontainers.utility.MountableFile;
import utilities.K6ReportHelper;
import java.io.File;
import com.github.dockerjava.api.model.WaitResponse;
import io.qameta.allure.Allure;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.k6.K6Container;

public class LoadTestFlow {

    public static void loadTest() {
    	
    	Allure.step("Load test flow", () -> {
	        try {
	            StringBuilder output = new StringBuilder();
	
	            @SuppressWarnings("resource")
	            K6Container k6 = new K6Container("grafana/k6:0.49.0")
	                    .withTestScript(MountableFile.forClasspathResource("load-test.js"))
	                    .withScriptVar("TARGET_URL", System.getenv("URL"))
	                    .withEnv("K6_LOG_OUTPUT", "stdout") //Run container output in the console
	                    .withReuse(false)
	                    .withLogConsumer(frame -> {
	                        String line = frame.getUtf8String();
	                        System.out.print(line);
	                        output.append(line);
	                    });
	
	            File outDir = new File("k6_results");
	            if (!outDir.exists()) {
	                outDir.mkdirs();
	            }
	            // Create docker client
	            DockerClient dockerClient = DockerClientFactory.instance().client();
	
	            k6.start();
	
	            // Wait until the k6 container finishes
	            dockerClient.waitContainerCmd(k6.getContainerId())
	                    .exec(new ResultCallback.Adapter<WaitResponse>() {})
	                    .awaitCompletion();
	
	            // Pass k6 log output to the report helper
	            K6ReportHelper.generateHtmlFromString(output.toString());
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
    	});
    }
}
