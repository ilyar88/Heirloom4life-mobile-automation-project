package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.openqa.selenium.json.Json;

public class DataUtil {

	private static final Properties dotEnv = loadDotEnv();

	// Values loaded from the local .env file, so callers (e.g. WebActions) can mask secrets in logs.
	public static Collection<Object> dotEnvValues() {
		return dotEnv.values();
	}

	@SuppressWarnings("unchecked")
	public static Object[][] getTestData(String filePath, String testName) throws IOException {
		Map<String, Object> allData = new Json().toType(
				new String(Files.readAllBytes(Paths.get(filePath))), Json.MAP_TYPE);

		Object cases = allData.get(testName);
		if (!(cases instanceof List)) return new Object[0][];

		List<Object[]> dataList = new ArrayList<>();
		for (Object testCase : (List<Object>) cases) {
			List<Object> rawParams = (List<Object>) testCase;
			Object[] params = new Object[rawParams.size()];
			for (int i = 0; i < rawParams.size(); i++) params[i] = resolveValue(rawParams.get(i));
			dataList.add(params);
		}
		return dataList.toArray(new Object[0][]);
	}

	// A JSON array parameter becomes a resolved String[]; a JSON string is resolved as-is.
	@SuppressWarnings("unchecked")
	private static Object resolveValue(Object value) {
		if (value instanceof List) {
			return ((List<Object>) value).stream()
					.map(v -> resolveEnv(String.valueOf(v))).toArray(String[]::new);
		}
		return resolveEnv(String.valueOf(value));
	}

	// Convert ${NAME} placeholders to their value from the local .env file or a JVM system
	// property (e.g. CI secrets); non-placeholder values are returned unchanged (used by
	// BasePage to resolve every property loaded from config.properties).
	public static String resolveEnv(String value) {
		if (value == null || !value.startsWith("${") || !value.endsWith("}")) return value;
		String key = value.substring(2, value.length() - 1);
		String resolved = dotEnv.getProperty(key);
		return resolved != null ? resolved : System.getProperty(key, value);
	}

	private static Properties loadDotEnv() {
		Properties props = new Properties();
		Path envFile = Paths.get(System.getProperty("user.dir"), ".env");
		if (Files.exists(envFile)) {
			try (InputStream input = Files.newInputStream(envFile)) {
				props.load(input);
			} catch (IOException ignored) {
				// Unreadable .env: fall back to empty, so placeholders stay unresolved.
			}
		}
		return props;
	}
}
