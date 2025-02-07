package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE; // white is starting team
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = getBoard().getPiece(startPosition);

        // check for piece at startPosition
        if (piece == null) {
            return null;
        }
        // there is a piece, get possible moves
        Collection<ChessMove> possibleMoves = piece.pieceMoves(getBoard(), startPosition);
        ArrayList<ChessMove> validMoves = new ArrayList<>(possibleMoves);

        // remove any moves that leave king in check
        TeamColor pieceColor = piece.getTeamColor();
        for (ChessMove move : possibleMoves) {
            // temporarily apply move to the board
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            // check if king is in check now
            if (isInCheck(pieceColor)) {
                // remove it from the valid moves
                validMoves.remove(move);
            }

            // undo temporary move
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), null);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("piece is null");
        } else if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move)) {
            // not a valid move
            throw new InvalidMoveException("not a valid move");
        }

        // add piece to new pos and remove it from old pos
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        // switch team turns
        setTeamTurn(teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // get the pieces on the board for the opposite team
        // for each piece, get all of its possible moves
        // for each move, check if our team's king is in check

        // find king's position
        ChessPosition king = getKing(teamColor);
        TeamColor opColor = (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        // go through other teams possible moves and see if any hit our king?
//        ArrayList<ChessMove> validMoves = new ArrayList<>(possibleMoves);
//        for (ChessPiece piece : getBoard()){
//
//        }
    }

    public ChessPosition getKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPos = new ChessPosition(row, col);
                ChessPiece currPiece = board.getPiece(currPos);
                if (currPiece != null && currPiece.getPieceType() == ChessPiece.PieceType.KING
                        && currPiece.getTeamColor() == teamColor) {
                    // found the king's position, return it
                    return currPos;
                }
            }
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");

    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
