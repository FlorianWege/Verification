package core;

import core.structures.semantics.SemanticNode;
import core.structures.semantics.prog.HoareBlock;
import core.structures.semantics.prog.HoareCond;
import javafx.beans.property.ObjectProperty;

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
