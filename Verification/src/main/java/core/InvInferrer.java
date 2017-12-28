package core;

import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.While;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class InvInferrer {
    private final While _loop;
    private final HoareCond _postCond;

    public List<HoareCond> execSync() {
        List<HoareCond> ret = new ArrayList<>();



        return ret;
    }

    public InvInferrer(@Nonnull While loop, @Nonnull HoareCond postCond) {
        _loop = loop;
        _postCond = postCond;
    }
}
