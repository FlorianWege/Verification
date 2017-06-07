package util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOUtil {
	public static String getResourceAsString(String name) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(Paths.get(IOUtil.class.getResource(name).toURI())));
	}
}
