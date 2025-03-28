package ui;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {

    private final ChessClient client;

    public Repl(String serverUrl) {
        this.client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to my chess game! Please log in to get started");
        Scanner scanner = new Scanner(System.in);

        var result = "";

        while (!result.equals("quit")) {
            // loop until user says they want to quit
            System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print("\n" + SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
