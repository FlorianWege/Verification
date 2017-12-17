package tests.hoare.impl;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.*;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Prog;
import core.structures.semantics.prog.Skip;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoopInvTest {
    private void checkPre(@Nonnull BoolExp inv, @Nonnull BoolExp loopExp, @Nonnull Prog loopBody) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
        Hoare hoare = new Hoare(new Hoare.ActionInterface() {
            @Override
            public void beginNode(@Nonnull SemanticNode node, @Nonnull HoareCond postCond) {

            }

            @Override
            public void endNode(@Nonnull SemanticNode node, @Nonnull HoareCond preCond) {

            }

            @Override
            public void reqSkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Skip_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqAssignDialog(@Nonnull Assign assign, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Assign_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                System.out.println(assign.getVar() + "->" + assign.getExp());

                callback.result();
            }

            @Override
            public void reqCompNextDialog(@Nonnull Hoare.wlp_comp comp, @Nonnull Hoare.CompNext_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqCompMergeDialog(@Nonnull Hoare.wlp_comp comp, @Nonnull Hoare.CompMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqAltFirstDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltThen_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqAltElseDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltElse_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqAltMergeDialog(@Nonnull Hoare.wlp_alt alt, @Nonnull Hoare.AltMerge_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                callback.result();
            }

            @Override
            public void reqLoopAskInvDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAskInv_callback callback) throws IOException {

            }

            @Override
            public void reqLoopCheckPostCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckPostCond_callback callback) throws IOException {

            }

            @Override
            public void reqLoopGetBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopGetBodyCond_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

            }

            @Override
            public void reqLoopCheckBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckBodyCond_callback callback) throws IOException {

            }

            @Override
            public void reqLoopAcceptInvCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAcceptInv_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {

            }

            @Override
            public void reqConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException, Lexer.LexerException, Parser.ParserException, Hoare.HoareException {

            }

            @Override
            public void reqConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException, Hoare.HoareException, Parser.ParserException, Lexer.LexerException {

            }
        });

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