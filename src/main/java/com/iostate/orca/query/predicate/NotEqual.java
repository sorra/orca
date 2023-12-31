package com.iostate.orca.query.predicate;

import com.iostate.orca.query.expression.Expression;

class NotEqual extends AbstractPredicate {

    private final Expression l;
    private final Expression r;

    public NotEqual(Expression l, Expression r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public String toString() {
        return l + " <> " + r;
    }

    @Override
    public Predicate negate() {
        return new Equal(l, r);
    }
}
