package gui;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.value.Val;

import core.Grammar;
import core.SyntaxTreeNode;
import core.Token;
import core.structures.LexerRule;
import core.structures.Terminal;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class ExtendedCodeArea {
	private CodeArea _textArea;
	private Grammar _grammar;
	
	public ExtendedCodeArea(CodeArea textArea, Grammar grammar) {
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
		_textArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.TAB)) {
					IndexRange indexRange = _textArea.getSelection();
					
					if (indexRange.getLength() < 1) return;
					
					int start = indexRange.getStart();
					int end = indexRange.getEnd();
					int caretPos = _textArea.getCaretPosition();
					
					if (e.isShiftDown()) {
						int i = end;
						int c = 0;
						
						while (i >= start) {
							if ((i == 0) || (_textArea.getText(i - 1, i).matches("\n") && _textArea.getText(i, i + 1).matches("\t"))) {
								_textArea.replaceText(i, i + 1, "");
								c++;
							}
							
							i--;
						}
						
						while ((start > 0) && !_textArea.getText(start - 1, start).matches("\n")) {
							start--;
						}
						
						_textArea.selectRange(start, end - c);
					} else {
						Pattern pattern = Pattern.compile("\n");
						
						Matcher matcher = pattern.matcher(_textArea.getText(start, end));
						
						_textArea.insertText(start, "\t");
						int c = 1;
						int c2 = 0;
						
						while (matcher.find()) {
							_textArea.insertText(start + c + matcher.end(), "\t");
							c++;
							
							if (matcher.end() < caretPos) {
								c2++;
							}
						}
						
						System.out.println(_textArea.getText(start, start));

						while ((start > 0) && !_textArea.getText(start - 1, start).matches("\n")) {
							start--;
						}
						
						_textArea.selectRange(start, end + c);
						_textArea.positionCaret(caretPos + c2);
					}
					
					e.consume();
				}
			}
		});
		_textArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
				highlight();
				
				setErrorPos(null);
			}
		});
	}
	
	private Integer _errorPos = null;
	
	public void setErrorPos(Integer pos) {
		_errorPos = pos;
		
		highlight();
	}
	
	private void highlight() {
		Set<String> keywords = new LinkedHashSet<>();

		for (Terminal rule : _grammar.getTerminals()) {
			for (LexerRule pattern : rule.getRules()) {
				if (!pattern.isRegEx()) {
					keywords.add(Pattern.quote(pattern.toString()));
					
					break;
				}
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
		
		_textArea.clearStyle(0, _textArea.getText().length());
		
		if (c > 0) {
			StyleSpans<Collection<String>> spans = spansBuilder.create();
			
			if (spans.getSpanCount() > 0) {
				_textArea.setStyleSpans(0, spans);
			}
		}
		
		if (_errorPos != null) {
			_textArea.setStyle(_errorPos, _textArea.getText().length(), Collections.singleton("error"));
		}
		
		final ObjectProperty<Integer> lineP = new SimpleObjectProperty<>();
		
		lineP.set(2);
		
		IntFunction<Node> numFactory = LineNumberFactory.get(_textArea);
		IntFunction<Node> arrowFactory = new IntFunction<Node>() {
			@Override
			public Node apply(int line) {
				Polygon triangle = new Polygon(0D, 0D, 10D, 5D, 0D, 10D);
				
				triangle.setFill(Color.RED);
				
				ObservableValue<Boolean> vis = Val.map(_currentNodeP, new Function<SyntaxTreeNode, Boolean>() {
					@Override
					public Boolean apply(SyntaxTreeNode node) {
						if (node == null) return false;
						
						List<Token> tokens = node.tokenize();
						
						Token firstToken = tokens.get(0);
						Token lastToken = tokens.get(tokens.size() - 1);
						
						return (line >= firstToken.getLine() && line <= lastToken.getLine());
					}
				});
				
				triangle.visibleProperty().bind(vis);
				
				return triangle;
			}
		};
		
		_textArea.setParagraphGraphicFactory(new IntFunction<Node>() {
			@Override
			public Node apply(int line) {
				HBox box = new HBox(numFactory.apply(line), arrowFactory.apply(line));
				
				box.setAlignment(Pos.CENTER_LEFT);
				
				return box;
			}
		});

		//_textArea.setStyle(0, _textArea.getText().length() - 1, Collections.singleton("styled-text-area"));
	}
	
	private ObjectProperty<SyntaxTreeNode> _currentNodeP = new SimpleObjectProperty<>();
	
	public void setCurrentNode(SyntaxTreeNode node) {
		_currentNodeP.set(node);
	}
}