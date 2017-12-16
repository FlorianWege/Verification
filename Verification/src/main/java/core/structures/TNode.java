package core.structures;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class TNode<T extends TNode> {
    @Nonnull
    public abstract List<T> getChildren();

    @Nonnull
    public abstract String getTreeText();
}
