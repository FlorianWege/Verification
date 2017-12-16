package gui.hoare;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareCond;
import util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConseqCheckPostDialog extends ConseqCheckDialog {
	public ConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, ConseqCheckDialog.Callback callback) throws IOException {
		super(node, newPostCond, origPostCond, callback);
	}

	@Override
	public String getTitle() {
		return "Consequence Check (Post)";
	}

	@Override
	public void getRationale_conseqCheckHeader(RationaleBuilder sb) {
		sb.addProse("using Hoare rule 6 (consequence): p" + StringUtil.bool_impl + "p<sub>0</sub>, {p<sub>0</sub>} S {q<sub>0</sub>}, q<sub>0</sub>" + StringUtil.bool_impl + "q " + StringUtil.bool_impl_meta + "{p} S {q}");
		sb.addProse("with p" + StringUtil.bool_eq + "p<sub>0</sub>: p" + "{p} S {q<sub>0</sub>}, q<sub>0</sub>" + StringUtil.bool_impl + "q " + StringUtil.bool_impl_meta + "{p} S {q}");

		sb.addParam("q<sub>0</sub>", styleCond(_targetCond));
		sb.addParam("q", styleCond(_sourceCond));

		sb.addStep("transform both sides to DNF");

		sb.addParam("DNF(q<sub>0</sub>')", styleCond(_targetDNF));
		sb.addParam("DNF(q')", styleCond(_sourceDNF));
	}
}