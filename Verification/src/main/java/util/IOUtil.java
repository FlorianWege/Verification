package util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtil {
	public static String getResourceAsString(String name) throws IOException, URISyntaxException {
		URI uri = IOUtil.class.getClassLoader().getResource(name).toURI();
		
		Path path = Paths.get(uri);
		
		byte[] bytes = Files.readAllBytes(path);
		
		return new String(bytes);
	}
}
