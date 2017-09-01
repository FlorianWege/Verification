package gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXMain extends Application {
	public interface PrintInterface {
		public void writeToOut(String s);
		public void writeToErr(String s);
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
	
	public void getClipboardContents() throws UnsupportedFlavorException, IOException, ClassNotFoundException {
		// get the clipboard contents
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		clipboard.setContents(new Transferable() {
		    @Override
			public DataFlavor[] getTransferDataFlavors() {
		      return new DataFlavor[0];
		    }

		    @Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
		      return false;
		    }

		    @Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		      throw new UnsupportedFlavorException(flavor);
		    }
		}, null);
		// dump out all available flavors
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		
		for (int i = 0; i < flavors.length; i++) {
			System.out.println(flavors[i]);
			
			DataFlavor flavor = flavors[i];//new DataFlavor("text/rtf;class=java.io.InputStream");
			
			if (flavor.isRepresentationClassInputStream()) {
				InputStream stream = (InputStream) transferable.getTransferData(flavor);
				// ... read the bytes from the stream...
				
				byte buf[] = new byte[4096];
				int c = 0;
				
				while ((c = stream.read(buf)) > 0) {
					System.out.println(new String(buf, c));
				};
				
				stream.close();
			}
		}
		// If the clipboard contains part of a Word-doc or an Excel-Sheet,
		// you'll get dozens of different flavors here, for example:
		// DataFlavor[mimetype=text/rtf;representationclass=java.io.InputStream]
		// DataFlavor[mimetype=text/rtf;representationclass=
		// DataFlavor[mimetype=text/plain;representationclass=[B;charset=Cp1252]

		// get the contents in one of the flavors (RTF InputStream)

	}
	
	private MainWindow _mainWindow = null;
	
	public interface StopInterface {
		public void onStop();
	}
	
	@Override
	public void stop() {
		_mainWindow.onStop();
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		wrapPrintStreams();

		_mainWindow = new MainWindow(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);
	}
}