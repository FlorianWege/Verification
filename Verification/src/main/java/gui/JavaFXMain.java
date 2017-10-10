package gui;

import java.io.IOException;
import java.io.PrintStream;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.annotation.Nonnull;

public class JavaFXMain extends Application {
	public interface PrintInterface {
		void writeToOut(String s);
		void writeToErr(String s);
	}
	
	private void wrapPrintStreams() {
		PrintStream stdOut = System.out;
		PrintStream stdErr = System.err;
		
		PrintStream printStreamOut = new PrintStream(stdOut) {
			@Override
			public void write(byte[] buf, int off, int len) {
				if (_mainWindow != null) _mainWindow.writeToOut(new String(buf, off, len));
				
				super.write(buf, off, len);
			}
		};
		
		PrintStream printStreamErr = new PrintStream(stdErr) {
			@Override
			public void write(byte[] buf, int off, int len) {
				if (_mainWindow != null) _mainWindow.writeToErr(new String(buf, off, len));
				
				super.write(buf, off, len);
			}
		};

		System.setOut(printStreamOut);
		System.setErr(printStreamErr);
	}
	
	public interface StopInterface {
		void onStop();
	}

	private MainWindow _mainWindow = null;

	@Override
	public void stop() {
		_mainWindow.onStop();
	}
	
	@Override
	public void start(@Nonnull Stage primaryStage) throws IOException {
		wrapPrintStreams();

		_mainWindow = new MainWindow(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);
	}
}