package Domain.Expr;

import Domain.ASTVisitor;

public class ParenExpression extends Expression{
	public Expression expr;
	
	public ParenExpression(Expression expr){
		this.expr = expr;
	}
	
	@Override
	public String toString(){
		return "(" + expr.toString() + ")";
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitParen(this);
	}
}
