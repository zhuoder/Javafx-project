package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * listener that shows piece in piece board
 */
public interface NextPieceListener {

  /**
   * abstract method used for showing pieces
   *
   * @param currentPiece       current piece
   * @param followingGamePiece next piece
   */
  public void nextPiece(GamePiece currentPiece, GamePiece followingGamePiece);

}
