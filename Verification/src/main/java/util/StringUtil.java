package util;

public class StringUtil {
	public static String repeat(String s, int repeat) {
		return new String(new char[repeat]).replace("\0", s);
	}
}