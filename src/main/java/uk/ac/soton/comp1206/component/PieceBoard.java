package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;


/**
 * this class is used to show the pieceBoard(on the side of screen)
 */
public class PieceBoard extends GameBoard {


  /**
   * constructor of piece board
   *
   * @param cols   the number of columns
   * @param rows   the number of rows
   * @param width  the width of the board
   * @param height the height of the board
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);
  }


  /**
   * place the upcoming piece in the grid
   *
   * @param piece upcoming piece
   */
  public void setPiece(GamePiece piece) {
    grid.reset();

    grid.playPiece(piece, 1, 1);
  }


}
