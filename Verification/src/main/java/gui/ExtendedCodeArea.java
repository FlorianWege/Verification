package gui;

import core.Grammar;
import core.Token;
import core.structures.LexerRule;
import core.structures.Terminal;
import core.structures.semantics.SemanticNode;
import grammars.HoareWhileGrammar;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.*;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedCodeArea {
	private final CodeArea _textArea;
	private final ObjectProperty<SemanticNode> _currentNodeP;
	private final ObjectProperty<SemanticNode> _currentHoareNodeP;
	private final Type _type;

	private final Grammar _grammar = HoareWhileGrammar.getInstance();
	private final IntFunction<Node> _numFactory;
	private final IntFunction<Node> _arrowFactory;

	public enum Type {
		CODE,
		TOKEN,
		HOARE
	}

	public ExtendedCodeArea(@Nonnull CodeArea textArea, @Nullable ObjectProperty<SemanticNode> currentNodeP, @Nullable ObjectProperty<SemanticNode> currentHoareNodeP, @Nonnull Type type) {
		_textArea = textArea;
		_currentNodeP = currentNodeP;
		_currentHoareNodeP = currentHoareNodeP;
		_type = type;

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
		/*_textArea.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Function<String, String> mapFunc = new Function<String, String>() {
					@Override
					public String apply(String s) {
						if (s.equals("&")) return StringUtil.bool_and;
						if (s.equals("|")) return StringUtil.bool_or;
						if (s.equals("~")) return StringUtil.bool_neg;

						return s;
					}
				};

				String input = event.getCharacter();
				String output = mapFunc.apply(input);

				if (!output.equals(input)) {
					event.consume();

					_textArea.insertText(_textArea.getCaretPosition(), output);
				}
			}
		});*/
		_textArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
				int caretPos = _textArea.getCaretPosition();

				String text = newVal.replaceAll(Pattern.quote("&") + "+", StringUtil.bool_and).replaceAll(Pattern.quote("|") + "+", StringUtil.bool_or).replaceAll("~", StringUtil.bool_neg);

				if (!text.equals(newVal)) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							_textArea.positionCaret(caretPos);
						}
					});
				}

				highlight();
				
				setErrorPos(null);
			}
		});

		_numFactory = (_type == Type.HOARE && _currentNodeP != null) ? LineNumberFactoryEx.get(_textArea, _currentNodeP) : LineNumberFactory.get(_textArea);
		_arrowFactory = (_type == Type.CODE && _currentNodeP != null && _currentHoareNodeP != null) ? new IntFunction<Node>() {
				private Node makeCurrentNodeTriangle(int line) {
					Polygon triangle = new Polygon(0D, 0D, 10D, 5D, 0D, 10D);

					triangle.setFill(Color.RED);

					ObservableValue<Boolean> vis = Val.map(_currentNodeP, new Function<SemanticNode, Boolean>() {
						@Override
						public Boolean apply(SemanticNode node) {
							if (node == null) return false;

							List<Token> tokens = node.tokenize();

							if (tokens.isEmpty()) return false;

							Token firstToken = tokens.get(0);
							Token lastToken = tokens.get(tokens.size() - 1);

							return (line >= firstToken.getLine() && line <= lastToken.getLine());
						}
					});

					triangle.visibleProperty().bind(vis);

					return triangle;
				}

				private Node makeCurrentHoareNodeTriangle(int line) {
					Polygon triangle = new Polygon(0D, 0D, 10D, 5D, 0D, 10D);

					triangle.setFill(Color.LIGHTBLUE);

					ObservableValue<Boolean> vis = Val.map(_currentHoareNodeP, new Function<SemanticNode, Boolean>() {
						@Override
						public Boolean apply(SemanticNode node) {
							if (node == null) return false;

							List<Token> tokens = node.tokenize();

							Token firstToken = tokens.get(0);
							Token lastToken = tokens.get(tokens.size() - 1);

							return (line == firstToken.getLine() || line == lastToken.getLine());
						}
					});

					triangle.visibleProperty().bind(vis);

					return triangle;
				}

				@Override
				public Node apply(int line) {
					StackPane stackPane = new StackPane();

					stackPane.getChildren().add(makeCurrentNodeTriangle(line));
					stackPane.getChildren().add(makeCurrentHoareNodeTriangle(line));

					return stackPane;
				}
			} : null;

		_textArea.setParagraphGraphicFactory(new IntFunction<Node>() {
			@Override
			public Node apply(int line) {
				HBox box = new HBox();

				if (_numFactory != null) box.getChildren().add(_numFactory.apply(line));
				if (_arrowFactory != null) box.getChildren().add(_arrowFactory.apply(line));

				box.setAlignment(Pos.CENTER_LEFT);

				return box;
			}
		});
	}

	private static class LineNumberFactoryEx implements IntFunction<Node> {
		private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
		private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
		private static final Font DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13);
		private static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));

		public static @Nonnull IntFunction<Node> get(@Nonnull StyledTextArea<?> area, @Nonnull ObjectProperty<SemanticNode> currentNodeP) {
			return get(area, digits -> "%0" + digits + "d", currentNodeP);
		}

		public static @Nonnull IntFunction<Node> get(@Nonnull StyledTextArea<?> area, @Nonnull IntFunction<String> format, @Nonnull ObjectProperty<SemanticNode> currentNodeP) {
			return new LineNumberFactoryEx(area, format, currentNodeP);
		}

		private final Val<Integer> nParagraphs;
		private final IntFunction<String> format;
		private final ObjectProperty<SemanticNode> _currentNodeP;

		private LineNumberFactoryEx(@Nonnull StyledTextArea<?> area, @Nonnull IntFunction<String> format, @Nonnull ObjectProperty<SemanticNode> currentNodeP) {
			nParagraphs = LiveList.sizeOf(area.getParagraphs());
			this.format = format;
			_currentNodeP = currentNodeP;
		}

		@Override
		public Node apply(int idx) {
			Val<String> formatted = Val.map(_currentNodeP, new Function<SemanticNode, String>() {
				@Override
				public String apply(SemanticNode syntaxNode) {
					List<Token> tokens = syntaxNode.tokenize();

					if (tokens.isEmpty()) return LineNumberFactoryEx.this.format(0, 1);

					Token firstToken = tokens.get(0);
					Token lastToken = tokens.get(tokens.size() - 1);

					return LineNumberFactoryEx.this.format(firstToken.getLine() + idx + 1, lastToken.getLine() + 1);
				}
			});

			Label lineNo = new Label();
			lineNo.setFont(DEFAULT_FONT);
			lineNo.setBackground(DEFAULT_BACKGROUND);
			lineNo.setTextFill(DEFAULT_TEXT_FILL);
			lineNo.setPadding(DEFAULT_INSETS);
			lineNo.getStyleClass().add("lineno");

			// bind label's text to a Val that stops observing area's paragraphs
			// when lineNo is removed from scene
			lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

			return lineNo;
		}

		private String format(int x, int max) {
			int digits = (int) Math.floor(Math.log10(max)) + 1;

			return String.format(format.apply(digits), x);
		}
	}

	private Integer _errorPos = null;
	
	public void setErrorPos(@Nullable Integer pos, boolean requestFocus) {
		_errorPos = pos;
		
		highlight();

		if (requestFocus) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					_textArea.positionCaret(pos);
					_textArea.requestFocus();
				}
			});
		}
	}

	public void setErrorPos(@Nullable Integer pos) {
		setErrorPos(pos, false);
	}

	private void highlight() {
		Set<String> keywords = new LinkedHashSet<>();

		for (Terminal terminal : _grammar.getTerminals()) {
			if (!terminal.isKeyword()) continue;

			for (LexerRule rule : terminal.getRules()) {
				keywords.add(Pattern.quote(rule.toString()));
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

		//_textArea.setStyle(0, _textArea.getName().length() - 1, Collections.singleton("styled-text-area"));
	}
}