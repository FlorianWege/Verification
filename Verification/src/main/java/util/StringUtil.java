package util;

import com.google.common.html.HtmlEscapers;
import core.structures.Terminal;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {
	public static final String bool_and = "\u2227";
	public static final String bool_or = "\u2228";
	public static final String bool_neg = "\u00AC";
	public static final String bool_impl = "\u2192";
	public static final String bool_eq = "\u2194";
	public static final String bool_impl_meta = "\u21D2";
	public static final String bool_eq_meta = "\u21D4";
	public static final String html_tab = "&nbsp;";
	public static final String html_line_sep = "<br>";
	public static final String line_sep = System.lineSeparator();

	public static String repeat(String s, int repeat) {
		if (repeat < 1) return "";
		
		return new String(new char[repeat]).replace("\0", s);
	}
	
	public static String latexify(String s) {
		List<Pair<String, String>> replacements = new ArrayList<>();
		
		replacements.add(new Pair<>("_", "\\\\textunderscore "));
		replacements.add(new Pair<>("{", "\\\\{ "));
		replacements.add(new Pair<>("}", "\\\\} "));
		
		replacements.add(new Pair<>("^", "\\\\^{}"));
		
		replacements.add(new Pair<>("|", "\\\\textbar"));
		replacements.add(new Pair<>("&", "\\\\&"));
		replacements.add(new Pair<>(Terminal.EPSILON.toString(), "\\\\straightepsilon{}"));
		
		replacements.add(new Pair<>("NUM", "num"));
		replacements.add(new Pair<>("ID", "id"));
		
		replacements.add(new Pair<>("<", "\\\\textless{}"));
		replacements.add(new Pair<>(">", "\\\\textgreater{}"));
		replacements.add(new Pair<>("~", "\\\\textasciitilde{}"));
		
		for (Pair<String, String> replacement : replacements) {
			s = s.replaceAll(Pattern.quote(replacement.getKey()), replacement.getValue());
		}
		
		return s;
	}

	public static String escapeHTML(String s) {
		String ret = HtmlEscapers.htmlEscaper().escape(s);

		return ret.replaceAll(StringUtil.line_sep, "<br>");
	}
}