package Domain.Expr;

import Domain.ASTVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AssignNode extends Expression{
	public TerminalNode t_node;
	public Expression expr;
	
	public AssignNode(TerminalNode t_node, Expression expr){
		super();
		this.t_node = t_node;
		this.expr = expr;
	}
	
	@Override
	public String toString(){
		return t_node.getText() + " = " + expr.toString() ;
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitAssign(this);
	}
}
