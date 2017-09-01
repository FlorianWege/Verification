package gui;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import core.Grammar;
import core.structures.LexerRule;
import core.structures.Terminal;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class ExtendedCodeAreaToken {
	private CodeArea _textArea;
	private Grammar _grammar;
	
	public ExtendedCodeAreaToken(CodeArea textArea, Grammar grammar) {
		_textArea = textArea;
		_grammar = grammar;
		
		_textArea.setParagraphGraphicFactory(new IntFunction<Node>() {
			@Override
			public Node apply(int line) {
				HBox box = new HBox(LineNumberFactory.get(_textArea).apply(line));
				
				box.setAlignment(Pos.CENTER_LEFT);
				
				return box;
			}
		});
		_textArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
				highlight();
			}
		});
	}
	
	private void highlight() {
		Set<String> keywords = new LinkedHashSet<>();

		for (Terminal rule : _grammar.getTerminals()) {
			boolean found = true;
			
			for (LexerRule pattern : rule.getRules()) {
				if (pattern.isRegEx()) {
					found = false;
				}
			}
			
			if (found) {
				keywords.add(rule.getKey().toString());
			}
		}

		Pattern pattern = Pattern.compile("\\b(" + String.join("|", keywords) + ")\\b");
		
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		
		Matcher matcher = pattern.matcher(_textArea.getText());
		
		int last = 0;
		int c = 0;
		
		while (matcher.find()) {
			String styleClass = "keyword";
			
			spansBuilder.add(Collections.emptyList(), matcher.start() - last);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			c++;
			
			last = matcher.end();
		}

		if (c > 0) {
			StyleSpans<Collection<String>> spans = spansBuilder.create();
			
			if (spans.getSpanCount() > 0) {
				_textArea.setStyleSpans(0, spans);
			}
		}
	}
}
