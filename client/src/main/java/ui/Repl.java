package ui;

import server.ServerFacade;

import java.util.Scanner;


public class Repl {

    private final ChessClient client;
    private final ServerFacade server;

    public Repl(String serverUrl) {
        this.client = new ChessClient(serverUrl);
        this.server = new ServerFacade(serverUrl);

//        server = new Server();
//        var port = server.run(0);
    }

    public void run() {
        System.out.println("Welcome to my chess game! Please log in to get started");
        Scanner scanner = new Scanner(System.in);

        PreLoginUI preLoginUI = new PreLoginUI(client, server);

        var result = "";

        while (!result.equals("quit")) {
        // loop until user logs in
        while (client.getAuthData() == null) {
            System.out.print("\n>>> ");
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        PostLoginUI postLoginUI = new PostLoginUI(client, server);

//        while (!result.equals("quit")) {
            System.out.print("\n>>> ");
            String line = scanner.nextLine();

            try {
                result = postLoginUI.eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
