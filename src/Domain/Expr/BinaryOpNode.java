package Domain.Expr;

import Domain.ASTVisitor;

public class BinaryOpNode extends Expression{
	public Expression lhs, rhs;
	public String op;
	
	public BinaryOpNode(Expression lhs, String op, Expression rhs){
		super();
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
	}
	
	@Override
	public String toString(){
		return lhs.toString() + " " + op + " " + rhs.toString();
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitBinary_op(this);
	}
}
