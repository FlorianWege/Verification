package gui;

import core.Hoare;
import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.prog.HoareCond;
import core.structures.semantics.prog.Skip;
import gui.hoare.ConseqCheckPostDialog;
import gui.hoare.HoareDialog;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConseqCheckPostDialogTest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@Nonnull Stage primaryStage) throws IOException, Lexer.LexerException, Parser.ParserException {
        HoareCond origPostCond = new HoareCond(BoolExp.fromString("x=3|x=0-3"));
        HoareCond newPostCond = new HoareCond(BoolExp.fromString("x=3|x=0-3"));

        HoareDialog dialog = new ConseqCheckPostDialog(new Skip(), origPostCond, newPostCond, new ConseqCheckPostDialog.Callback() {
            @Override
            public void result(BoolExp boolExp) throws Lexer.LexerException, Hoare.HoareException, Parser.ParserException, IOException {

            }
        });

        primaryStage.setScene(dialog.getRoot().getScene());
        primaryStage.show();
    }
}
