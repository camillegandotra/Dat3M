package dartagnan.parsers.utils.branch;

import dartagnan.expression.IExpr;
import dartagnan.program.event.Event;

public class Cmp extends Event {

    private IExpr left;
    private IExpr right;

    public Cmp(IExpr left, IExpr right){
        this.left = left;
        this.right = right;
    }

    public IExpr getLeft(){
        return left;
    }

    public IExpr getRight(){
        return right;
    }

    @Override
    public String toString(){
        return "cmp " + left + " " + right;
    }

    @Override
    public Cmp clone(){
        return new Cmp(left.clone(), right.clone());
    }
}
