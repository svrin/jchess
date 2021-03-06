package jchess;

import jchess.mvc.Controller;
import jchess.mvc.events.*;
import jchess.pieces.*;
import jchess.util.BoardCoordinate;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;


/**
 * Created by andreas on 07.12.14.
 */
@Listener(references = References.Strong)
public class Game {

    private Player[] players;
    private Board board;
    private Tile selectedTile;
    private Tile promotionTile;
    private BoardCoordinate selectedBC;
    private int activePlayerID;
    private HashSet<BoardCoordinate> possibleMovesCoordinates;
    private HashSet<PieceNames> possiblePromotions;

    private Game(Player[] players) {
        Controller.INSTANCE.subscribe(this);
        this.players = players;
        this.board = new Board();
        this.possibleMovesCoordinates = new HashSet<>();
        this.possiblePromotions = new HashSet<>();
    }

    public static Game newGame(Player[] players) throws IllegalArgumentException {
        if (players.length != 3) {
            throw new IllegalArgumentException();
        }
        Game game = new Game(players);
        for (Player player : players) {
            player.setGame(game);
        }
        return game;
    }

    public Board getBoard() {
        return board;
    }

    public void emitInvalidSelectEvent() {
        InvalidSelectEvent invalidSelectEvent = new InvalidSelectEvent(this);
        Logging.GAME.debug("Game: Emit InvalidSelectEvent");
        invalidSelectEvent.emit();
    }

    public void emitPossibleMovesEvent() {
        collectPossibleMoveCoordinates();
        PossibleMovesEvent possibleMovesEvent = new PossibleMovesEvent(this, possibleMovesCoordinates);
        Logging.GAME.debug("Game: Emit PossibleMovesEvent");
        possibleMovesEvent.emit();
    }

    public void emitPossiblePromotionsEvent() {
        collectPossiblePromotions();
        PossiblePromotionsEvent possiblePromotionsEvent = new PossiblePromotionsEvent(this, possiblePromotions);
        Logging.GAME.debug("Game: Emit PossiblePromotionsEvent");
        possiblePromotionsEvent.emit();
    }

    public void emitUpdateBoardEvent() {
        UpdateBoardEvent updateBoardEvent = new UpdateBoardEvent(this);
        Logging.GAME.debug("Game: Emit UpdateBoardEvent");
        updateBoardEvent.emit();
    }

    @Handler
    public void handleSelectEvent(SelectEvent selectEvent) {
        if (selectEvent.shouldReceive(this)) {
            Logging.GAME.debug(selectEvent.getGame() + " Received SelectEvent");
            selectedBC = selectEvent.getBoardCoordinate();
            if (selectedTile == null) {
                Tile tile = board.getTile(selectEvent.getBoardCoordinate().getI());
                if (tile.getPiece() == null || !players[tile.getPiece().getPlayerID()].isActive()) {
                    emitInvalidSelectEvent();
                } else {
                    selectedTile = tile;
                    emitPossibleMovesEvent();
                }
            } else {
                Tile tile = board.getTile(selectEvent.getBoardCoordinate().getI());

                if (tile.getPiece() == null || !players[tile.getPiece().getPlayerID()].isActive()) {
                    if (possibleMovesCoordinates.contains(selectEvent.getBoardCoordinate())) {
                        Piece selectedPiece = selectedTile.getPiece();
                        selectedTile.removePiece();
                        tile.placePiece(selectedPiece);

                        activePlayerID++;
                        activePlayerID %= 3;
                        for (int i = 0; i < 3; i++) {
                            players[i].setActive(i == activePlayerID);
                        }

                        selectedTile = null;
                        emitUpdateBoardEvent();

                        if (tile.isPromotionTileFor(selectedPiece.getPlayerID())) {
                            promotionTile = tile;
                            emitPossiblePromotionsEvent();
                        }
                    } else {
                        emitInvalidSelectEvent();
                    }
                } else {
                    selectedTile = tile;
                    emitPossibleMovesEvent();
                }
            }
        }
    }

