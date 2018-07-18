package Domain.Stmt;

import Domain.ASTVisitor;
import Domain.MiniCNode;

public abstract class Statement extends MiniCNode{
    public abstract void accept(ASTVisitor v);
}
