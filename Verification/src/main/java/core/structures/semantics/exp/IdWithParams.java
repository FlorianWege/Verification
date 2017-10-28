package core.structures.semantics.exp;

import java.util.ArrayList;
import java.util.List;

public abstract class Func extends ExpElem {
    private final List<Param> _params = new ArrayList<>();

    public List<Param> getParams() {
        return new ArrayList<>(_params);
    }

    public void addParam(Param param) {
        _params.add(param);
    }
}
