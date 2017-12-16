package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AltMergeDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private Button _button_continue;

	private final Hoare.wlp_alt _alt;
	private final Callback _callback;
	private final HoareCond _thenPreCond;
	private final HoareCond _elsePreCond;

	public interface Callback {
		void result() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public AltMergeDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Callback callback) throws IOException {
		super(alt._altNode, alt._preCond, alt._postCond);

		_alt = alt;
		_callback = callback;
		_thenPreCond = alt._thenPreCond;
		_elsePreCond = alt._elsePreCond;
		
		inflate(new File("AltThenDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Alternative (Step 3)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 4 (conditional): " + "{p" + StringUtil.bool_and + "B} S<sub>1</sub> {q}, {p" + StringUtil.bool_and + StringUtil.bool_neg + "B} S<sub>2</sub>{q}" + StringUtil.bool_impl_meta + "{p} if B then S<sub>1</sub> else S<sub>2</sub> fi {q}");

		sb.addParam("B", styleNode(_alt._altNode.getBoolExp()));
		sb.addParam("S<sub>1</sub>", styleNode(_alt._altNode.getThenProg()));
		sb.addParam("S<sub>2</sub>", styleNode((_alt._altNode.getElseProg() != null) ? _alt._altNode.getElseProg() : new Skip()));
		sb.addParam("{q}", styleCond(_postCond));

		sb.addStep("get p" + StringUtil.bool_and + "B = wlp(S<sub>1</sub>, q)");
		sb.addResult(styleCond(_thenPreCond));
		sb.addStep("get p" + StringUtil.bool_and + StringUtil.bool_neg + "B = wlp(S<sub>2</sub>, q)");
		sb.addResult(styleCond(_elsePreCond));
		sb.addStep("p = (p" + StringUtil.bool_and + "B)" + StringUtil.bool_or + "(p" + StringUtil.bool_and + StringUtil.bool_neg + "B)");

		sb.addOutput();

		return sb.toString();
	}

	@Override
	public void initialize(@Nonnull URL url, @Nullable ResourceBundle resources) {
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