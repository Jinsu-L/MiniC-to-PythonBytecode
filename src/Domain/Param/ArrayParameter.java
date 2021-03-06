package Domain.Param;

import Domain.ASTVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import Domain.Type_spec.TypeSpecification;

public class ArrayParameter extends Parameter{

	public ArrayParameter(TypeSpecification type, TerminalNode t_node) {
		super(type, t_node);
	}
	
	@Override
	public String toString(){
		return type.toString() + " " + t_node.toString() + "[]";
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitParam_array(this);
	}
}
