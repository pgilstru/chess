package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private boolean gameOver;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE; // white is starting team
        this.board = new ChessBoard(); // initialize board
        this.board.resetBoard();
        this.gameOver = false;
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
        ChessPiece piece = board.getPiece(startPosition);

        // check for piece at startPosition
        if (piece == null) {
            return null;
        }

        // get possible moves and add valid ones (that don't leave king in check)
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        TeamColor pieceColor = piece.getTeamColor();

        for (ChessMove move : possibleMoves) {
            ChessPiece captured = board.getPiece(move.getEndPosition());

            // temporarily apply move to the board
            tempMoves(move, piece, captured, true);

            if (!isInCheck(pieceColor)) {
                // king not in check, add it to valid moves
                validMoves.add(move);
            }

            // undo temporary move aka put board back
            tempMoves(move, piece, captured, false);
        }

        return validMoves;
    }

    private void tempMoves(ChessMove move, ChessPiece piece, ChessPiece captured, boolean applyMove) {
        if (applyMove) {
            // we need to apply a temporary move
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
        } else {
            // we need to undo our temporary move
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), captured);
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // check if the game is over
        if (gameOver) {
            throw new InvalidMoveException("Game is over");
        }

        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("piece is null");
        }

        if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move)) {
            // not a valid move
            throw new InvalidMoveException("not a valid move");
        }

        // check if move is a pawn promotion move
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                (move.getEndPosition().getRow() == 8 || move.getEndPosition().getRow() == 1)) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        // add piece to new pos and remove it from old pos
        tempMoves(move, piece,null, true);

        // switch team turns
        setTeamTurn(teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);

        // after moving, check for any game end conditions
        checkGameEndConditions();
    }

    private void checkGameEndConditions() {
        if (isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK) ||
        isInStalemate(TeamColor.WHITE) || isInStalemate(TeamColor.BLACK)) {
            // set gameOver to true
            gameOver = true;
        }
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getGameState() {
        // gets current game state as a string
        if (gameOver) {
            return "Game is over";
        }
        if (isInCheckmate(TeamColor.WHITE)) {
            return "White team is in checkmate";
        }
        if (isInCheckmate(TeamColor.BLACK)) {
            return "Black team is in checkmate";
        }
        if (isInStalemate(TeamColor.WHITE)) {
            return "White team is in stalemate";
        }
        if (isInStalemate(TeamColor.BLACK)) {
            return "Black team is in stalemate";
        }
        if (isInCheck(TeamColor.WHITE)) {
            return "White team is in check";
        }
        if (isInCheck(TeamColor.BLACK)) {
            return "Black team is in check";
        }

        return "game is in progress";
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // find king's position
        ChessPosition kingPos = getKing(teamColor);
        if (kingPos == null) {
            throw new RuntimeException("invalid board... no king found");
        }

        TeamColor opColor = ((teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);

        // for opposite team's current positions, check if any can capture the king
        for (ChessPosition position : findAllPositions()) {
            ChessPiece currPiece = board.getPiece(position);
            if (currPiece != null && currPiece.getTeamColor() == opColor) {
                Collection<ChessMove> possibleMoves = currPiece.pieceMoves(board, position);

                // check if any of the possibleMoves go to the king's position
                for (ChessMove move : possibleMoves) {
                    // return if the king is in check
                    if (move.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }
            }
        }
        // king is not in check
        return false;
    }

    private Iterable<ChessPosition> findAllPositions() {
        ArrayList<ChessPosition> allPositions = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                allPositions.add(new ChessPosition(row, col));
            }
        }
        return allPositions;
    }

    public ChessPosition getKing(TeamColor teamColor) {
        for (ChessPosition currPos : findAllPositions()) {
            ChessPiece currPiece = board.getPiece(currPos);
            if (currPiece != null && currPiece.getPieceType() == ChessPiece.PieceType.KING
                    && currPiece.getTeamColor() == teamColor) {
                // found the king's position, return it
                return currPos;
            }
        }
        // if for some reason the board is invalid, return null
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // return true if the king is in check and there are no legal moves left
        return isInCheck(teamColor) && hasNoMovesLeft(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // return true if the king is not in check and there are no legal moves left
        return !isInCheck(teamColor) && hasNoMovesLeft(teamColor);
    }

    private boolean hasNoMovesLeft(TeamColor teamColor) {
        // see if any pieces for the given team have any legal moves left
        for (ChessPosition currPos : findAllPositions()) {
            ChessPiece currPiece = board.getPiece(currPos);

            if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                // check if any pieces have valid moves
                Collection<ChessMove> possibleMoves = validMoves(currPos);

                // found valid move
                if (!possibleMoves.isEmpty()){
                    return false;
                }
            }
        }
        // no valid moves found
        return true;
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


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}