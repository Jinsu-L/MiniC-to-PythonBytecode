import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Paths;

public class TestMiniC {
    public static void main(String[] args) throws IOException{
        String filePath = Paths.get(".","src", "test").toString();
        MiniCLexer lexer = new MiniCLexer(new ANTLRFileStream(filePath));
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        MiniCParser parser = new MiniCParser( tokens );
        ParseTree tree = parser.program();

        MiniCAstVisitor visitor = new MiniCAstVisitor();
        visitor.visit(tree).accept(new PyByteCodeGenVisitor());
    }
}
