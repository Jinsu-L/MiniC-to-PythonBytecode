package Domain.Expr;

import Domain.ASTVisitor;

public class UnaryOpNode extends Expression{
	public String op;
	public Expression expr;
	
	public UnaryOpNode(String op, Expression expr){
		super();
		this.op = op;
		this.expr = expr;
	}
	
	@Override
	public String toString(){
		return op + expr.toString();
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitUnary_op(this);
	}
}
