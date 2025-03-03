package model;
import chess.ChessGame;

public record JoinRequest(ChessGame.TeamColor userColor, int gameID) {
}
