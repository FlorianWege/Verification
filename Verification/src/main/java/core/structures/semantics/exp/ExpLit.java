package core.structures.semantics.exp;

import core.structures.semantics.SemanticNode;
import org.nevec.rjm.BigDecimalMath;
import util.IOUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ExpLit extends ExpElem {
    private final BigInteger _num;
    private final BigInteger _denom;

    @Nonnull
    public BigInteger getNum() {
        return _num;
    }

    @Nonnull
    public BigInteger getDenom() {
        return _denom;
    }

    public BigDecimal getVal() {
        return BigDecimal.valueOf(_num.doubleValue() / _denom.doubleValue());
    }

    public boolean isZero() {
        return getNum().equals(BigInteger.ZERO);
    }

    public boolean isOne() {
        return _num.equals(_denom);
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
    }

    public ExpLit(@Nonnull BigInteger num) {
        this(num, BigInteger.valueOf(1));
    }

    public ExpLit(@Nonnull Integer num, @Nonnull Integer denom) {
        this(BigInteger.valueOf(num), BigInteger.valueOf(denom));
    }

    public ExpLit(@Nonnull Integer num) {
        this(num, 1);
    }

    @CheckReturnValue
    @Nonnull
    private ExpLit applyGcd() {
        BigInteger num = _num;
        BigInteger denom = _denom;

        BigInteger gcd = num.gcd(denom);

        num = num.divide(gcd);
        denom = denom.divide(gcd);

        return new ExpLit(num, denom);
    }

    public boolean isNeg() {
        boolean ret = (_num.compareTo(BigInteger.ZERO) < 0);

        if (_denom.compareTo(BigInteger.ZERO) < 0) ret = !ret;

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit neg() {
        return new ExpLit(_num.negate(), _denom);
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit inv() {
        return new ExpLit(_denom, _num);
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit add(@Nonnull ExpLit other) {
        ExpLit ret = new ExpLit(_num.multiply(other._denom).add(other._num.multiply(_denom)), _denom.multiply(other._denom));

        return ret.applyGcd();
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit sub(@Nonnull ExpLit other) {
        return add(new ExpLit(other._num.negate(), other._denom));
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit mult(@Nonnull ExpLit other) {
        ExpLit ret = new ExpLit(_num.multiply(other._num), _denom.multiply(other._denom));

        return ret.applyGcd();
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit div(@Nonnull ExpLit other) {
        return mult(new ExpLit(other._denom, other._num));
    }

    private BigDecimal pow(@Nonnull BigDecimal base, @Nonnull BigDecimal exp) {
        return BigDecimalMath.pow(base, exp);
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit pow(@Nonnull ExpLit exponent) {
        if (exponent.isOne()) return this;

        ExpLit ret = this;

        if (exponent.isNeg()) {
            exponent = exponent.neg();
            ret = ret.inv();
        }

        exponent = exponent.applyGcd();

        if (!exponent.getDenom().equals(BigInteger.ONE)) {
            @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
            BigDecimal dec_base = new BigDecimal(ret.getNum()).divide(new BigDecimal(ret.getDenom()));
            @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
            BigDecimal dec_exponent = new BigDecimal(exponent.getNum()).divide(new BigDecimal(exponent.getDenom()));

            BigDecimal result = pow(dec_base, dec_exponent);

            BigInteger front = result.toBigInteger();
            BigDecimal remainder = result.remainder(BigDecimal.ONE);

            BigInteger mult = BigInteger.valueOf(10).pow(remainder.toString().length());

            BigInteger remainderI = remainder.multiply(new BigDecimal(mult)).toBigInteger();

            ret = new ExpLit(front.multiply(mult).add(remainderI), mult);

            ret = ret.applyGcd();

            return ret;
        }

        //TODO
        int exp = exponent.getNum().intValue();

        ret = new ExpLit(ret.getNum().pow(exp), ret.getDenom().pow(exp));

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit fact() {
        ExpLit ret = applyGcd();

        if (!ret.getDenom().equals(BigInteger.ONE)) throw new RuntimeException("cannot apply factorial: " + this.getContentString());

        BigInteger num = getNum();

        BigInteger k = num.subtract(BigInteger.ONE);

        while (k.compareTo(BigInteger.ONE) > 0) {
            num = num.multiply(k);

            k = k.subtract(BigInteger.ONE);
        }

        ret = new ExpLit(num);

        return ret;
    }

    @CheckReturnValue
    @Nonnull
    public ExpLit gcd(ExpLit b) {
        ExpLit a = applyGcd();

        b = b.applyGcd();

        if (!a.getDenom().equals(BigInteger.ONE)) throw new RuntimeException("a has fraction: " + this.getContentString());
        if (!b.getDenom().equals(BigInteger.ONE)) throw new RuntimeException("b has fraction: " + this.getContentString());

        return new ExpLit(_num.gcd(b._num), _denom);
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

    @Nonnull
    @Override
    public Exp reduce_spec(@Nonnull Reducer reducer) {
        Exp ret = applyGcd();

        reducer.addEntry(ret, Reducer.Law.APPLY_GCD);

        return ret;
    }

    @Nonnull
    @Override
    public Exp order_spec() {
        return applyGcd();
    }

    @Override
    public int comp_spec(@Nonnull Exp b) {
        return getVal().compareTo(((ExpLit) b).getVal());
    }
}