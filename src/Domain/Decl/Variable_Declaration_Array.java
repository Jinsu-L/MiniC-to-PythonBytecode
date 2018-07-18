package Domain.Decl;

import Domain.ASTVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import Domain.Type_spec.TypeSpecification;

public class Variable_Declaration_Array extends Variable_Declaration{
	public TerminalNode rhs;
	
	public Variable_Declaration_Array(TypeSpecification type, TerminalNode lhs, TerminalNode rhs){
		super(type,lhs);
		this.rhs = rhs;
	}
	
	@Override
	public String toString(){
		return type.toString() + " " + lhs.getText() + "[" + rhs.getText() + "];";
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitVar_decl_array(this);
	}
}