    @Handler
    public void handlePromotionSelectEvent(PromotionSelectEvent promotionSelectEvent) {
        if (promotionSelectEvent.shouldReceive(this)) {
            Logging.GAME.debug(promotionSelectEvent.getGame() + " Received PromotionSelectEvent");

            try {
                Piece piece = (Piece)promotionSelectEvent.getPieceNames().getPiece().getConstructors()[0].newInstance(promotionTile.getPiece().getPlayerID());
                promotionTile.placePiece(piece);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            emitUpdateBoardEvent();
        }
    }

    private void collectPossibleMoveCoordinates() {
        possibleMovesCoordinates.clear();

        Piece piece = selectedTile.getPiece() ;
        // repeat
        for( BoardCoordinate repeatBC : piece.getTileFilter().getRepeat() )  {
            BoardCoordinate resultBC = new BoardCoordinate( selectedBC.getA() + repeatBC.getA() , selectedBC.getB() + repeatBC.getB() ) ;
            while(  ( resultBC.getA() >=  0 && resultBC.getA() < 15 ) &&
                    ( resultBC.getB() >=  0 && resultBC.getB() < 15 ) &&
                    ( resultBC.getC() >= -7 && resultBC.getC() <= 7 )   ) {

                // Need to know if any piece is in the possible move trajectory
                Piece pieceOnResultBC = board.getTile( resultBC ).getPiece();

                // Stop collecting tiles if another activePlayer piece is in the trajectory
                if ( pieceOnResultBC != null && activePlayerID == pieceOnResultBC.getPlayerID() ) break;

                possibleMovesCoordinates.add( resultBC );

                // if we got here and pieceOnResultBC is not null, pieceOnResultBC is an enemy
                if ( pieceOnResultBC != null ) break;

                // continue trajectory with new BoardCoordinates
                resultBC = new BoardCoordinate( resultBC.getA() + repeatBC.getA() , resultBC.getB() + repeatBC.getB() ) ;
            }
        }

        // single
        for( BoardCoordinate singleBC : piece.getTileFilter().getSingle() ) {
            BoardCoordinate resultBC = new BoardCoordinate(selectedBC.getA() + singleBC.getA(), selectedBC.getB() + singleBC.getB());
            if (    ( resultBC.getA() >=  0 && resultBC.getA() < 15 ) &&
                    ( resultBC.getB() >=  0 && resultBC.getB() < 15 ) &&
                    ( resultBC.getC() >= -7 && resultBC.getC() <= 7 )) {

                // Don't collect tile if another activePlayer piece is on it
                Piece pieceOnResultBC = board.getTile(resultBC).getPiece();
                if ( pieceOnResultBC == null )
                    possibleMovesCoordinates.add(resultBC);
            }
        }

        // singleKill
        for( BoardCoordinate singleKillBC : piece.getTileFilter().getSingleKill() ) {
            BoardCoordinate resultBC = new BoardCoordinate(selectedBC.getA() + singleKillBC.getA(), selectedBC.getB() + singleKillBC.getB());
            if ((resultBC.getA() >= 0 && resultBC.getA() < 15) &&
                    (resultBC.getB() >= 0 && resultBC.getB() < 15) &&
                    (resultBC.getC() >= -7 && resultBC.getC() <= 7)) {

                // Only collect tile if another players piece is on it
                Piece pieceOnResultBC = board.getTile(resultBC).getPiece();
                if ( pieceOnResultBC != null && activePlayerID != pieceOnResultBC.getPlayerID() )
                    possibleMovesCoordinates.add(resultBC);
            }
        }

        //possibleMovesCoordinates.add(new BoardCoordinate(5, 5));
        //possibleMovesCoordinates.add(new BoardCoordinate(6, 6));
        //possibleMovesCoordinates.add(new BoardCoordinate(7, 7));

    }

    private void collectPossiblePromotions() {
        possiblePromotions.clear();

        possiblePromotions.add(PieceNames.QUEEN);
        possiblePromotions.add(PieceNames.BISHOP);
        possiblePromotions.add(PieceNames.KNIGHT);
        possiblePromotions.add(PieceNames.ROOK);
    }
}
