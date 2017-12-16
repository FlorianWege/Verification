package gui;

import core.Lexer;
import core.Parser;
import core.structures.semantics.boolExp.BoolExp;
import core.structures.semantics.boolExp.BoolImpl;
import core.structures.semantics.boolExp.BoolLit;
import gui.hoare.ConseqCheckTableView;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConseqCheckTableViewTest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@Nonnull Stage primaryStage) throws IOException, Lexer.LexerException, Parser.ParserException {
        BoolExp origPostCond = BoolExp.fromString("x=3|x=0-3");
        BoolExp newPostCond = BoolExp.fromString("x=3|x=0-3");

        BoolImpl impl = new BoolImpl(origPostCond, newPostCond);

        ConseqCheckTableView tableView = new ConseqCheckTableView(new ConseqCheckTableView.Callback() {
            @Override
            public void result(@Nonnull BoolExp boolExp) {
                if (boolExp.equals(new BoolLit(true))) {
                    System.out.println("yes");
                } else {
                    System.out.println("no");
                }
            }
        });

        tableView.setBoolImpl(impl);

        primaryStage.setScene(tableView.getRoot().getScene());
        primaryStage.show();
    }
}
