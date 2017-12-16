package util;

import gui.ErrorDialog;

import javax.annotation.Nonnull;
import java.io.PrintStream;
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

	public static class NestedPrinter {
		private PrintStream _stream;

		private int _nestDepth = 0;

		public void begin() {
			_nestDepth++;
		}

		public void end() {
			_nestDepth--;
		}

		public void println(String s) {
			_stream.println(StringUtil.repeat("\t", _nestDepth) + s);
		}

		public NestedPrinter(PrintStream stream) {
			_stream = stream;
		}
	}

	public final static NestedPrinter PRINTER = new NestedPrinter(System.out);
}