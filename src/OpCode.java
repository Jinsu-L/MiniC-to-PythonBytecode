public enum OpCode {
    STOP_CODE("00"),  POP_TOP("01"), ROT_TWO("02"), DUP_TOP("04"), NOP("09"),
    UNARY_POSITIVE("0a"), UNARY_NEGATIVE("0b"), UNARY_NOT("0c"), LIST_APPEND("12"),
    BINARY_MULTIPLY("14"), BINARY_DIVIDE("15"), BINARY_MODULO("16"),
    BINARY_ADD("17"), BINARY_SUBTRACT("18"), BINARY_SUBSCR("19"), POP_JUMP_IF_FALSE("72"),
    STORE_SUBSCR("3c"), PRINT_EXPR("46"), PRINT_ITEM("47"), PRINT_NEWLINE("48"),
    BREAK_LOOP("50"), LOAD_LOCALS("52"), RETURN_VALUE("53"), POP_BLOCK("57"), STORE_NAME("5a"),
    STORE_GLOBAL("61"), LOAD_CONST("64"), LOAD_NAME("65"), BUILD_MAP("69"),
    COMPARE_OP("6b"), JUMP_FORWARD("6e"), JUMP_IF_FALSE_OR_POP("6f"),
    JUMP_IF_TRUE_OR_POP("70"), JUMP_ABSOLUTE("71"), LOAD_GLOBAL("74"),
    SETUP_LOOP("78"), LOAD_FAST("7c"), STORE_FAST("7d"), CALL_FUNCTION("83"),
    MAKE_FUNCTION("84");

    private String hexCode;

    OpCode(String opCode) {
        hexCode = opCode;
    }

    public String getHexCode() {
        return hexCode;
    }

}
