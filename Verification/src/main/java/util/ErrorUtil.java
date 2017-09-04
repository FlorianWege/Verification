package util;

import java.io.IOException;

import gui.ErrorDialog;

public class ErrorUtil {
	public static void log(String msg) {
		System.out.println(msg);
	}
	
	public static void logE(String msg) {
		System.err.println(msg);
	}
	
	public static void logE(Exception e) {
		e.printStackTrace();
	}
	
	public static void logEFX(Exception e) {
		logE(e);
		
		ErrorDialog diag = new ErrorDialog(e);
		
		diag.show();
	}
}