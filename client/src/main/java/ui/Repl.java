package ui;

import server.ServerFacade;

import java.util.Scanner;

public class Repl {

    private final ChessClient client;
    private final ServerFacade server;

    public Repl(String serverUrl) {
        this.client = new ChessClient(serverUrl);
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to my chess game! Please log in to get started");
        Scanner scanner = new Scanner(System.in);

        PreLoginUI preLoginUI = new PreLoginUI(client, server);

        var result = "";

        // loop until user logs in
        while (client.getAuthData() == null) {
            System.out.print(">>> ");
            String line = scanner.nextLine();

            try {
                result = new PreLoginUI(client, server).eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        PostLoginUI postLoginUI = new PostLoginUI(client, server);

        while (!result.equals("quit")) {
            System.out.print(">>> ");
            String line = scanner.nextLine();

            try {
                result = preLoginUI.eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
