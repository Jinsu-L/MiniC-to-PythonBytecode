package Domain.Expr;

import Domain.ASTVisitor;
import Domain.MiniCNode;

public class Expression extends MiniCNode {

    @Override
    public void accept(ASTVisitor v) {
        v.visitExpr(this);
    }
}
