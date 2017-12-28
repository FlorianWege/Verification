package tests.hoare.impl;

import core.Hoare;
import core.HoareExecuter;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Prog;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoopInvTest {
    private void checkPre(@Nonnull BoolExp inv, @Nonnull BoolExp loopExp, @Nonnull Prog loopBody) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
        Hoare hoare = new Hoare(new HoareExecuter.StdActionInterface());

        hoare.wlp(loopBody, new HoareCond(inv), new Hoare.wlp_callback() {
            @Override
            public void result(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                System.out.println("wlp is " + preCond);

                BoolExp wlp = preCond.getBoolExp();

                BoolExp source = new BoolAnd(inv, loopExp);
                BoolExp target = wlp;

                source = source.reduce();
                target = target.reduce();

                BoolImpl impl = new BoolImpl(source, target);

                System.out.println("pre impl: " + impl);

                //Assert.assertEquals(impl.reduce(), new BoolLit(true));
            }
        });
    }

    private void checkPost(@Nonnull BoolExp inv, @Nonnull BoolExp loopExp, @Nonnull BoolExp post) {
        BoolExp source = new BoolAnd(inv, new BoolNeg(loopExp));
        BoolExp target = post;

        source = source.reduce();
        target = target.reduce();

        System.out.println("source: " + source);
        System.out.println("target: " + target);

        BoolImpl impl = new BoolImpl(source, target);

        Assert.assertEquals(impl.reduce(), new BoolLit(true));

        BoolExp split = impl.split(true, false);

        System.out.println("split " + split);
    }

    @Test()
    public void factorial() throws Lexer.LexerException, Parser.ParserException, IOException, Hoare.HoareException {
        checkPre(BoolExp.fromString("f=k! & 0<=k & k<=n"), BoolExp.fromString("k<n"), Prog.fromString("k:=k+1;f:=f*k"));
        //checkPost(BoolExp.fromString("f=k! & 0<=k & k<=n"), BoolExp.fromString("k<n"), BoolExp.fromString("f=n!"));
    }

    @Test()
    public void factorial_and() throws Lexer.LexerException, Parser.ParserException {
        System.out.println(BoolExp.fromString("f=k! & 0<k & k=n").reduce());
        //Assert.assertEquals(BoolExp.fromString("0<=k & k<=n & k>=n").reduce(), BoolExp.fromString("k=n & "));
    }

    private abstract class Loop {
        public int _x;
        public int _y;

        @Override
        public boolean equals(Object other) {
            if (other instanceof Loop) {
                return _x == ((Loop) other)._x && _y == ((Loop) other)._y;

            }

            return super.equals(other);
        }

        public abstract void exec();

        public Loop() {

        }
    }

    private class Loop_a extends Loop {
        @Override
        public void exec() {
            while (_x>0) {
                _y = _y + 1;

                _x = _x - 1;
            }
        }

        public Loop_a() {
            super();
        }
    }

    private class Loop_b extends Loop {
        @Override
        public void exec() {
            int c = 0;

            while (_x>0) {
                _x = _x - 1;
                c++;
            }

            int c2 = 0;
            while (c2 < c) {
                _y = _y + 1;
                c2++;
            }
        }

        public Loop_b() {
            super();
        }
    }

    @Test()
    public void splitLoop() {
        List<Loop> loops = new ArrayList<>();

        loops.add(new Loop_a());
        loops.add(new Loop_b());

        for (Loop loop : loops) {
            loop._x = 10;
            loop._y = 1;

            loop.exec();

            System.out.println(loop.getClass().getSimpleName() + ": x: " + loop._x + ", y: " + loop._y);
            Assert.assertEquals(loop, loops.get(0));
        }
    }
}