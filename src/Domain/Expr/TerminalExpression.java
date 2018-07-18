package Domain.Expr;

import Domain.ASTVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TerminalExpression extends Expression{
	public TerminalNode t_node;
	
	public TerminalExpression(TerminalNode t_node) {
		this.t_node = t_node;
	}
	
	@Override
	public String toString(){
		return t_node.getText();
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitTerminal(this);
	}
}
