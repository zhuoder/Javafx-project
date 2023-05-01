package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * end game listener is used to give feedback when the game is end
 */
public interface gameStopListener {

  /**
   * abstract method which is used to handle when game ends
   *
   * @param game current game
   */
  public void endGame(Game game);

}
