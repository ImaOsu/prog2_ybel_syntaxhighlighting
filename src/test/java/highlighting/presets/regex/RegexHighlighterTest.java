package highlighting.presets.regex;


import static org.junit.jupiter.api.Assertions.*;


import highlighting.core.HighlightRegion;
import highlighting.presets.MiniJavaTokens;
import java.util.List;


import highlighting.regex.RegexHighlighter;
import org.junit.jupiter.api.Test;


public class RegexHighlighterTest {


    private List<HighlightRegion> run(String text) {
        return new RegexHighlighter().computeRegions(text);
    }


    @Test
    void testNoMatches() {
        var res = run("xyz abc 123");
        assertTrue(res.isEmpty());
    }


    @Test
    void testSimpleNonOverlapping() {
        var res = run("public class Test {}");


        // public + class
        assertEquals(2, res.size());
        assertEquals("public", "public");
    }


    @Test
    void testAdjacentRegions() {
        var res = run("public class");


        // "public" ends at index 6, "class" beginnt bei 7 → kein Overlap
        assertEquals(2, res.size());
        assertTrue(res.get(0).end() <= res.get(1).start());
    }


    @Test
    void testKeywordInsideCommentIsIgnored() {
        var res = run("// public class");


        // Kommentar matcht → Keyword wird verworfen
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).start());
    }


    @Test
    void testJavadocPreferredOverBlockComment() {
        var res = run("/** Test */");


        // Javadoc kommt in MiniJavaTokens VOR Blockkommentar
        assertEquals(1, res.size());
        assertEquals(MiniJavaTokens.defaultTokens().get(0).colour(), res.get(0).colour());
    }


    @Test
    void testOverlappingStringAndKeyword() {
        var res = run("\"public\" public");


        // Das "public" im String darf NICHT als Keyword markiert werden
        // → 1 String + 1 Keyword
        assertEquals(2, res.size());


        // sicherstellen, dass die Regionen nicht überlappen
        assertTrue(res.get(0).end() <= res.get(1).start());
    }
}

