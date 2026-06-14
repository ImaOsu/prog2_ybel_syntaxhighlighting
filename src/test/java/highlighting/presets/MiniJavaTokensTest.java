package highlighting.presets;


import static org.junit.jupiter.api.Assertions.*;


import highlighting.regex.Token;
import highlighting.core.HighlightRegion;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;


public class MiniJavaTokensTest {


    // Hilfsmethode: führt ein Token auf Text aus und gibt Start/Ende zurück
    private List<HighlightRegion> run(Token t, String text) {
        return t.test(text);
    }


    @Test
    void testStringLiteral_basic() {
        var token = Token.of(
            Pattern.compile("\"([^\"\\\\]|\\\\.)*\""),
            MiniJavaColours.STRING_LITERAL_COLOUR
        );


        var res = run(token, "String s = \"Hallo\";");
        assertEquals(1, res.size());
        assertEquals(11, res.get(0).start());
        assertEquals(18, res.get(0).end());
    }


    @Test
    void testStringLiteral_withEscapes() {
        var token = Token.of(
            Pattern.compile("\"([^\"\\\\]|\\\\.)*\""),
            MiniJavaColours.STRING_LITERAL_COLOUR
        );


        var res = run(token, "\"Test \\\"123\\\"\"");
        assertEquals(1, res.size());
    }


    @Test
    void testCharLiteral_basic() {
        var token = Token.of(
            Pattern.compile("'([^'\\\\]|\\\\.)'"),
            MiniJavaColours.CHAR_LITERAL_COLOUR
        );


        var res = run(token, "char c = 'a';");
        assertEquals(1, res.size());
    }


    @Test
    void testCharLiteral_escape() {
        var token = Token.of(
            Pattern.compile("'([^'\\\\]|\\\\.)'"),
            MiniJavaColours.CHAR_LITERAL_COLOUR
        );


        var res = run(token, "'\\n'");
        assertEquals(1, res.size());
    }


    @Test
    void testLineComment() {
        var token = Token.of(
            Pattern.compile("//.*"),
            MiniJavaColours.LINE_COMMENT_COLOUR
        );


        var res = run(token, "int x; // Kommentar");
        assertEquals(1, res.size());
        assertEquals(7, res.get(0).start());
    }


    @Test
    void testBlockComment() {
        var token = Token.of(
            Pattern.compile("/\\*([^*]|\\*+[^*/])*\\*/"),
            MiniJavaColours.BLOCK_COMMENT_COLOUR
        );


        var res = run(token, "/* Hallo */ int x;");
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).start());
        assertEquals(11, res.get(0).end());
    }


    @Test
    void testJavadocComment() {
        var token = Token.of(
            Pattern.compile("/\\*\\*([\\s\\S]*?)\\*/"),
            MiniJavaColours.JAVADOC_COMMENT_COLOUR
        );


        var res = run(token, "/** Test\n * Mehr */");
        assertEquals(1, res.size());
    }


    @Test
    void testAnnotation() {
        var token = Token.of(
            Pattern.compile("@[A-Za-z][A-Za-z0-9_-]*"),
            MiniJavaColours.ANNOTATION_COLOUR
        );


        var res = run(token, "@Override\npublic void x(){}");
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).start());
    }


    @Test
    void testKeyword() {
        var token = Token.of(
            Pattern.compile("\\b(package|import|class|public|private|final|return|null|new)\\b"),
            MiniJavaColours.KEYWORD_COLOUR
        );


        var res = run(token, "public class Test {}");
        assertEquals(2, res.size()); // public + class
    }


    @Test
    void testKeyword_notInsideIdentifier() {
        var token = Token.of(
            Pattern.compile("\\b(class)\\b"),
            MiniJavaColours.KEYWORD_COLOUR
        );


        var res = run(token, "myclassTest");
        assertEquals(0, res.size());
    }


    @Test
    void testNumberLiteral() {
        var token = Token.of(
            Pattern.compile("\\b\\d+\\b"),
            java.awt.Color.RED
        );


        var res = run(token, "int x = 123;");
        assertEquals(1, res.size());
    }
}

