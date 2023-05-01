package uk.ac.soton.comp1206.event;


import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * listener which used when a line need to be cleared
 */
public interface LineFadeListener {
  /**
   * abstract method which is used for clearing a line
   *
   * @param gameBlockCoordinates the coordinate of aiming blocks
   */
  public void lineToClear(HashSet<GameBlockCoordinate> gameBlockCoordinates);
}
