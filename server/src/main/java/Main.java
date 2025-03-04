import chess.*;
import server.Server;
import service.ClearService;
import service.UserService;
import service.GameService;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        // create a Server object, and then call run on it.
        Server server = new Server();
        server.run(8080);
    }
}