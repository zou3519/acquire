package acquire.gui;

import acquire.gui.acquireUI.*;
import javafx.application.Application;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Main is the entry point for this project
 */
public class Main {
    static boolean PRODUCTION = true;
    public static void main(String[] args) throws FileNotFoundException {
        if (PRODUCTION) {
            System.setOut(new PrintStream("log.out"));
            System.setErr(new PrintStream("log.err"));
        }
        Application.launch(AcquireTheatre.class, args);
    }
}