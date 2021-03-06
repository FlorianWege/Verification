package gui.hoare;

import core.Hoare;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.StyleClassedTextArea;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CompMergeDialog extends HoareDialog implements gui.Initializable {
	@FXML
	private StyleClassedTextArea _textArea_output;
	@FXML
	private Button _button_continue;

	private final Hoare.wlp_comp _comp;
	private final Hoare.CompMerge_callback _callback;
	
	public CompMergeDialog(@Nonnull Hoare.wlp_comp comp, @Nonnull Hoare.CompMerge_callback callback) throws IOException {
		super(comp._compNode, comp._preCond, comp._postCond);

		_comp = comp;
		_callback = callback;
		
		inflate(new File("CompMergeDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Composition (Merge)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 3 (composition): {p} S<sub>i</sub> {r<sub>i+1</sub>}, {r<sub>i+1</sub>} S<sub>i+1</sub> {q} " + StringUtil.bool_impl_meta + " {p} S<sub>i</sub>; S<sub>i+1</sub> {q}");

		for (int i = 0; i < _comp._compNode.getChildren().size(); i++) {
			sb.addParam("S<sub>" + (i + 1) + "</sub>", styleNode(_comp._compNode.getChildren().get(i)));
		}

		sb.addParam("q", styleCond(_postCond));

		for (int i = _comp._compNode.getChildren().size() - 1; i >= _comp._curPart; i--) {
			if (i < _comp._compNode.getChildren().size() - 1 && i >= _comp._curPart) {
				sb.addResult(styleCond(_comp._preConds[i + 1]));
			}

			String preCondS = (i != 0) ? "r<sub>" + (_comp._compNode.getChildren().size() - i) + "</sub>" : "p";
			String postCondS = (i != _comp._compNode.getChildren().size() - 1) ? "r<sub>" + (_comp._compNode.getChildren().size() - i - 1) + "</sub>" : "q";

			sb.addStep("get " + preCondS + " = wlp(S<sub>" + (_comp._compNode.getChildren().size() - i) + "</sub>, " + postCondS + ")");
		}

		sb.addResult(styleCond(_comp._preConds[0]));

		sb.addStep("merge/continue");

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