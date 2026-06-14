package highlighting.antlr;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class AntlrTokenCollector extends SyntaxHighlighter {

    @Override
    public List<HighlightRegion> collectMatches(String text) {

        List<HighlightRegion> regions = new ArrayList<>();

        // 1) Eingabetext in ANTLR-CharStream umwandeln
        CharStream input = CharStreams.fromString(text);

        // 2) Lexer erzeugen
        MiniJavaLexer lexer = new MiniJavaLexer(input);

        // 3) Alle Tokens holen
        List<? extends Token> tokens = lexer.getAllTokens();

        // 4) Über alle Tokens iterieren
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            int type = t.getType();

            if (type == Token.EOF) continue;

            int start = t.getStartIndex();
            int end = t.getStopIndex() + 1;

            // ---------------------------------------
            // Kommentare (liegen im HIDDEN channel)
            // ---------------------------------------
            if (t.getChannel() == Token.HIDDEN_CHANNEL) {

                if (type == MiniJavaLexer.LINE_COMMENT) {
                    regions.add(new HighlightRegion(start, end, MiniJavaColours.LINE_COMMENT_COLOUR));
                } else if (type == MiniJavaLexer.BLOCK_COMMENT) {
                    regions.add(new HighlightRegion(start, end, MiniJavaColours.BLOCK_COMMENT_COLOUR));
                } else if (type == MiniJavaLexer.JAVADOC_COMMENT) {
                    regions.add(new HighlightRegion(start, end, MiniJavaColours.JAVADOC_COMMENT_COLOUR));
                }

                continue;
            }

            // ---------------------------------------
            // Keywords
            // ---------------------------------------
            if (isKeyword(type)) {
                regions.add(new HighlightRegion(start, end, MiniJavaColours.KEYWORD_COLOUR));
                continue;
            }

            // ---------------------------------------
            // Literale
            // ---------------------------------------
            if (type == MiniJavaLexer.STRING_LITERAL) {
                regions.add(new HighlightRegion(start, end, MiniJavaColours.STRING_LITERAL_COLOUR));
                continue;
            }

            if (type == MiniJavaLexer.CHAR_LITERAL) {
                regions.add(new HighlightRegion(start, end, MiniJavaColours.CHAR_LITERAL_COLOUR));
                continue;
            }

            // ---------------------------------------
            // Annotationen: '@' + IDENTIFIER
            // ---------------------------------------
            if (type == MiniJavaLexer.AT) {
                // '@'
                regions.add(new HighlightRegion(start, end, MiniJavaColours.ANNOTATION_COLOUR));

                // nächstes Token prüfen
                if (i + 1 < tokens.size()) {
                    Token next = tokens.get(i + 1);
                    if (next.getType() == MiniJavaLexer.IDENTIFIER) {
                        int s2 = next.getStartIndex();
                        int e2 = next.getStopIndex() + 1;
                        regions.add(new HighlightRegion(s2, e2, MiniJavaColours.ANNOTATION_COLOUR));
                    }
                }
                continue;
            }

            // ---------------------------------------
            // Identifier → KEINE Farbe definiert
            // → also NICHT highlighten
            // ---------------------------------------
        }

        return regions;
    }

    // Hilfsmethode: Prüft, ob ein Token ein Keyword ist
    private boolean isKeyword(int type) {
        return switch (type) {
            case MiniJavaLexer.PACKAGE,
                 MiniJavaLexer.IMPORT,
                 MiniJavaLexer.CLASS,
                 MiniJavaLexer.PUBLIC,
                 MiniJavaLexer.PRIVATE,
                 MiniJavaLexer.FINAL,
                 MiniJavaLexer.RETURN,
                 MiniJavaLexer.NEW,
                 MiniJavaLexer.IF,
                 MiniJavaLexer.ELSE,
                 MiniJavaLexer.WHILE,
                 MiniJavaLexer.EXTENDS,
                 MiniJavaLexer.IMPLEMENTS -> true;
            default -> false;
        };
    }
}
