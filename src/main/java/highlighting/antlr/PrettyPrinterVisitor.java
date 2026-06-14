package highlighting.antlr;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/// MiniJava Pretty Printer (minimal, stateful)
///
/// Requirements:
/// - Reproduce the whole program (comments and whitespaces are gone).
/// - Ignore whitespace from the input; instead, generate:
///     - indentation for class bodies and blocks,
///     - exactly one line per statement (lines ending in ';').
///
/// Simplification:
/// Everything that is not indentation or line breaks is printed as raw tokens (with a very simple
/// space heuristic). Expression and signature formatting is therefore not "nice", which is
/// acceptable for this exercise.
public final class PrettyPrinterVisitor extends MiniJavaBaseVisitor<Void> {

  private final StringBuilder out = new StringBuilder();
  private final int indentWidth;
  private int currentIndent = 0;
  private boolean atLineStart = true;

  // For simple spacing between tokens:
  private Token lastToken = null;

  public PrettyPrinterVisitor(int indentWidth) {
    this.indentWidth = Math.max(0, indentWidth);
  }

  public String result() {
    return out.toString();
  }

  // ----------------------------------------------------
  // Structural methods – these enforce indentation and "one statement per line"
  //
  // TODO: implement the four structural visitXyz-methods below: visitCompilationUnit,
  // visitClassBody, visitBlock, and visitStatement
  // ----------------------------------------------------

  //@Override
  //public Void visitCompilationUnit(MiniJavaParser.CompilationUnitContext ctx) {
      @Override
      public Void visitCompilationUnit(MiniJavaParser.CompilationUnitContext ctx) {

          // package declaration (falls vorhanden)
          if (ctx.packageDecl() != null) {
              visit(ctx.packageDecl());
              nl();
              nl(); // Leerzeile nach package
          }

          // import declarations
          for (var imp : ctx.importDecl()) {
              visit(imp);
              nl();
          }
          if (!ctx.importDecl().isEmpty()) {
              nl(); // Leerzeile nach imports
          }

          // type declarations (Klassen)
          for (var type : ctx.typeDecl()) {
              visit(type);
              nl();
              nl(); // Leerzeile zwischen Klassen
          }

          return null;
      }



      // TODO:
    // Produce a nicely structured compilation unit:
    // - package declaration (if present),
    // - import declarations (one per line),
    // - type declarations (one after another),
    // with sensible blank lines between these parts.
    //return null;
  //}

    @Override
    public Void visitClassBody(MiniJavaParser.ClassBodyContext ctx) {

        // Öffnende Klammer
        write("{");
        nl();

        // Eine Einrückstufe tiefer für den Inhalt
        currentIndent++;

        // Alle Member-Deklarationen (Felder, Methoden, Konstruktoren)
        for (var decl : ctx.classBodyDeclaration()) {
            visit(decl);
            nl(); // Jede Deklaration auf eine eigene Zeile
        }

        // Einrückung zurück
        currentIndent--;

        // Schließende Klammer
        write("}");
        return null;
    }



 // @Override
  //public Void visitClassBody(MiniJavaParser.ClassBodyContext ctx) {
    // TODO:
    // Format the contents of a class body:
    // - opening and closing brace,
    // - one member declaration per line,
    // - members indented relative to the class.
   // return null;
  //}


    @Override
    public Void visitBlock(MiniJavaParser.BlockContext ctx) {

        // Öffnende Klammer
        write("{");
        nl();

        // Eine Einrückstufe tiefer für den Blockinhalt
        currentIndent++;

        // Jede blockStatement auf eine eigene Zeile
        for (var stmt : ctx.blockStatement()) {
            visit(stmt);
            nl();
        }

        // Einrückung zurück
        currentIndent--;

        // Schließende Klammer
        write("}");
        return null;
    }






  //@Override
  //public Void visitBlock(MiniJavaParser.BlockContext ctx) {
    // TODO:
    // Format a block:
    // - opening and closing brace,
    // - one blockStatement per line,
    // - nested blocks indented further.
   // return null;
  //}






