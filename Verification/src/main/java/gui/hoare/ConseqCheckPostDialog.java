package gui.hoare;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.prog.HoareCond;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConseqCheckPostDialog extends HoareDialog implements Initializable {
	@FXML
	private Pane _pane_tableHost;

	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;

	private final HoareCond _origPostCond;
	private final HoareCond _newPostCond;
	private final Callback _callback;

	private final BoolAnd _origPostCNF;
	private final BoolOr _origPostDNF;
	private final BoolAnd _newPostCNF;
	private final BoolOr _newPostDNF;

	private final BoolImpl _impl;
	private final BoolExp _split;

	public interface Callback {
		void result(BoolExp boolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public ConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, Callback callback) throws IOException {
		super(node, null, null);

		HoareCond tmpCond = origPostCond;

		origPostCond = newPostCond;
		newPostCond = tmpCond;

		_origPostCond = origPostCond;
		_newPostCond = newPostCond;
		_callback = callback;

		_origPostCNF = _origPostCond.getBoolExp().makeCNF();
		_origPostDNF = _origPostCond.getBoolExp().makeDNF();

		_newPostCNF = _newPostCond.getBoolExp().makeCNF();
		_newPostDNF = _newPostCond.getBoolExp().makeDNF();

		_impl = new BoolImpl(_origPostCond.getBoolExp(), _newPostCond.getBoolExp());

		_split = _impl.split(true, false);

		inflate(new File("ConseqCheckPostDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Consequence Check (Post)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 6 (consequence): p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q<sub>0</sub>}, q<sub>0</sub>" + StringUtil.bool_impl + "q " + StringUtil.bool_impl_meta + "{p} S {q}");
		sb.addProse("with p" + StringUtil.bool_eq + "p<sub>0</sub>: p" + "{p} S {q<sub>0</sub>}, q<sub>0</sub>" + StringUtil.bool_impl + "q " + StringUtil.bool_impl_meta + "{p} S {q}");

		sb.addParam("q<sub>0</sub>", styleCond(_origPostCond));
		sb.addParam("q", styleCond(_newPostCond));

		/*sb.addStep("Does q<sub>0</sub> imply q? (reduce them)");

		sb.addParam("q<sub>0</sub>'", styleCond(_origPostCond.getBoolExp().reduce()));
		sb.addParam("q'", styleCond(_newPostCond.getBoolExp().reduce()));*/

		sb.addStep("transform left side to CNF, right side to DNF");

		sb.addParam("CNF(q<sub>0</sub>')", styleCond(_origPostCNF));
		sb.addParam("DNF(q')", styleCond(_newPostDNF));

		sb.addStep("merge to implication");

		sb.addParam("impl", styleCond(_impl));

		sb.addStep("split implication");

		sb.addParam("split", styleCond(_split));

		sb.addParam("CNF(q<sub>0</sub>)", styleCond(_origPostCNF));
		sb.addParam("CNF(q)", styleCond(_newPostCNF));

		sb.addParam("DNF(q<sub>0</sub>)", styleCond(_origPostDNF));
		sb.addParam("DNF(q)", styleCond(_newPostDNF));

		return sb.toString();
	}

	@Override
	public void initialize(URL url, ResourceBundle resources) {
		try {
			super.initialize(url, resources);

			_button_yes.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						_callback.result(new BoolLit(true));
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});

			_button_no.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						_callback.result(new BoolLit(false));
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});

			ConseqCheckTableView tableView = new ConseqCheckTableView(new ConseqCheckTableView.Callback() {
				@Override
				public void result(BoolExp boolExp) {
					try {
						_callback.result(boolExp);
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});

			_pane_tableHost.getChildren().add(tableView.getRoot());

			tableView.setBoolImpl(_impl);
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}