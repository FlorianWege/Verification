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

public class ConseqCheckPreDialog extends HoareDialog implements Initializable {
	@FXML
	private Pane _pane_tableHost;

	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;

	private final HoareCond _origPreCond;
	private final HoareCond _newPreCond;
	private final Callback _callback;

	private final BoolAnd _origPreCNF;
	private final BoolOr _origPreDNF;
	private final BoolAnd _newPreCNF;
	private final BoolOr _newPreDNF;

	private final BoolImpl _impl;
	private final BoolExp _split;

	public interface Callback {
		void result(BoolExp boolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException;
	}

	public ConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, Callback callback) throws IOException {
		super(node, null, null);

		_origPreCond = origPreCond;
		_newPreCond = newPreCond;
		_callback = callback;

		_origPreCNF = _origPreCond.getBoolExp().makeCNF();
		_origPreDNF = _origPreCond.getBoolExp().makeDNF();

		_newPreCNF = _newPreCond.getBoolExp().makeCNF();
		_newPreDNF = _newPreCond.getBoolExp().makeDNF();

		_impl = new BoolImpl(_origPreCond.getBoolExp(), _newPreCond.getBoolExp());

		_split = _impl.split(true, false);

		inflate(new File("ConseqCheckPreDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Consequence Check (Pre)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 6 (consequence): p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q<sub>0</sub>}, q<sub>0</sub> " + StringUtil.bool_impl + "q" + StringUtil.bool_impl_meta + "{p} S {q}");
		sb.addProse("with q<sub>0</sub>" + StringUtil.bool_eq + "q: p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q} " + StringUtil.bool_impl_meta + "{p} S {q}");

		sb.addParam("p", styleCond(_newPreCond));
		sb.addParam("p<sub>0</sub>", styleCond(_origPreCond));

		sb.addStep("Does p imply p<sub>0</sub>?");

		sb.addParam("CNF(p)", styleCond(_newPreCNF));
		sb.addParam("CNF(p<sub>0</sub>)", styleCond(_origPreCNF));

		sb.addParam("DNF(p)", styleCond(_newPreDNF));
		sb.addParam("DNF(p<sub>0</sub>)", styleCond(_origPreDNF));

		sb.addStep("merge to implication");

		sb.addParam("impl", styleCond(_impl));

		sb.addStep("split implication");

		sb.addParam("split", styleCond(_split));

		BoolOr splitOr = new BoolOr(_split);

		for (BoolExp orPart : splitOr.getBoolExps()) {
			BoolAnd splitAnd = new BoolAnd(orPart);

			for (BoolExp andPart : splitAnd.getBoolExps()) {

			}
		}

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