import Domain.ASTVisitor;
import Domain.Args.Arguments;
import Domain.Decl.*;
import Domain.Expr.*;
import Domain.Param.ArrayParameter;
import Domain.Param.Parameter;
import Domain.Param.Parameters;
import Domain.Program;
import Domain.Stmt.*;
import Domain.Type_spec.TypeSpecification;

public class PyByteCodeGenVisitor implements ASTVisitor {
    private Code pycCode;
    private Code currentCode;
    private int assignDepth;
    private int lineNumber;
    private int jumpNum;
    private boolean isReturn;

    @Override
    public void visitProgram(Program node) {
        if (node.decls == null) {
            System.out.println("No data! : visitProgram");
            return ;
        }
        lineNumber = 1;
        jumpNum = 0;
        pycCode = new Code(64);
        currentCode = new Code(67);

        pycCode.setFirstLineNumber(lineNumber++);
        assignDepth = 0;
        for (Declaration declaration : node.decls) {
            declaration.accept(this);
        }

        pycCode.appendCode(OpCode.LOAD_NAME.getHexCode());
        pycCode.appendCode(pycCode.indexOfNames("main"));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

        pycCode.appendCode(OpCode.CALL_FUNCTION.getHexCode());
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());
        pycCode.appendCode(OpCode.POP_TOP.getHexCode());

