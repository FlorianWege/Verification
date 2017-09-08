package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.html.HtmlEscapers;
import core.structures.Terminal;
import javafx.util.Pair;

public class StringUtil {
	public static Character bool_and = '\u2227';
	public static Character bool_or = '\u2228';
	public static Character bool_neg = '\u00AC';
	public static String html_tab = "&nbsp;";

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
		
		replacements.add(new Pair<>("||", "\\\\textbar\\\\textbar"));
		replacements.add(new Pair<>("&&", "\\\\&\\\\&"));
		replacements.add(new Pair<>(Terminal.EPSILON.toString(), "\\\\textepsilon{}"));
		
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
		return HtmlEscapers.htmlEscaper().escape(s);
	}
}