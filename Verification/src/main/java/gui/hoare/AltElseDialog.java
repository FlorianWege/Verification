package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AltElseDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private final Hoare.wlp_alt _alt;
	private final Callback _callback;
	private final HoareCond _firstPreCond;

	public interface Callback {
		void result() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public AltElseDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Callback callback) throws IOException {
		super(alt._altNode, null, alt._postCond);

		_alt = alt;
		_callback = callback;
		_firstPreCond = alt._thenPreCond;
		
		inflate(new File("AltElseDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Alternative (Step 2)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 4 (conditional): " + "{p" + StringUtil.bool_and + "B} S<sub>1</sub> {q}, {p" + StringUtil.bool_and + StringUtil.bool_neg + "B} S<sub>2</sub>{q}" + StringUtil.bool_impl + "{p} if B then S<sub>1</sub> else S<sub>2</sub> fi {q}");

		sb.addParam("B", styleNode(_alt._altNode.getBoolExp()));
		sb.addParam("S<sub>1</sub>", styleNode(_alt._altNode.getThenProg()));
		sb.addParam("S<sub>2</sub>", styleNode((_alt._altNode.getElseProg() != null) ? _alt._altNode.getElseProg() : new Skip()));
		sb.addParam("{q}", styleCond(_postCond));

		sb.addStep("get p" + StringUtil.bool_and + "B = wlp(S<sub>1</sub>, q)");
		sb.addResult(styleCond(_firstPreCond));
		sb.addStep("get p" + StringUtil.bool_and + StringUtil.bool_neg + "B = wlp(S<sub>2</sub>, q)");

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