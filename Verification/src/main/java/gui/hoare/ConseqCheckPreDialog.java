package gui.hoare;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolAnd;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolOr;
import core.structures.semantics.boolExp.HoareCond;
import core.structures.syntax.SyntaxNode;
import grammars.BoolExpGrammar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;

public class ConseqCheckPreDialog extends HoareDialog implements Initializable {
	@FXML
	private Tab _tab_cnf;
	@FXML
	private Tab _tab_dnf;

	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;

	private final HoareCond _origPreCond;
	private final HoareCond _newPreCond;
	private final Hoare.Executer.ConseqCheck_callback _callback;

	private final BoolAnd _origPreCNF;
	private final BoolOr _origPreDNF;
	private final BoolAnd _newPreCNF;
	private final BoolOr _newPreDNF;

	public ConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull Hoare.Executer.ConseqCheck_callback callback) throws IOException {
		super(node, null, null);

		_origPreCond = origPreCond;
		_newPreCond = newPreCond;
		_callback = callback;

		_origPreCNF = _origPreCond.getBoolExp().makeCNF();
		_origPreDNF = _origPreCond.getBoolExp().makeDNF();

		_newPreCNF = _newPreCond.getBoolExp().makeCNF();
		_newPreDNF = _newPreCond.getBoolExp().makeDNF();

		inflate(new File("ConseqCheckPreDialog.fxml"));
	}

	@Override
	public String getTitle() {
		return "Consequence Check (Pre)";
	}

	@Override
	public String getRationale() {
		RationaleBuilder sb = new RationaleBuilder();

		sb.addProse("using Hoare rule 6 (consequence): p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q<sub>0</sub>}, q<sub>0</sub> " + StringUtil.bool_impl_meta + "q" + StringUtil.bool_impl + "{p} S {q}");
		sb.addProse("with q<sub>0</sub>" + StringUtil.bool_eq + "q: p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q} " + StringUtil.bool_impl_meta + "{p} S {q}");

		sb.addParam("p", styleCond(_newPreCond));
		sb.addParam("p<sub>0</sub>", styleCond(_origPreCond));

		sb.addStep("Does p imply p<sub>0</sub>?");

		sb.addParam("CNF(p)", styleCond(_newPreCNF));
		sb.addParam("CNF(p<sub>0</sub>)", styleCond(_origPreCNF));

		sb.addParam("DNF(p)", styleCond(_newPreDNF));
		sb.addParam("DNF(p<sub>0</sub>)", styleCond(_origPreDNF));

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
						_callback.result(true);
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});

			_button_no.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						_callback.result(false);
					} catch (Exception e) {
						ErrorUtil.logEFX(e);
					}
				}
			});

			_tab_cnf.setContent(new ConseqCheckTableView(_origPreCNF, _newPreCNF).getRoot());
			_tab_dnf.setContent(new ConseqCheckTableView(_origPreDNF, _newPreDNF).getRoot());
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}