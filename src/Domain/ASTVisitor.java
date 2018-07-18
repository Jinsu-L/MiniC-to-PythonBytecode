package Domain;

import Domain.Args.Arguments;
import Domain.Decl.*;
import Domain.Expr.*;
import Domain.Param.ArrayParameter;
import Domain.Param.Parameter;
import Domain.Param.Parameters;
import Domain.Program;
import Domain.Stmt.*;
import Domain.Type_spec.TypeSpecification;

public interface ASTVisitor {

    public void visitProgram(Program node);

    public void visitDecl(Declaration node);

    public void visitVar_decl(Variable_Declaration node);

    public void visitVar_decl_array(Variable_Declaration_Array node);

    public void visitVar_decl_assign(Variable_Declaration_Assign node);

    public void visitType_spec(TypeSpecification node);

    public void visitFun_decl(Function_Declaration node);

    public void visitParam_array(ArrayParameter node);

    public void visitParams(Parameters node);

    public void visitParam(Parameter node);

    public void visitStmt(Statement node);

    public void visitExpr_stmt(Expression_Statement node);

    public void visitWhile_stmt(While_Statement node);

    public void visitCompound_stmt(Compound_Statement node);

    public void visitLocal_decl(Local_Declaration node);

    public void visitLocal_decl_array(Local_Variable_Declaration_Array node);

    public void visitLocal_decl_assign(Local_Variable_Declaration_Assign node);

    public void visitIf_stmt(If_Statement node);

    public void visitReturn_stmt(Return_Statement node);

    public void visitExpr(Expression node);

    public void visitAref_assign(ArefAssignNode node);

    public void visitAref(ArefNode node);

    public void visitAssign(AssignNode node);

    public void visitBinary_op(BinaryOpNode node);

    public void visitFun_call(FuncallNode node);

    public void visitParen(ParenExpression node);

    public void visitTerminal(TerminalExpression node);

    public void visitUnary_op(UnaryOpNode node);

    public void visitArgs(Arguments node);
}
