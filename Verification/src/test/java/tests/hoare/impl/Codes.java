package tests.hoare.impl;

import core.HoareExecuter;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareCond;
import grammars.HoareWhileGrammar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.testng.Assert;
import org.testng.annotations.Test;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.io.IOException;

public class Codes {
    public void check(@Nonnull String path, boolean positive) throws Exception {
        String s = IOUtil.getResourceAsString(path);

        SemanticNode semantic = SemanticNode.fromString(s, HoareWhileGrammar.getInstance());

        ObjectProperty<SemanticNode> semanticNodeP = new SimpleObjectProperty<>(semantic);

        final boolean[] isFinished = {false};

        HoareExecuter hoareExec = new HoareExecuter(semanticNodeP, semanticNodeP, new HoareExecuter.StdActionInterface() {
            @Override
            public void finished(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, boolean yes) throws IOException {
                super.finished(node, preCond, postCond, yes);

                Assert.assertEquals(yes, positive);

                isFinished[0] = true;
            }
        });

        hoareExec.exec();

        Assert.assertTrue(isFinished[0]);
    }

    public void checkPositive(@Nonnull String path) throws Exception {
        check(path, true);
    }

    public void checkNegative(@Nonnull String path) throws Exception {
        check(path, false);
    }

    @Test()
    public void alt() throws Exception {
        checkPositive("codes/alt.c");
    }

    @Test()
    public void assign() throws Exception {
        checkPositive("codes/assign.c");
    }

    @Test()
    public void assignNested() throws Exception {
        checkPositive("codes/assignNested.c");
    }

    @Test()
    public void collatz() throws Exception {
        checkPositive("codes/collatz.c");
    }

    @Test()
    public void div() throws Exception {
        checkPositive("codes/div.c");
    }

    @Test()
    public void euclid() throws Exception {
        checkPositive("codes/euclid.c");
    }

    @Test()
    public void factorial() throws Exception {
        checkPositive("codes/factorial.c");
    }

    @Test()
    public void factorial2() throws Exception {
        checkPositive("codes/factorial2.c");
    }

    @Test()
    public void power() throws Exception {
        checkPositive("codes/power.c");
    }

    @Test()
    public void swap() throws Exception {
        checkPositive("codes/swap.c");
    }

    @Test()
    public void swapF() throws Exception {
        checkNegative("codes/swapNeg.c");
    }
}
