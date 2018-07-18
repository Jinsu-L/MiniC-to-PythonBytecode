import java.util.ArrayList;
import java.util.TreeSet;

// flags 가 67이면 global 에 만들어진 함수, 87? 이면 def안에 def, default 로 67로 지정하는게 좋을듯.
/**
 * argCount : parameter 수
 * nLocals : 함수내의 지역변수와 parameter 의 수를 합친 것
 * stackSize : 한번에 얼마나 스택에 넣는지 최대 크기. array 10개를 다 한번에 넣으면 stack size 10
 * flags : 이 코드가 무엇인지를 알려주는 듯.
 * code : hex 값으로 저장된 실행할 코드 hex 를 String 으로 저장.
 * myConst : 내부의 상수 값의 List
 * names : 함수 내에서 사용한 global 변수, 함수 이름
 * varNames : 지역변수 이름
 * fileName : 파일 이름
 * name : 함수 이름, global 의 경우 '<module>'으로 저장.
 * firstLineNumber : 함수의 시작하는 부분
 * lNoTab : byte code 를 mapping 해주는 debug 용이라는데 잘 모르겠음.
 */
public class Code {
    private int argCount;
//    private int nLocals;  // varNames.size()로 대체 가능.
    private int stackSize; // global 은 1, 함수는 2로 무조건 가능 (def 안의 def가 없어서)
    private int flags;
    private StringBuilder code;
    private ArrayList<Object> myConst;
    private ArrayList<String> names;
    private ArrayList<String> varNames;
    private String fileName;
    private String name;
    private int firstLineNumber;
    private String lNoTab;

    public Code(int flags) {
        argCount = 0;
        stackSize = 10;
        this.flags = flags;
        code = new StringBuilder();
        myConst = new ArrayList<>();
        names = new ArrayList<>();
        varNames = new ArrayList<>();
        fileName = "test.py";
        if (flags == 64) {
            name = "<module>";
        }
        lNoTab = "";
    }

    public boolean isContainConst(Object thisConst) {
        return myConst.contains(thisConst);
    }

    public boolean isContainNames(String name) {
        return names.contains(name);
    }

    public boolean isContainVarNames(String varName) {
        return varNames.contains(varName);
    }

    public int getArgCount() {
        return argCount;
    }

    public void incArgCount() {
        ++this.argCount;
    }

    public int getNLocals() {
        return varNames.size();
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public StringBuilder getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = new StringBuilder(code);
    }

    public void appendCode(String code) {
        this.code.append(code);
    }

    public ArrayList<Object> getMyConst() {
        return myConst;
    }

    public boolean addConst(Object myConst) {
        if (this.myConst.contains(myConst)) {
            return false;
        }
        return this.myConst.add(myConst);
    }

    public String indexOfConst(Object myConst) {
        addConst(myConst);

        return String.format("%02x", this.myConst.indexOf(myConst));
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public boolean addNames(String name) {
        if (this.names.contains(name)) {
            return false;
        }
        return this.names.add(name);
    }

    public String indexOfNames(String name) {
        addNames(name);
        return String.format("%02x", this.names.indexOf(name));
    }

    public ArrayList<String> getVarNames() {
        return varNames;
    }

    public boolean addVarNames(String varNames) {
        if (this.varNames.contains(varNames)) {
            return false;
        }
        return this.varNames.add(varNames);
    }

    public String indexOfVarNames(String varNames) {
        addVarNames(varNames);
        return String.format("%02x", this.varNames.indexOf(varNames));
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    public void setFirstLineNumber(int firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    public String getlNoTab() {
        return lNoTab;
    }
}
