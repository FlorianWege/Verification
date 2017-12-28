package core;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.prog.Assign;
import core.structures.semantics.prog.HoareBlock;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import javafx.beans.property.ObjectProperty;
import util.ErrorUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class HoareExecuter {
    private ObjectProperty<SemanticNode> _semanticTreeP;
    private ObjectProperty<SemanticNode> _currentHoareNodeP;
    private ActionInterface _actionHandler;

    public interface ActionInterface extends Hoare.ActionInterface {
        void finished(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, boolean yes) throws IOException;
    }

    public static class StdActionInterface implements ActionInterface {
        private final ErrorUtil.NestedPrinter _printer = new ErrorUtil.NestedPrinter(System.out);

        @Override
        public void finished(@Nonnull SemanticNode node, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, boolean yes) throws IOException {

        }

        @Override
        public void beginNode(@Nonnull SemanticNode node, @Nonnull HoareCond postCond) {
            _printer.println("begin " + node + " with postcondition: " + postCond);

            _printer.begin();
        }

        @Override
        public void endNode(@Nonnull SemanticNode node, @Nonnull HoareCond preCond) {
            _printer.end();

            _printer.println("end " + node + " with precondition: " + preCond);
        }

        @Override
        public void reqSkipDialog(@Nonnull Skip skip, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Skip_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
            callback.result();
        }

        @Override
        public void reqAssignDialog(@Nonnull Assign assign, @Nonnull HoareCond preCond, @Nonnull HoareCond postCond, @Nonnull Hoare.Assign_callback callback) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
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
        public void reqLoopAskInvDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAskInv_callback callback) throws IOException, Hoare.HoareException, Parser.ParserException, Lexer.LexerException {
            InvInferrer inferrer = new InvInferrer(loop._whileNode, loop._postCond);

            List<HoareCond> candidates = inferrer.execSync();

            if (candidates.isEmpty()) {
                callback.result(null);
            } else {
                callback.result(candidates.get(0));
            }
        }

        @Override
        public void reqLoopCheckPostCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckPostCond_callback callback) throws IOException, Hoare.HoareException, Parser.ParserException, Lexer.LexerException {
            callback.result();
        }

        @Override
        public void reqLoopGetBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopGetBodyCond_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {
            callback.result();
        }

        @Override
        public void reqLoopCheckBodyCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopCheckBodyCond_callback callback) throws IOException, Hoare.HoareException, Parser.ParserException, Lexer.LexerException {
            callback.result();
        }

        @Override
        public void reqLoopAcceptInvCondDialog(@Nonnull Hoare.wlp_loop loop, @Nonnull Hoare.LoopAcceptInv_callback callback) throws IOException, Lexer.LexerException, Hoare.HoareException, Parser.ParserException, SemanticNode.CopyException {
            callback.result();
        }

        @Override
        public void reqConseqCheckPreDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPreCond, @Nonnull HoareCond newPreCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException, Lexer.LexerException, Parser.ParserException, Hoare.HoareException {
            TheoremSolver solver = new TheoremSolver(new BoolImpl(newPreCond.getBoolExp(), origPreCond.getBoolExp()), new TheoremSolver.Callback() {
                @Override
                public void accept() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
                    callback.result(true);
                }

                @Override
                public void reject(@Nonnull BoolExp reducedBoolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
                    callback.result(false);
                }
            });

            solver.exec();
        }

        @Override
        public void reqConseqCheckPostDialog(@Nonnull SemanticNode node, @Nonnull HoareCond origPostCond, @Nonnull HoareCond newPostCond, @Nonnull Hoare.ConseqCheck_callback callback) throws IOException, Hoare.HoareException, Parser.ParserException, Lexer.LexerException {
            TheoremSolver solver = new TheoremSolver(new BoolImpl(origPostCond.getBoolExp(), newPostCond.getBoolExp()), new TheoremSolver.Callback() {
                @Override
                public void accept() throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
                    callback.result(true);
                }

                @Override
                public void reject(@Nonnull BoolExp reducedBoolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {
                    callback.result(false);
                }
            });

            solver.exec();
        }
    }

    private Hoare _hoare;

    private class HoareNode {
        private HoareBlock _refNode;

        public HoareBlock getRefNode() {
            return _refNode;
        }

        private List<HoareNode> _children = new Vector<>();

        public List<HoareNode> getChildren() {
            return _children;
        }

        public void addChild(@Nonnull HoareNode child) {
            _children.add(child);
        }

        public HoareNode(@Nonnull HoareBlock actualNode) {
            _refNode = actualNode;
        }
    }

    private List<HoareNode> collectChildren(@Nonnull SemanticNode node) {
        List<HoareNode> ret = new ArrayList<>();

        for (SemanticNode child : node.getChildren()) {
            List<HoareNode> hoareChildren = collectChildren(child);

            ret.addAll(hoareChildren);
        }

        if (node instanceof HoareBlock) {
            HoareNode selfNode = new HoareNode((HoareBlock) node);

            for (HoareNode child : ret) {
                selfNode.addChild(child);
            }

            ret.clear();

            ret.add(selfNode);
        }

        return ret;
    }

    private interface ExecInterface {
        void finished() throws Hoare.HoareException, Lexer.LexerException, IOException, Parser.ParserException, SemanticNode.CopyException;
    }

    public class Executer {
        private HoareNode _node;
        private ExecInterface _callback;

        private Vector<Executer> _execChain = new Vector<>();
        private Iterator<Executer> _execChainIt;

        public void exec() throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
            HoareBlock hoareBlockNode = _node.getRefNode();

            _currentHoareNodeP.set(hoareBlockNode);

            HoareCond preCond = hoareBlockNode.getPreCond();
            HoareCond postCond = hoareBlockNode.getPostCond();

            _hoare.wlp(hoareBlockNode, postCond, new Hoare.wlp_callback() {
                @Override
                public void result(@Nonnull SemanticNode node, @Nonnull HoareCond lastPreCond, @Nonnull HoareCond lastPostCond) throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException {
                    System.out.println("final preCondition: " + lastPreCond);

                    _hoare.conseq_check_pre(hoareBlockNode, lastPreCond, preCond, new Hoare.ConseqCheck_callback() {
                        @Override
                        public void result(boolean yes) throws Hoare.HoareException, Lexer.LexerException, IOException, Parser.ParserException, SemanticNode.CopyException {
                            if (yes) {
                                System.out.println(preCond + "->" + postCond + " holds true (wlp: " + preCond + ")");
                            } else {
                                System.out.println(preCond + "->" + postCond + " failed (wlp: " + preCond + ")");
                            }

                            _actionHandler.finished(hoareBlockNode, preCond, postCond, yes);

                            _callback.finished();
                        }
                    });
                }
            });
        }

        public Executer(@Nonnull HoareNode node, int nestDepth, @Nonnull ExecInterface callback) throws IOException, Hoare.HoareException, Parser.NoRuleException, Lexer.LexerException {
            _node = node;
            _callback = callback;

            ExecInterface childCallback = new ExecInterface() {
                @Override
                public void finished() throws Hoare.HoareException, Lexer.LexerException, IOException, Parser.ParserException, SemanticNode.CopyException {
                    Executer next = _execChainIt.next();

                    next.exec();
                }
            };

            for (HoareNode child : node.getChildren()) {
                _execChain.add(new Executer(child, nestDepth + 1, childCallback));
            }

            _execChain.add(this);

            _execChainIt = _execChain.iterator();
        }
    }

    private List<Executer> _execChain;
    private Iterator<Executer> _execChainIt;

    public void exec() throws Hoare.HoareException, Lexer.LexerException, IOException, Parser.ParserException, SemanticNode.CopyException {
        System.err.println("hoaring...");

        List<HoareNode> children = collectChildren(_semanticTreeP.get());

        if (children.isEmpty()) {
            System.err.println("no hoareBlocks");
        } else {
            _execChain = new ArrayList<>();

            for (HoareNode child : children) {
                if (children.get(children.size() - 1).equals(child)) {
                    _execChain.add(new Executer(child, 0, new ExecInterface() {
                        @Override
                        public void finished() throws Hoare.HoareException, Parser.NoRuleException, Lexer.LexerException, IOException {
                            System.err.println("hoaring finished");

                            _currentHoareNodeP.set(null);
                        }
                    }));
                } else {
                    _execChain.add(new Executer(child, 0, new ExecInterface() {
                        @Override
                        public void finished() throws IOException, Hoare.HoareException, Lexer.LexerException, Parser.ParserException, SemanticNode.CopyException {
                            _execChainIt.next().exec();
                        }
                    }));
                }
            }

            _execChainIt = _execChain.iterator();

            _execChainIt.next().exec();
        }
    }

    public HoareExecuter(@Nonnull ObjectProperty<SemanticNode> syntaxTreeP, @Nonnull ObjectProperty<SemanticNode> currentHoareNodeP, @Nonnull ActionInterface actionHandler) throws Exception {
        _semanticTreeP = syntaxTreeP;
        _currentHoareNodeP = currentHoareNodeP;
        _actionHandler = actionHandler;

        _hoare = new Hoare(actionHandler);

        if (_semanticTreeP.get() == null) throw new Exception("no semanticTree");
    }
}
