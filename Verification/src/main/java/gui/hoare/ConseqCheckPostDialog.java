package gui.hoare;

import core.Hoare;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import util.ErrorUtil;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConseqCheckPostDialog extends HoareDialog implements Initializable {
	@FXML
	private Tab _tab_cnf;
	@FXML
	private Tab _tab_dnf;

	@FXML
	private Button _button_yes;
	@FXML
	private Button _button_no;

	private final HoareCond _origPostCond;
	private final HoareCond _newPostCond;
	private final Hoare.Executer.ConseqCheck_callback _callback;

	private final BoolAnd _origPostCNF;
	private final BoolOr _origPostDNF;
	private final BoolAnd _newPostCNF;
	private final BoolOr _newPostDNF;

	public ConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull Hoare.Executer.ConseqCheck_callback callback) throws IOException {
		super(node, null, null);

		_origPostCond = origPostCond;
		_newPostCond = newPostCond;
		_callback = callback;

		_origPostCNF = _origPostCond.getBoolExp().makeCNF();
		_origPostDNF = _origPostCond.getBoolExp().makeDNF();

		_newPostCNF = _newPostCond.getBoolExp().makeCNF();
		_newPostDNF = _newPostCond.getBoolExp().makeDNF();

		inflate(new File("ConseqCheckPreDialog.fxml"));
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

		sb.addStep("Does q<sub>0</sub> imply q? (reduce them)");

		BoolExp origPostCond = _origPostCond.getBoolExp().reduce();
		BoolExp newPostCond = _newPostCond.getBoolExp().reduce();

		sb.addParam("q<sub>0</sub>'", styleCond(origPostCond));
		sb.addParam("q'", styleCond(newPostCond));

		sb.addStep("transform left side to CNF, right side to DNF");

		BoolExp origPostCondCNF = origPostCond.makeCNF();
		BoolExp newPostCondDNF = newPostCond.makeDNF();

		sb.addParam("CNF(q<sub>0</sub>')", styleCond(origPostCondCNF));
		sb.addParam("DNF(q')", styleCond(newPostCondDNF));

		sb.addStep("merge to implication");

		HoareCond impl = new HoareCond(new BoolImpl(origPostCond, newPostCond));

		sb.addParam("impl", styleCond(impl));

		sb.addStep("split implication");

		HoareCond splitImpl = new HoareCond(impl.getBoolExp().reduce());

		sb.addParam("split", styleCond(splitImpl));

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

			_tab_cnf.setContent(new ConseqCheckTableView(_origPostCNF, _newPostCNF).getRoot());
			_tab_dnf.setContent(new ConseqCheckTableView(_origPostDNF, _newPostDNF).getRoot());
		} catch (Exception e) {
			ErrorUtil.logEFX(e);
		}
	}
}