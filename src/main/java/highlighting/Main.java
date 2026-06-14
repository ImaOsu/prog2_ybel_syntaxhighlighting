package highlighting;

import highlighting.antlr.*;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.Texts;
import highlighting.regex.*;
import highlighting.ui.EditorUI;

import org.antlr.v4.runtime.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String... args) throws Exception {
        // Phase I: RegexHighlighter
        SyntaxHighlighter regex = new RegexHighlighter();

        // Phase II: ScanningHighlighter
        SyntaxHighlighter scanning = new ScanningHighlighter();

        // Phase III: AntlrTokenCollector (tokenbasiert)
        SyntaxHighlighter antlrToken = new AntlrTokenCollector();

        // Editor starten
        EditorUI.show(Texts.START_TEXT, regex);
        EditorUI.show(Texts.START_TEXT, scanning);
        // EditorUI.show(Texts.START_TEXT, antlrToken);

        // Pretty Printer starten
        runPrettyPrinter();
    }

    // ---------------------------------------------------------
    // HIER beginnt die Pretty-Printer-Methode
    // ---------------------------------------------------------
    public static void runPrettyPrinter() throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("Indent width (z.B. 2 oder 4): ");
        int indentWidth = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Pfad zur MiniJava-Datei: ");
        String path = sc.nextLine().trim();

        String code = Files.readString(Path.of(path));

        // ANTLR: Lexer → TokenStream → Parser
        CharStream input = CharStreams.fromString(code);
        MiniJavaLexer lexer = new MiniJavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJavaParser parser = new MiniJavaParser(tokens);

        // Parse tree erzeugen
        var tree = parser.compilationUnit();

        // Pretty Printer ausführen
        PrettyPrinterVisitor pp = new PrettyPrinterVisitor(indentWidth);
        pp.visit(tree);

        // Ergebnis ausgeben
        System.out.println("\n--- Pretty Printed Code ---\n");
        System.out.println(pp.result());
    }
}
