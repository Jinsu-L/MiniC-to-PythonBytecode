import Domain.Args.Arguments;
import Domain.Decl.*;
import Domain.Expr.*;
import Domain.MiniCNode;
import Domain.Param.ArrayParameter;
import Domain.Param.Parameter;
import Domain.Param.Parameters;
import Domain.Program;
import Domain.Stmt.*;
import Domain.Type_spec.TypeSpecification;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.List;

import static Domain.Type_spec.TypeSpecification.Type.INT;
import static Domain.Type_spec.TypeSpecification.Type.VOID;

/**
 * MiniCAstVisitor (AST)
 * KimHyeockJin 201502043
 */
public class MiniCAstVisitor extends MiniCBaseVisitor<MiniCNode> {

    @Override
    public Program visitProgram(MiniCParser.ProgramContext ctx) {
        List<Declaration> declContextList = new ArrayList<>();
        for(int i = 0; i < ctx.decl().size(); ++i) {
            declContextList.add((Declaration) visit(ctx.decl(i)));
        }

        return new Program(declContextList);
    }

    @Override
    public Declaration visitDecl(MiniCParser.DeclContext ctx) {
        return (Declaration) visitChildren(ctx);
    }

    @Override
    public Variable_Declaration visitVar_decl(MiniCParser.Var_declContext ctx) {
        if (isDeclare(ctx)) {
            return new Variable_Declaration((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT());
        }
        else if (isDefine(ctx)) {
            return new Variable_Declaration_Assign((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT(), ctx.LITERAL());
        }
        else if (isArray(ctx)) {
            return new Variable_Declaration_Array((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT(), ctx.LITERAL());
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public TypeSpecification visitType_spec(MiniCParser.Type_specContext ctx) {
        if (isVoid(ctx)) {
            return new TypeSpecification(VOID);
        }
        else if (isInt(ctx)) {
            return new TypeSpecification(INT);
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Function_Declaration visitFun_decl(MiniCParser.Fun_declContext ctx) {
        return new Function_Declaration((TypeSpecification)visit(ctx.type_spec()), ctx.IDENT(),
                (Parameters) visit(ctx.params()), (Compound_Statement) visit(ctx.compound_stmt()));
    }

    @Override
    public Parameters visitParams(MiniCParser.ParamsContext ctx) {
        if (isParams(ctx)) {
            List<Parameter> parameterList = new ArrayList<>();
            for (MiniCParser.ParamContext paramContext :ctx.param()) {
                parameterList.add((Parameter) visit(paramContext));
            }
            return new Parameters(parameterList);
        }
        else if (isVoidParam(ctx)) {
            return new Parameters(new TypeSpecification(VOID));
        }
        else if (isNoParam(ctx)) {
            return new Parameters();
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Parameter visitParam(MiniCParser.ParamContext ctx) {
        if (isParameter(ctx)) {
            return new Parameter((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT());
        }
        else if (isArrayParam(ctx)) {
            return new ArrayParameter((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT());
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Statement visitStmt(MiniCParser.StmtContext ctx) {
        return (Statement) visitChildren(ctx);
    }

    @Override
    public Expression_Statement visitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        return new Expression_Statement((Expression) visit(ctx.expr()));
    }

    @Override
    public While_Statement visitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        return new While_Statement(ctx.WHILE(), (Expression)visit(ctx.expr()), (Statement) visit(ctx.stmt()));
    }

    @Override
    public Compound_Statement visitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        List<Local_Declaration> localList = new ArrayList<>();
        List<Statement> statementList = new ArrayList<>();
        for (MiniCParser.Local_declContext localDeclContext : ctx.local_decl()) {
            localList.add((Local_Declaration) visit(localDeclContext));
        }
        for (MiniCParser.StmtContext stmtContext : ctx.stmt()) {
            statementList.add((Statement) visit(stmtContext));
        }

        return new Compound_Statement(localList, statementList);
    }

    @Override
    public Local_Declaration visitLocal_decl(MiniCParser.Local_declContext ctx) {
        if (isLocalDeclare(ctx)) {
            return new Local_Declaration((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT());
        }
        else if (isLocalDefine(ctx)) {
            return new Local_Variable_Declaration_Assign((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT(), ctx.LITERAL());
        }
        else if (isLocalArray(ctx)) {
            return new Local_Variable_Declaration_Array((TypeSpecification) visit(ctx.type_spec()), ctx.IDENT(), ctx.LITERAL());
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Statement visitIf_stmt(MiniCParser.If_stmtContext ctx) {

        if (isIf(ctx)) {
            return new If_Statement(ctx.IF(),(Expression) visit(ctx.expr()), (Statement) visit(ctx.stmt(0)));
        }
        else if (isIfElse(ctx)) {
            return new If_Statement(ctx.IF(), (Expression) visit(ctx.expr()), (Statement) visit(ctx.stmt(0)), ctx.ELSE(), (Statement) visit(ctx.stmt(1)));
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Return_Statement visitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        if (isreturnVoid(ctx)) {
            return new Return_Statement(ctx.RETURN());
        }
        else if (isreturnExpr(ctx)) {
            return new Return_Statement(ctx.RETURN(), (Expression) visit(ctx.expr()));
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Expression visitExpr(MiniCParser.ExprContext ctx) {
        if (isLiteralIdent(ctx)) {
            if (ctx.getChild(0) == ctx.IDENT())
                return new TerminalExpression(ctx.IDENT());
            if (ctx.getChild(0) == ctx.LITERAL())
                return new TerminalExpression(ctx.LITERAL());
        }
        else if (isBracket(ctx)) {
            return new ParenExpression((Expression) visit(ctx.expr(0)));
        }
        else if (isIdentBracket(ctx)) {
            if (ctx.getChild(1).getText().equals("["))
                return new ArefNode(ctx.IDENT(), (Expression) visit(ctx.expr(0)));
            if (ctx.getChild(1).getText().equals("("))
                return new FuncallNode(ctx.IDENT(),(Arguments) visit(ctx.args()));
        }
        else if (isOperation(ctx)) {
            return new UnaryOpNode(ctx.op.getText(), (Expression) visit(ctx.expr(0)));
        }
        else if (isBinaryOperation(ctx)) {
            Expression expr1 = (Expression) visit(ctx.left);
            Expression expr2 = (Expression) visit(ctx.right);
            if ("*/%+-".indexOf(ctx.op.getText()) >= 0) {
                if (isToUnary(expr1, expr2, ctx.op.getText()))
                    return new UnaryOpNode(ctx.op.getText(), expr2);

                if (isOneLeft(expr1, expr2, ctx.op.getText())) return expr1;
                else if (isOneLeft(expr2, expr1, ctx.op.getText())) return expr2;
                else if (isToZero(expr1, expr2, ctx.op.getText()))
                    return new TerminalExpression(new TerminalNodeImpl(new CommonToken(33, "0")));

                if (expr1 instanceof TerminalExpression && ((TerminalExpression) expr1).t_node.getSymbol().getType()==33
                        && expr2 instanceof TerminalExpression && ((TerminalExpression) expr2).t_node.getSymbol().getType()==33) {
                    int v1 = parseInteger(expr1.toString());
                    int v2 = parseInteger(expr2.toString());
                    int result = calculate(v1, v2, ctx.op.getText());
                    return new TerminalExpression(new TerminalNodeImpl(new CommonToken(33, ""+result)));
                }
            }
            return new BinaryOpNode(expr1, ctx.op.getText(), expr2);
        }
        else if (isIdentAssign(ctx)) {
            return new AssignNode(ctx.IDENT(), (Expression) visit(ctx.expr(0)));
        }
        else if (isArrayInput(ctx)) {
            return new ArefAssignNode(ctx.IDENT(), (Expression) visit(ctx.expr(0)), (Expression) visit(ctx.expr(1)));
        }
        System.out.println("Error");
        return null;
    }

    @Override
    public Arguments visitArgs(MiniCParser.ArgsContext ctx) {
        if (isExprs(ctx)) {
            List<Expression> expressionList = new ArrayList<>();
            for (MiniCParser.ExprContext exprContext : ctx.expr()) {
                expressionList.add((Expression) visit(exprContext));
            }
            return new Arguments(expressionList);
        }
        else if (isEmptyExprs(ctx)) {
            return new Arguments();
        }

        System.out.println("Error");
        return null;
    }

    /**
     * var_decl의 함수
     */
    private boolean isLocalDeclare(MiniCParser.Local_declContext ctx) {
        return ctx.getChildCount() == 3;
    }
    private boolean isLocalDefine(MiniCParser.Local_declContext ctx) {
        return ctx.getChildCount() == 5;
    }
    private boolean isLocalArray(MiniCParser.Local_declContext ctx) {
        return ctx.getChildCount() == 6;
    }

    /**
     * type_spec의 함수
     */
    private boolean isVoid(MiniCParser.Type_specContext ctx) {
        return ctx.getChild(0) == ctx.VOID();
    }
    private boolean isInt(MiniCParser.Type_specContext ctx) {
        return ctx.getChild(0) == ctx.INT();
    }

    /**
     * params의 함수
     */
    private boolean isParams(MiniCParser.ParamsContext ctx) {
        return ctx.getChildCount() > 0 && ctx.getChild(0) != ctx.VOID();
    }
    private boolean isVoidParam(MiniCParser.ParamsContext ctx) {
        return ctx.getChildCount() == 1 && ctx.getChild(0) == ctx.VOID();
    }
    private boolean isNoParam(MiniCParser.ParamsContext ctx) {
        return ctx.getChildCount() == 0;
    }

    /**
     * param의 함수
     */
    private boolean isParameter(MiniCParser.ParamContext ctx) {
        return ctx.getChildCount() == 2;
    }
    private boolean isArrayParam(MiniCParser.ParamContext ctx) {
        return ctx.getChildCount() != 2;
    }

    /**
     * while_stmt의 함수
     */
    private boolean isNotWhileCompound(MiniCParser.While_stmtContext ctx) {
        return !ctx.getChild(4).getChild(0).getChild(0).getText().equals("{");
    }

    /**
     * local_decl의 함수
     */
    private boolean isDeclare(MiniCParser.Var_declContext ctx) {
        return ctx.getChildCount() == 3;
    }
    private boolean isDefine(MiniCParser.Var_declContext ctx) {
        return ctx.getChildCount() == 5;
    }
    private boolean isArray(MiniCParser.Var_declContext ctx) {
        return ctx.getChildCount() == 6;
    }

    /**
     * if_stmt의 함수
     */
    private boolean isIf(MiniCParser.If_stmtContext ctx) {
        return ctx.getChildCount()  == 5;
    }
    private boolean isIfElse(MiniCParser.If_stmtContext ctx) {
        return ctx.getChildCount() == 7;
    }
    private boolean isNotIfCompound(MiniCParser.If_stmtContext ctx) {
        return !ctx.getChild(4).getChild(0).getChild(0).getText().equals("{");
    }
    private boolean isNotElseCompound(MiniCParser.If_stmtContext ctx) {
        return !ctx.getChild(6).getChild(0).getChild(0).getText().equals("{");
    }
    /**
     * return_stmt의 함수
     */
    private boolean isreturnVoid(MiniCParser.Return_stmtContext ctx) {
        return ctx.getChildCount() == 2;
    }
    private boolean isreturnExpr(MiniCParser.Return_stmtContext ctx) {
        return ctx.getChildCount() == 3;
    }

    /**
     * expr 의 함수
     */
    private boolean isLiteralIdent(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 1;
    }
    private boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr()  && ctx.getChild(0) != ctx.IDENT();
    }
    private boolean isOperation(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 2;
    }
    private boolean isBracket(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(0).getText().equals("(");
    }
    private boolean isIdentBracket(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 4;
    }
    private boolean isArrayInput(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 6;
    }
    private boolean isIdentAssign(MiniCParser.ExprContext ctx) {
        return ctx.getChild(0) == ctx.IDENT() && ctx.getChildCount() == 3;
    }

    /**
     * args의 함수
     */
    private boolean isExprs(MiniCParser.ArgsContext ctx) {
        return ctx.getChildCount() != 0;
    }
    private boolean isEmptyExprs(MiniCParser.ArgsContext ctx) {
        return ctx.getChildCount() == 0;
    }


    private boolean isOneLeft(Expression expr1, Expression expr2, String op) {
        if (op.equals("*") && expr2.toString().equals("1"))
            return true;
        if (op.equals("/") && expr2.toString().equals("1"))
            return true;
        if (op.equals("+") && expr2.toString().equals("0"))
            return true;
        if (op.equals("-") && expr2.toString().equals("0"))
            return true;

        return false;
    }
    private boolean isToZero(Expression expr1, Expression expr2, String op) {
        if (op.equals("*") && (expr1.toString().equals("0") || expr2.toString().equals("0")))
            return true;
        if (op.equals("/") && expr1.toString().equals("0"))
            return true;
        if (op.equals("%") && expr2.toString().equals("1"))
            return true;

        return false;
    }
    private boolean isToUnary(Expression expr1, Expression expr2, String op) {
        return op.equals("-") && expr1.toString().equals("0");
    }

    private int parseInteger(String literal) {
        if (literal.startsWith("0x") || literal.startsWith("0X"))
            return Integer.parseInt(literal.substring(2), 16);
        else if (literal.startsWith("0"))
            return Integer.parseInt(literal, 8);
        else
            return Integer.parseInt(literal, 10);
    }
    private int calculate(int v1, int v2, String op) {
        switch (op) {
            case "*":
                return v1*v2;
            case "/":
                return v1/v2;
            case "%":
                return v1%v2;
            case "+":
                return v1+v2;
            default:  // -
                return v1-v2;
        }
    }
}
