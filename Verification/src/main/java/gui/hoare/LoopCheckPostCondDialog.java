package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolNeg;
import core.structures.semantics.prog.HoareCond;
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

public class LoopCheckPostCondDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private Button _button_continue;

	private final Hoare.wlp_loop _loop;
	private final Callback _callback;
	private final HoareCond _postInvariant;

	public interface Callback {
		void result() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public LoopCheckPostCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Callback callback) throws IOException {
		super(loop._whileNode, null, loop._postCond);

		_loop = loop;
		_callback = callback;
		_postInvariant = loop._postInvariant;
		
		inflate(new File("LoopCheckPostCondDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "While (Step 2)";
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
		sb.addResult(styleCond(_postInvariant));

		sb.addStep("check p*" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q");

		HoareCond impl = new HoareCond(new BoolImpl(new BoolAnd(_postInvariant.getBoolExp(), new BoolNeg(_loop._whileNode.getBoolExp())), _postCond.getBoolExp()));

		sb.addParam("p*" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q", styleCond(impl));

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