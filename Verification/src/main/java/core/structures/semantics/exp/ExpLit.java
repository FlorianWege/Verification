package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import util.IOUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ExpLit extends ExpElem {
    private BigInteger _num;
    private BigInteger _denom;

    public @Nonnull BigInteger getNum() {
        return _num;
    }

    public @Nonnull BigInteger getDenom() {
        return _denom;
    }

    public boolean isZero() {
        return getNum().equals(BigInteger.ZERO);
    }

    @Override
    public String toString() {
        return getContentString(new IOUtil.BiFunc<SemanticNode, String, String>() {
            @Override
            public String apply(SemanticNode semanticNode, String s) {
                return s;
            }
        });
    }

    public ExpLit(@Nonnull BigInteger num, @Nonnull BigInteger denom) {
        if (denom.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            denom = denom.negate();
        }

        _num = num;
        _denom = denom;

        applyGcd();
    }

    public ExpLit(@Nonnull int num, @Nonnull int denom) {
        this(BigInteger.valueOf(num), BigInteger.valueOf(denom));
    }

    public ExpLit(@Nonnull int num) {
        this(num, 1);
    }

    private void applyGcd() {
        BigInteger gcd = _num.gcd(_denom);

        _num = _num.divide(gcd);
        _denom = _denom.divide(gcd);
    }

    public boolean isNeg() {
        boolean ret = false;

        if (_num.compareTo(BigInteger.ZERO) < 0) ret = !ret;
        if (_denom.compareTo(BigInteger.ZERO) < 0) ret = !ret;

        return ret;
    }

    public void neg() {
        _num = _num.negate();
    }

    public void inv() {
        BigInteger num = _num;

        _num = _denom;
        _denom = num;
    }

    public void add(@Nonnull ExpLit other) {
        _num = _num.multiply(other._denom).add(other._num.multiply(_denom));
        _denom = _denom.multiply(other._denom);

        applyGcd();
    }

    public void sub(@Nonnull ExpLit other) {
        add(new ExpLit(other._num.negate(), other._denom));
    }

    public void mult(@Nonnull ExpLit other) {
        _num = _num.multiply(other._num);
        _denom = _denom.multiply(other._denom);

        applyGcd();
    }

    public void div(@Nonnull ExpLit other) {
        mult(new ExpLit(other._denom, other._num));
    }

    public void pow(@Nonnull ExpLit exponent) {
        int exp = exponent._num.divide(exponent._denom).intValue();

        _num = _num.pow(exp);
        _denom = _denom.pow(exp);
    };

    public void fact() {
        applyGcd();

        if (!_denom.equals(BigInteger.ONE)) throw new RuntimeException("cannot apply factorial: " + this.getContentString());

        BigInteger k = _num.subtract(BigInteger.ONE);

        while (k.compareTo(BigInteger.ONE) > 0) {
            _num = _num.multiply(k);

            k = k.subtract(BigInteger.ONE);
        }
    }

    public void gcd(ExpLit b) {
        applyGcd();
        b.applyGcd();

        if (!_denom.equals(BigInteger.ONE)) throw new RuntimeException("a has fraction: " + this.getContentString());
        if (!b._denom.equals(BigInteger.ONE)) throw new RuntimeException("b has fraction: " + this.getContentString());

        _num = _num.gcd(b._num);
    }

    @Override
    public String getContentString(@Nonnull IOUtil.BiFunc<SemanticNode, String, String> mapper) {
        return mapper.apply(this, (_num.toString() + (!_denom.equals(BigInteger.ONE) ? _grammar.TERMINAL_OP_DIV.getPrimRule() + _denom.toString() : "")));
    }

    @Nonnull
    @Override
    public SemanticNode replace(@Nonnull IOUtil.Func<SemanticNode, SemanticNode> replaceFunc) {
        return replaceFunc.apply(this);
    }

    @Override
    public Exp reduce() {
        return new ExpLit(_num, _denom);
    }

    @Override
    public void order() {
        applyGcd();
    }

    public BigDecimal getVal() {
        return BigDecimal.valueOf(_num.doubleValue() / _denom.doubleValue());
    }

    @Override
    public int comp(Exp b) {
        applyGcd();
        ((ExpLit) b).applyGcd();

        return getVal().compareTo(((ExpLit) b).getVal());
    }
}