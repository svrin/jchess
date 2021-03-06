package jchess;

import jchess.mvc.events.*;
import net.engio.mbassy.listener.Handler;

/**
 * Created by andreas on 06.12.14.
 */
public interface IOSystem {
    @Handler void handleSelectEvent(SelectEvent selectEvent);
    @Handler void handleUpdateBoardEvent(UpdateBoardEvent updateBoardEvent);
    @Handler void handlePossibleMovesEvent(PossibleMovesEvent possibleMovesEvent);
    @Handler void handleInvalidSelectEvent(InvalidSelectEvent invalidSelectEvent);
    @Handler void handlePossiblePromotionsEvent(PossiblePromotionsEvent possiblePromotionsEvent);
    @Handler void handlePromotionSelectEvent(PromotionSelectEvent promotionSelectEvent);
    public void setPlayer(Player player);
}
