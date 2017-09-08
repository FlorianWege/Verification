package util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class IOUtil {
	public static String getResourceAsString(String name) throws IOException, URISyntaxException {
		URI uri = IOUtil.class.getClassLoader().getResource(name).toURI();
		
		Path path = Paths.get(uri);
		
		byte[] bytes = Files.readAllBytes(path);
		
		return new String(bytes);
	}

	private static Map<File, URL> _urlMap = new LinkedHashMap<>();

	public static Scene inflateFXML(File file, Object controller) throws IOException {
		if (!_urlMap.containsKey(file)) _urlMap.put(file, controller.getClass().getResource(file.toString()));

		URL url = _urlMap.get(file);
		
		if (url == null) throw new IOException(file.toString() + " not found");
		
		FXMLLoader loader = new FXMLLoader(url);
		
		loader.setController(controller);
		
		PrintStream errStream = System.err;
		
		try {
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
			
			loader.load();
		} finally {
			System.setErr(errStream);
		}
		
		Parent root = loader.getRoot();
		
		return new Scene(root);
	}
}