    @Override
    public Void visitStatement(MiniJavaParser.StatementContext ctx) {

        // -------------------------
        // 1) Block: { ... }
        // -------------------------
        if (ctx.block() != null) {
            visit(ctx.block());
            return null;
        }

        // -------------------------
        // 2) return statement
        // -------------------------
        if (ctx.RETURN() != null) {
            write("return");
            if (ctx.expression() != null) {
                write(" ");
                visit(ctx.expression());
            }
            write(";");
            return null;
        }

        // -------------------------
        // 3) if (...) statement (else optional)
        // -------------------------
        if (ctx.IF() != null) {
            // if (expr)
            write("if (");
            visit(ctx.expression());
            write(")");
            nl();

            // then‑Zweig
            currentIndent++;
            visit(ctx.statement(0));
            currentIndent--;

            // else‑Zweig
            if (ctx.ELSE() != null) {
                nl();
                write("else");
                nl();
                currentIndent++;
                visit(ctx.statement(1));
                currentIndent--;
            }

            return null;
        }

        // -------------------------
        // 4) while (...) statement
        // -------------------------
        if (ctx.WHILE() != null) {
            write("while (");
            visit(ctx.expression());
            write(")");
            nl();

            currentIndent++;
            visit(ctx.statement(0));
            currentIndent--;

            return null;
        }

        // -------------------------
        // 5) expression ;
        // -------------------------
        if (ctx.expression() != null) {
            visit(ctx.expression());
            write(";");
            return null;
        }

        return null;
    }

    // @Override
  //public Void visitStatement(MiniJavaParser.StatementContext ctx) {
    // TODO:
    // Ensure that each statement (if/while/return/block/...) ends up
    // on exactly one line, with proper indentation for nested statements.
    //return null;
//  }

  // ---------------- helper methods ----------------

  private void indent() {
    if (atLineStart) {
      out.repeat(" ", Math.max(0, indentWidth * currentIndent));
      atLineStart = false;
    }
  }

  private void write(String s) {
    if (s == null || s.isEmpty()) return;
    indent();
    out.append(s);
  }

  private void nl() {
    out.append('\n');
    atLineStart = true;
    lastToken = null; // Reset spacing context at the beginning of a line
  }

  private void writeln(String s) {
    write(s);
    nl();
  }

  // --------------- token output + basic spacing ---------------

  @Override
  public Void visitTerminal(TerminalNode node) {
    Token t = node.getSymbol();
    String text = t.getText();

    if (lastToken != null) {
      int prevType = lastToken.getType();
      int curType = t.getType();

      // Simple heuristic: insert a space between "word-like" tokens
      if (needsSpaceBetween(prevType, curType)) write(" ");
    }

    write(text);
    lastToken = t;
    return null;
  }

  private boolean needsSpaceBetween(int prevType, int curType) {
    return isWordLike(prevType) && isWordLike(curType);
  }

  private boolean isWordLike(int type) {
    return type == MiniJavaLexer.IDENTIFIER
        || type == MiniJavaLexer.STRING_LITERAL
        || type == MiniJavaLexer.CHAR_LITERAL
        || type == MiniJavaLexer.NULL
        || type == MiniJavaLexer.PACKAGE
        || type == MiniJavaLexer.IMPORT
        || type == MiniJavaLexer.CLASS
        || type == MiniJavaLexer.PUBLIC
        || type == MiniJavaLexer.PRIVATE
        || type == MiniJavaLexer.FINAL
        || type == MiniJavaLexer.RETURN
        || type == MiniJavaLexer.NEW
        || type == MiniJavaLexer.IF
        || type == MiniJavaLexer.ELSE
        || type == MiniJavaLexer.WHILE
        || type == MiniJavaLexer.EXTENDS
        || type == MiniJavaLexer.IMPLEMENTS;
  }
}
