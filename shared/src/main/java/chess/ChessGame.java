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
        this.board = new ChessBoard(); // initialize board
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
        ArrayList<ChessMove> validMoves = new ArrayList<>();

        // add any moves that don't leave king in check
        TeamColor pieceColor = piece.getTeamColor();
        for (ChessMove move : possibleMoves) {
            ChessPiece captured = board.getPiece(move.getEndPosition());

            // temporarily apply move to the board
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);

            if (!isInCheck(pieceColor)) {
                // king not in check, add it to valid moves
                validMoves.add(move);
            }

            // undo temporary move aka put board back
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), captured);
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

        // check if move is a promotion move
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                (move.getEndPosition().getRow() == 8 || move.getEndPosition().getRow() == 1)) {
            ChessPiece.PieceType promoType = move.getPromotionPiece() != null ? move.getPromotionPiece() : ChessPiece.PieceType.QUEEN;
            piece = new ChessPiece(piece.getTeamColor(), promoType);
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
        if (king == null) {
            throw new RuntimeException("invalid board... no king found");
        }
        TeamColor opColor = ((teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
        // go through other teams pieces then those possible moves and see if any hit our king?
        for (int row = 1; row<= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                // iterate to find other team's pieces
                ChessPosition currPos = new ChessPosition(row, col);
                ChessPiece currPiece = board.getPiece(currPos);
                // if piece belongs to op
                if (currPiece != null && currPiece.getTeamColor() == opColor) {
                    // get posssible moves
                    Collection<ChessMove> possibleMoves = currPiece.pieceMoves(getBoard(), currPos);
                    // check if any of the possibleMoves go to the king's position
                    for (ChessMove move : possibleMoves) {
                        // return if the king is in check
                        return move.getEndPosition() == king;
                    }
                }
            }
        }
        // king is not in check
        return false;
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
        // when a king is in check and has no legal moves to escape
        // first, check if the king is in check
        // next, check if the king has any legal moves to escape
        if (isInCheck(teamColor)) {
            // find all possible moves for the team
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPosition currPos = new ChessPosition(row, col);
                    ChessPiece currPiece = board.getPiece(currPos);

                    if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                        // piece belongs to your team, see if it has any valid moves (aka can save king)
                        Collection<ChessMove> possibleMoves = validMoves(currPos);

                        // if there is at least one valid move, it is not in checkmate
//                        if (possibleMoves != null && !possibleMoves.isEmpty()) {
                        if (!possibleMoves.isEmpty()) {
                            // no possible moves, it is in checkmate
                            return false;
                        }

                    }

                }
            }
            return true;
        }
        // king not in check, so checkmate is impossible
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // return true if the king not in check and they have no legal moves left
        if (isInCheck(teamColor)){
            return false;
        }

        // see if any pieces for the given team have any moves left
        for (int row = 1; row<= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPos = new ChessPosition(row, col);
                ChessPiece currPiece = board.getPiece(currPos);

                if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                    // get possible moves
                    Collection<ChessMove> possibleMoves = validMoves(currPos);

                    // if any piece has a valid move, return false
                    if (!possibleMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        // no valid moves found... in stalemate
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
}