        pycCode.appendCode(OpCode.LOAD_CONST.getHexCode());
        pycCode.addConst("None");
        pycCode.appendCode(pycCode.indexOfConst("None"));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());
        pycCode.appendCode(OpCode.RETURN_VALUE.getHexCode());

        new ByteCodeToPycGenerator().compile(pycCode);


    }

    @Override
    public void visitDecl(Declaration node) {}

    @Override
    public void visitVar_decl(Variable_Declaration node) {
        if (!pycCode.addNames(node.lhs.getText())) {
            System.out.println("This value is already exist : visitVar_decl");
        }
    }

    @Override
    public void visitVar_decl_array(Variable_Declaration_Array node) {
        String variableName = node.lhs.getText();
        if (!pycCode.addNames(variableName)) {
            System.out.println("This value is already exist : visitVar_decl_array");
            return ;
        }
        if (literalNumber(node.rhs.getText()) <= 0) {
            System.out.println("This array number is le than 0 : visitVar_decl_array");
            return ;
        }

        pycCode.appendCode(OpCode.BUILD_MAP.getHexCode());
        pycCode.appendCode(String.format("%02x", 0));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

        pycCode.appendCode(OpCode.STORE_GLOBAL.getHexCode());
        pycCode.appendCode(pycCode.indexOfNames(variableName));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

    }

    @Override
    public void visitVar_decl_assign(Variable_Declaration_Assign node) {
        String variableName = node.lhs.getText();
        if (!pycCode.addNames(variableName)) {
            System.out.println("This value is already exist : visitVar_decl_assign");
            return ;
        }
        int literal = literalNumber(node.rhs.getText());

        pycCode.addConst(literal);
        pycCode.appendCode(OpCode.LOAD_CONST.getHexCode());
        pycCode.appendCode(pycCode.indexOfConst(literal));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

        pycCode.appendCode(OpCode.STORE_GLOBAL.getHexCode());
        pycCode.appendCode(pycCode.indexOfNames(variableName));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

    }

    @Override
    public void visitType_spec(TypeSpecification node) {}

    @Override
    public void visitFun_decl(Function_Declaration node) {
        String functionName = node.t_node.getText();
        if (!pycCode.addNames(functionName)) {
            System.out.println("Function name is already exist : visitFun_decl");
            return ;
        }
        currentCode.setName(functionName);
        currentCode.addConst("None");
        isReturn = false;

        node.params.accept(this);
        node.compount_stmt.accept(this);
        currentCode.setFirstLineNumber(lineNumber++);
        pycCode.addConst(currentCode);

        pycCode.appendCode(OpCode.LOAD_CONST.getHexCode());
        pycCode.appendCode(pycCode.indexOfConst(currentCode));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

        pycCode.appendCode(OpCode.MAKE_FUNCTION.getHexCode());
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());

        pycCode.appendCode(OpCode.STORE_NAME.getHexCode());
        pycCode.appendCode(pycCode.indexOfNames(functionName));
        pycCode.appendCode(OpCode.STOP_CODE.getHexCode());


        if (!isReturn) {
            currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
            currentCode.appendCode(currentCode.indexOfConst("None"));
            currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
            currentCode.appendCode(OpCode.RETURN_VALUE.getHexCode());
        }
        System.out.println(currentCode.getCode().toString());
        currentCode = new Code(67);

    }

    @Override
    public void visitParam_array(ArrayParameter node) {
        if (!currentCode.addVarNames(node.t_node.getText())) {
            System.out.println("argument is already exist : visitParam_array");
            return ;
        }
        currentCode.incArgCount();
    }

    @Override
    public void visitParams(Parameters node) {
        if (node.params == null) {
            return ;
        }
        for (Parameter parameter : node.params) {
            parameter.accept(this);
        }
    }

    @Override
    public void visitParam(Parameter node) {
        if (!currentCode.addVarNames(node.t_node.getText())) {
            System.out.println("argument is already exist : visitParam");
            return ;
        }
        currentCode.incArgCount();
    }

    @Override
    public void visitStmt(Statement node) {}

    @Override
    public void visitExpr_stmt(Expression_Statement node) {
        node.expr.accept(this);
    }

    @Override
    public void visitWhile_stmt(While_Statement node) {
        // TODO Disassemble이 안돼;;
        currentCode.appendCode(OpCode.SETUP_LOOP.getHexCode());
        currentCode.appendCode("J"+jumpNum++);
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        int savePos = currentCode.getCode().toString().length()/2;

        ++assignDepth;
        node.expr.accept(this);
        --assignDepth;
        currentCode.appendCode(OpCode.POP_JUMP_IF_FALSE.getHexCode());
        currentCode.appendCode("J"+jumpNum++);
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        node.stmt.accept(this);

        currentCode.appendCode(OpCode.JUMP_ABSOLUTE.getHexCode());
        currentCode.appendCode(String.format("%02x", savePos));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        currentCode.appendCode(OpCode.POP_BLOCK.getHexCode());
        --jumpNum;
        currentCode.setCode(
        currentCode.getCode().toString().replaceAll(("J"+jumpNum),
                String.format("%02x", (currentCode.getCode().toString().length()/2-1))));
        --jumpNum;
        currentCode.setCode(
        currentCode.getCode().toString().replaceAll(("J"+jumpNum),
                String.format("%02x", (currentCode.getCode().toString().length()/2 - savePos))));

    }

    @Override
    public void visitCompound_stmt(Compound_Statement node) {
        if (node.local_decls != null) {
            for (Local_Declaration localDeclaration : node.local_decls) {
                localDeclaration.accept(this);
            }
        }
        if (node.stmts != null) {
            for (Statement statement : node.stmts) {
                statement.accept(this);
            }
        }
    }

    @Override
    public void visitLocal_decl(Local_Declaration node) {
        if (!currentCode.addVarNames(node.lhs.getText())) {
            System.out.println("This value is already exist! : visitLocal_decl");
            return ;
        }
    }

    @Override
    public void visitLocal_decl_array(Local_Variable_Declaration_Array node) {
        String variableName = node.lhs.getText();
        if (!currentCode.addVarNames(variableName)) {
            System.out.println("This value is already exist! : visitLocal_decl");
            return ;
        }
        if (literalNumber(node.rhs.getText()) <= 0) {
            System.out.println("This array number is le than 0 : visitLocal_decl");
            return ;
        }

        currentCode.appendCode(OpCode.BUILD_MAP.getHexCode());
        currentCode.appendCode(String.format("%02x", 0));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        currentCode.appendCode(OpCode.STORE_FAST.getHexCode());
        currentCode.appendCode(currentCode.indexOfVarNames(variableName));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
    }

    @Override
    public void visitLocal_decl_assign(Local_Variable_Declaration_Assign node) {
        String variableName = node.lhs.getText();
        if (!currentCode.addVarNames(variableName)) {
            System.out.println("This value is already exist! : visitLocal_decl");
            return ;
        }
        int literal = literalNumber(node.rhs.getText());

        currentCode.addConst(literal);
        currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
        currentCode.appendCode(currentCode.indexOfConst(literal));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        currentCode.appendCode(OpCode.STORE_FAST.getHexCode());
        currentCode.appendCode(currentCode.indexOfVarNames(variableName));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
    }

    @Override
    public void visitIf_stmt(If_Statement node) {
        // TODO

        ++assignDepth;
        node.expr.accept(this);
        --assignDepth;
        currentCode.appendCode(OpCode.POP_JUMP_IF_FALSE.getHexCode());
        currentCode.appendCode("J"+jumpNum++);
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        node.if_stmt.accept(this);

        boolean isIfReturn = isReturn;
        isReturn = false;


        if (node.else_stmt != null) {
            currentCode.appendCode(OpCode.JUMP_FORWARD.getHexCode());
            currentCode.appendCode("J"+jumpNum++);
            currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

            int save2 = (currentCode.getCode().toString().length()/2);

            int savePos = currentCode.getCode().toString().length()/2;
            node.else_stmt.accept(this);
            --jumpNum;
            currentCode.setCode(
                    currentCode.getCode().toString().replaceAll(("J"+(jumpNum)),
                            String.format("%02x", (currentCode.getCode().toString().length()/2-savePos))));

            --jumpNum;
            currentCode.setCode(
                    currentCode.getCode().toString().replaceAll("J"+(jumpNum),
                            String.format("%02x", save2)));
            if (isReturn && isIfReturn) {
                isReturn = true;
            } else {
                isReturn = false;
            }
        } else {
            --jumpNum;
            currentCode.setCode(
                    currentCode.getCode().toString().replaceAll(("J"+jumpNum),
                            String.format("%02x", (currentCode.getCode().toString().length()/2))));
        }


    }

    @Override
    public void visitReturn_stmt(Return_Statement node) {
        if (node.expr != null) {
            ++assignDepth;
            node.expr.accept(this);
            --assignDepth;
        } else {
            currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
            currentCode.appendCode(currentCode.indexOfConst("None"));
            currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        }
        currentCode.appendCode(OpCode.RETURN_VALUE.getHexCode());
        isReturn = true;
    }

    @Override
    public void visitExpr(Expression node) {}

    @Override
    public void visitAref_assign(ArefAssignNode node) {
        ++assignDepth;
        node.rhs.accept(this);
        --assignDepth;
        if (assignDepth > 0) {
            currentCode.appendCode(OpCode.DUP_TOP.getHexCode());
        }
        String variableName = node.t_node.toString();
        if (currentCode.isContainVarNames(variableName)) {
            currentCode.appendCode(OpCode.LOAD_FAST.getHexCode());
            currentCode.appendCode(currentCode.indexOfVarNames(variableName));
        } else {
            currentCode.addNames(variableName);
            currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
            currentCode.appendCode(currentCode.indexOfNames(variableName));
        }
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        ++assignDepth;
        node.lhs.accept(this);
        --assignDepth;
        currentCode.appendCode(OpCode.STORE_SUBSCR.getHexCode());
    }

    @Override
    public void visitAref(ArefNode node) {
        if (assignDepth > 0) {
            String variableName = node.t_node.toString();
            if (currentCode.isContainVarNames(variableName)) {
                currentCode.appendCode(OpCode.LOAD_FAST.getHexCode());
                currentCode.appendCode(currentCode.indexOfVarNames(variableName));
            } else {
                currentCode.addNames(variableName);
                currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
                currentCode.appendCode(currentCode.indexOfNames(variableName));
            }
            currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
            node.expr.accept(this);
            currentCode.appendCode(OpCode.BINARY_SUBSCR.getHexCode());
        }
    }

    @Override
    public void visitAssign(AssignNode node) {
        String exprName = node.t_node.toString();
        ++assignDepth;
        node.expr.accept(this);
        --assignDepth;
        if (assignDepth > 0) {
            currentCode.appendCode(OpCode.DUP_TOP.getHexCode());
        }
        storeValue(exprName);
    }

    @Override
    public void visitBinary_op(BinaryOpNode node) {
        node.lhs.accept(this);
        if (node.op.equals("and")) {
                currentCode.appendCode(OpCode.JUMP_IF_FALSE_OR_POP.getHexCode());
                // 점프할 위치
                // TODO
                currentCode.appendCode("J"+jumpNum);
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

        } else if (node.op.equals("or")) {
            currentCode.appendCode(OpCode.JUMP_IF_TRUE_OR_POP.getHexCode());
            // 점프할 위치
            currentCode.appendCode("J"+ ++jumpNum);
            currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        }
        node.rhs.accept(this);
        if (node.op.equals("or")) {
            currentCode.setCode(
                    currentCode.getCode().toString().replaceAll(("J"+jumpNum--),
                            String.format("%02x", (currentCode.getCode().toString().length()/2+3))));
        }

        switch (node.op) {
            case "*":
                currentCode.appendCode(OpCode.BINARY_MULTIPLY.getHexCode());
                break;
            case "/":
                currentCode.appendCode(OpCode.BINARY_DIVIDE.getHexCode());
                break;
            case "%":
                currentCode.appendCode(OpCode.BINARY_MODULO.getHexCode());
                break;
            case "+":
                currentCode.appendCode(OpCode.BINARY_ADD.getHexCode());
                break;
            case "-":
                currentCode.appendCode(OpCode.BINARY_SUBTRACT.getHexCode());
                break;
            case "<":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 0));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            case "<=":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 1));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            case "==":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 2));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            case "!=":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 3));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            case ">":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 4));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            case ">=":
                currentCode.appendCode(OpCode.COMPARE_OP.getHexCode());
                currentCode.appendCode(String.format("%02x", 5));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                break;
            default:
                break;
        }

    }

    @Override
    public void visitFun_call(FuncallNode node) {
        String functionName = node.t_node.toString();
        if (functionName.equals("write")) {
            ++assignDepth;
            node.args.accept(this);
            --assignDepth;
            currentCode.appendCode(OpCode.PRINT_ITEM.getHexCode());
            currentCode.appendCode(OpCode.PRINT_NEWLINE.getHexCode());
            return ;
        } else if (functionName.equals("read")) {

            return ;
        }
        currentCode.addNames(functionName);
        currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
        currentCode.appendCode(currentCode.indexOfNames(functionName));
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        ++assignDepth;
        node.args.accept(this);
        --assignDepth;

        currentCode.appendCode(OpCode.CALL_FUNCTION.getHexCode());
        if (node.args.exprs == null) {
            currentCode.appendCode(String.format("%02x", 0));
        } else {
            currentCode.appendCode(String.format("%02x", node.args.exprs.size()));
        }

        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
        if (assignDepth == 0) {
            currentCode.appendCode(OpCode.POP_TOP.getHexCode());
        }

    }

    @Override
    public void visitParen(ParenExpression node) {
        node.expr.accept(this);
    }

    @Override
    public void visitTerminal(TerminalExpression node) {
        String variableName = node.toString();
        if ('0' <= variableName.charAt(0)  && variableName.charAt(0) <= '9') {
            currentCode.addConst(literalNumber(variableName));
            currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
            currentCode.appendCode(currentCode.indexOfConst(literalNumber(variableName)));
        } else if (currentCode.isContainVarNames(variableName)) {
            currentCode.appendCode(OpCode.LOAD_FAST.getHexCode());
            currentCode.appendCode(currentCode.indexOfVarNames(variableName));
        } else {
            currentCode.addNames(variableName);
            currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
            currentCode.appendCode(currentCode.indexOfNames(variableName));
        }
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
    }

    @Override
    public void visitUnary_op(UnaryOpNode node) {
        String exprName = node.expr.toString();
        ++assignDepth;
        node.expr.accept(this);
        --assignDepth;
        switch (node.op) {
            case "-":
                currentCode.appendCode(OpCode.UNARY_NEGATIVE.getHexCode());
                break;
            case "+":
                currentCode.appendCode(OpCode.UNARY_POSITIVE.getHexCode());
                break;
            case "--":
                currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
                currentCode.addConst(1);
                currentCode.appendCode(currentCode.indexOfConst(1));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());

                currentCode.appendCode(OpCode.BINARY_SUBTRACT.getHexCode());
                if (assignDepth > 0) {
                    currentCode.appendCode(OpCode.DUP_TOP.getHexCode());
                }
                if (node.expr instanceof ArefNode) {
                    String variableName = ((ArefNode)(node.expr)).t_node.toString();
                    if (currentCode.isContainVarNames(variableName)) {
                        currentCode.appendCode(OpCode.LOAD_FAST.getHexCode());
                        currentCode.appendCode(currentCode.indexOfVarNames(variableName));
                    } else {
                        currentCode.addNames(variableName);
                        currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
                        currentCode.appendCode(currentCode.indexOfNames(variableName));
                    }
                    currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                    ++assignDepth;
                    ((ArefNode)(node.expr)).expr.accept(this);
                    --assignDepth;
                    currentCode.appendCode(OpCode.STORE_SUBSCR.getHexCode());
                } else {
                    storeValue(exprName);
                }
                break;
            case "++":
                currentCode.appendCode(OpCode.LOAD_CONST.getHexCode());
                currentCode.addConst(1);
                currentCode.appendCode(currentCode.indexOfConst(1));
                currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                currentCode.appendCode(OpCode.BINARY_ADD.getHexCode());
                if (assignDepth > 0) {
                    currentCode.appendCode(OpCode.DUP_TOP.getHexCode());
                }
                if (node.expr instanceof ArefNode) {
                    String variableName = ((ArefNode)(node.expr)).t_node.toString();
                    if (currentCode.isContainVarNames(variableName)) {
                        currentCode.appendCode(OpCode.LOAD_FAST.getHexCode());
                        currentCode.appendCode(currentCode.indexOfVarNames(variableName));
                    } else {
                        currentCode.addNames(variableName);
                        currentCode.appendCode(OpCode.LOAD_GLOBAL.getHexCode());
                        currentCode.appendCode(currentCode.indexOfNames(variableName));
                    }
                    currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
                    ++assignDepth;
                    ((ArefNode)(node.expr)).expr.accept(this);
                    --assignDepth;
                    currentCode.appendCode(OpCode.STORE_SUBSCR.getHexCode());
                } else {
                    storeValue(exprName);
                }
                break;
            case "!":
                currentCode.appendCode(OpCode.UNARY_NOT.getHexCode());
                break;
            default:
                break;
        }
    }

    @Override
    public void visitArgs(Arguments node) {
        if (node.exprs == null) {
            return ;
        }
        for (Expression expression : node.exprs) {
            expression.accept(this);
        }
    }
    private void storeValue(String exprName) {
        if (currentCode.isContainVarNames(exprName)) {
            currentCode.appendCode(OpCode.STORE_FAST.getHexCode());
            currentCode.appendCode(currentCode.indexOfVarNames(exprName));
        } else {
            currentCode.addNames(exprName);
            currentCode.appendCode(OpCode.STORE_GLOBAL.getHexCode());
            currentCode.appendCode(currentCode.indexOfNames(exprName));
        }
        currentCode.appendCode(OpCode.STOP_CODE.getHexCode());
    }

    private int literalNumber(String number) {
        if (number.length() > 2) {
            if (number.charAt(1) == 'x' || number.charAt(1) == 'X') {
                return Integer.parseInt(number.substring(2, number.length()), 16);
            }
        }
        if (number.length() > 1) {
            if (number.charAt(0) == '0') {
                return Integer.parseInt(number, 8);
            }
        }
        return Integer.parseInt(number);
    }

}
