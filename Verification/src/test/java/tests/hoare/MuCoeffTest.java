package tests.hoare;

import core.structures.semantics.boolExp.ExpComp;
import core.structures.semantics.boolExp.ExpCompOp;
import core.structures.semantics.exp.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MuCoeffTest {
    @Test()
    public void sum() {
        Exp exp = new Sum(new ExpMu(), new Id("abc"), new ExpMu());

        Assert.assertEquals(exp.getMuCoeff(), new ExpLit(2));
    }

    @Test()
    public void prod() {
        Exp exp = new Prod(new ExpMu(), new Id("abc"), new ExpMu());

        Assert.assertEquals(exp.getMuCoeff(), new Prod(new Id("abc"), new ExpMu()));
    }

    @Test()
    public void revert() {
        ExpComp expComp = new ExpComp(new Sum(new Id("x"), new ExpMu()), new ExpCompOp(ExpCompOp.Type.EQUAL), new ExpLit(0));

        expComp = expComp.revertMu();

        Assert.assertEquals(new ExpComp(new Id("x"), new ExpCompOp(ExpCompOp.Type.LESS), new ExpLit(0)), expComp);
    }
}
