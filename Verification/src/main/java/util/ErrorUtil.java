package util;

import gui.ErrorDialog;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtil {
	public static void log(String msg) {
		System.out.println(msg);
	}
	
	public static void logE(String msg) {
		System.err.println(msg);
	}
	
	public static void logE(@Nonnull Exception e) {
		StringWriter stringWriter = new StringWriter();

		PrintWriter printWriter = new PrintWriter(stringWriter);

		e.printStackTrace(printWriter);

		System.out.println("error: " + stringWriter.toString());
	}
	
	public static void logEFX(@Nonnull Exception e) {
		logE(e);
		
		ErrorDialog diag = new ErrorDialog(e);
		
		diag.show();
	}
}
