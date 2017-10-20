package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AssignDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private final Assign _assign;
	private final Callback _callback;

	public interface Callback {
		void result() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public AssignDialog(@Nonnull Assign assign, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Callback callback) throws IOException {
		super(assign, preCond, postCond);

		_assign = assign;
		_callback = callback;
		
		inflate(new File("AssignDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Variable Assignment";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare axiom 2 (assignment): {p[u:=t]} u:=t {p}");

		sb.addParam("p", styleCond(_postCond));
		sb.addParam("u", styleNode(_assign.getVar()));
		sb.addParam("t", styleNode(_assign.getExp()));

		sb.addStep("replace occurences of u in p by t");

		sb.addOutput(new IOUtil.BiFunc<SemanticNode, String, String>() {
			@Override
			public String apply(SemanticNode semanticNode, String s) {
				if (semanticNode.equals(_assign.getExp())) {
					return "<span class='output-highlight'>" + s + "</span>";
				}

				return s;
			}
		}, new IOUtil.BiFunc<SemanticNode, String, String>() {
			@Override
			public String apply(SemanticNode semanticNode, String s) {
				return s;
			}
		}, new IOUtil.BiFunc<SemanticNode, String, String>() {
			@Override
			public String apply(SemanticNode semanticNode, String s) {
				if (semanticNode.equals(_assign.getVar())) {
					return "<span class='output-highlight'>" + s + "</span>";
				}

				return s;
			}
		});

		return sb.toString();
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			super.initialize(url, resources);

			_button_continue.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						_callback.result();
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}