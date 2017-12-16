package gui.hoare;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareCond;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConseqCheckPreDialog extends ConseqCheckDialog {
	public ConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, ConseqCheckDialog.Callback callback) throws IOException {
		super(node, origPreCond, newPreCond, callback);
	}

	@Override
	public String getTitle() {
		return "Consequence Check (Pre)";
	}

	@Override
	public void getRationale_conseqCheckHeader(RationaleBuilder sb) {
		sb.addProse("using Hoare rule 6 (consequence): p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q<sub>0</sub>}, q<sub>0</sub> " + StringUtil.bool_impl + "q" + StringUtil.bool_impl_meta + "{p} S {q}");
		sb.addProse("with q<sub>0</sub>" + StringUtil.bool_eq + "q: p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q} " + StringUtil.bool_impl_meta + "{p} S {q}");

		sb.addParam("p", styleCond(_targetCond));
		sb.addParam("p<sub>0</sub>", styleCond(_sourceCond));

		sb.addStep("transform both sides to DNF");

		sb.addParam("DNF(p')", styleCond(_targetDNF));
		sb.addParam("DNF(p<sub>0</sub>')", styleCond(_sourceDNF));
	}
}