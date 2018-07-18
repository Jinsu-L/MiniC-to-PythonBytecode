package Domain.Decl;

import Domain.ASTVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import Domain.Type_spec.TypeSpecification;

public class Local_Variable_Declaration_Assign extends Local_Declaration{
	public TerminalNode rhs;
	
	public Local_Variable_Declaration_Assign(TypeSpecification type, TerminalNode lhs, TerminalNode rhs){
		super(type,lhs);
		this.rhs = rhs;
	}
	
	@Override
	public String toString(){
		return type.toString() + " " + lhs.getText() + " = " + rhs.getText() + ";";
	}

	@Override
	public void accept(ASTVisitor v) {
		v.visitLocal_decl_assign(this);
	}
}
