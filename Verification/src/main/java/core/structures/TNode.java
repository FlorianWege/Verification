package core.structures;

import java.util.List;

public abstract class TNode<T extends TNode> {
    public abstract List<T> getChildren();

    public abstract String getTreeText();
}
