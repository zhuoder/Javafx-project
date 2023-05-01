package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.game.Game;

/**
 * listener which is called when we right-click a block
 */
public interface RightClickedListener {

  /**
   * abstract method which could handle event when we right-click a block
   *
   * @param block the block we right-click
   */
  public void rightClick(GameBlock block);


}
