package uk.ac.soton.comp1206.event;

/**
 * listener used every time we loop the game
 */
public interface GameLoopListener {


  /**
   * abstract method which is used to
   *
   * @param time the time that the time bar need to play
   */
  public void gameLoop(int time);


}
