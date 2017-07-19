package util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import gui.JavaFXMain;
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
	
	public static Scene inflateFXML(File file, Object controller) throws IOException {
		URL url = JavaFXMain.class.getResource(file.toString());
		
		if (url == null) throw new IOException(file.toString() + " not found");
		
		FXMLLoader loader = new FXMLLoader(url);
		
		loader.setController(controller);
		
		loader.load();
		
		Parent root = loader.getRoot();
		
		Scene scene = new Scene(root);
		
		return scene;
	}
}
