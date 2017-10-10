package gui.hoare;

import core.Hoare;
import core.structures.semantics.boolExp.HoareCond;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.prog.Prog;
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

public class LoopGetBodyCondDialog extends HoareDialog implements Initializable {
	@FXML
	private Button _button_continue;

	private final Hoare.Executer.wlp_loop _loop;
	private final Hoare.Executer.LoopGetBodyCond_callback _callback;
	private final HoareCond _postInvariant;

	public LoopGetBodyCondDialog(@Nonnull Hoare.Executer.wlp_loop loop, @Nonnull Hoare.Executer.LoopGetBodyCond_callback callback) throws IOException {
		super(loop._whileNode, null, loop._postCond);

		_loop = loop;
		_callback = callback;
		_postInvariant = loop._postInvariant;
		
		inflate(new File("LoopGetBodyCondDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "While (Step 3)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 5 (loop): {p" + StringUtil.bool_and + "B} S {p}" + StringUtil.bool_impl + "{p} while B do S od {p" + StringUtil.bool_and + StringUtil.bool_neg + "B}");
		sb.addProse("p" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q");

		sb.addParam("B", styleNode(_loop._whileNode.getBoolExp()));
		sb.addParam("S", styleNode(_loop._whileNode.getProg()));
		sb.addParam("q", styleCond(_postCond));

		sb.addStep("get alleged invariant {p*}");
		sb.addResult(styleCond(_postInvariant));

		sb.addStep("check p" + StringUtil.bool_and + StringUtil.bool_neg + "B" + StringUtil.bool_impl + "q");
		sb.addResult("true");

		sb.addStep("get wlp(S, p*)");

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