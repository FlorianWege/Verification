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

import gui.MainWindow;
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
		URL url = MainWindow.class.getResource(file.toString());
		
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
		} catch (Exception e) {
			throw e;
		} finally {
			System.setErr(errStream);
		}
		
		Parent root = loader.getRoot();
		
		Scene scene = new Scene(root);
		
		return scene;
	}
}
