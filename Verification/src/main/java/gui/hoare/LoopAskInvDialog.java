package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.prog.HoareCond;
import grammars.BoolExpGrammar;
import gui.ExtendedCodeArea;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoopAskInvDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private CodeArea _codeArea_invariant;
	@FXML
	private Button _button_suggest;

	private final Hoare.wlp_loop _loop;
	private final Callback _callback;

	private ExtendedCodeArea _extendedCodeArea_invariant;

	public interface Callback {
		void result(@Nonnull HoareCond invariant) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public LoopAskInvDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Callback callback) throws IOException {
		super(loop._whileNode, null, loop._postCond);

		_loop = loop;
		_callback = callback;

		inflate(new File("LoopAskInvDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "While (Step 1)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 5 (loop): {p" + StringUtil.bool_and + "B} S {p} " + StringUtil.bool_impl_meta + " {p} while B do S od {p" + StringUtil.bool_and + StringUtil.bool_neg + "B}");
		sb.addProse("p" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q");

		sb.addParam("B", styleNode(_loop._whileNode.getBoolExp()));
		sb.addParam("S", styleNode(_loop._whileNode.getProg()));
		sb.addParam("q", styleCond(_postCond));

		sb.addStep("get alleged invariant {p*}");

		return sb.toString();
	}

	@Override
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
		try {
			super.initialize(url, resources);

			_extendedCodeArea_invariant = new ExtendedCodeArea(_codeArea_invariant);

			_extendedCodeArea_invariant.setParser(new Parser(BoolExpGrammar.getInstance()));

			_button_suggest.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						BoolExp boolExp = (BoolExp) _extendedCodeArea_invariant.getParsing();

						if (boolExp != null) {
							HoareCond invariant = new HoareCond(boolExp);

							close();

							_callback.result(invariant);
						}
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