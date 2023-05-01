package uk.ac.soton.comp1206.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

  /**
   * logger for gameBoard
   */
  private static final Logger logger = LogManager.getLogger(GameBoard.class);

  /**
   * Number of columns in the board
   */
  private final int cols;

  /**
   * Number of rows in the board
   */
  private final int rows;

  /**
   * The visual width of the board - has to be specified due to being a Canvas
   */
  private final double width;

  /**
   * The visual height of the board - has to be specified due to being a Canvas
   */
  private final double height;

  /**
   * The grid this GameBoard represents
   */
  protected final Grid grid;

  /**
   * The blocks inside the grid
   */
  GameBlock[][] blocks;

  /**
   * The listener to call when a specific block is clicked
   */
  private BlockClickedListener blockClickedListener;

  /**
   * The listener to call when a specific block is right clicked
   */
  private RightClickedListener rightClickedListener;

  /**
   * the integer property of coordinate X
   */
  private IntegerProperty boardX = new SimpleIntegerProperty();

  /**
   * the integer property of coordinate Y
   */
  private IntegerProperty boardY = new SimpleIntegerProperty();

  /**
   * return the propertyX
   *
   * @return the property of X
   */
  public IntegerProperty boardXProperty() {
    return boardX;
  }

  /**
   * return the propertyY
   *
   * @return the property of Y
   */
  public IntegerProperty boardYProperty() {
    return boardY;
  }

  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid   linked grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height) {
    //用于创建游戏board，即棋盘
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;

    //Build the GameBoard
    build();
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
   * visual width and height.
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height) {
    //用于构建pieceBoard
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);

    //Build the GameBoard
    build();
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Build the GameBoard by creating a block at every x and y column and row
   */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        createBlock(x, y);
      }
    }
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    //创建一个块在xy上
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    //Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

    //Add to the GridPane
    add(block, x, y);

    //Add to our block directory
    blocks[x][y] = block;

    //Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));
    //将block与grid绑定在一起，这也是为什么block能变色，是因为在grid类中我们用了playPiece方法，改变了grid的值，
    //进而改变了block的value，而value有监听器，会给block重新上色

    //Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked((e) -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        blockClicked(e, block);
      } else {
        //set right click listener
        rightClicked(e, block);
      }
    });

    return block;
  }

  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Set the listener to handle an event when a block is right-clicked
   *
   * @param listener listener to add
   */
  public void setOnRightClicked(RightClickedListener listener) {
    this.rightClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param event mouse event
   * @param block block clicked on
   */
  private void blockClicked(MouseEvent event, GameBlock block) {
    logger.info("Block clicked: {}", block);

    if (blockClickedListener != null) {
      blockClickedListener.blockClicked(block);
    }
  }

  /**
   * Triggered when a block is right-clicked. Call the attached listener.
   *
   * @param event mouse event
   * @param block block clicked on
   */
  private void rightClicked(MouseEvent event, GameBlock block) {
    logger.info("Block Right clicked: {}", block);

    if (rightClickedListener != null) {
      rightClickedListener.rightClick(block);
    }
  }

  /**
   * give the center block a true boolean variable
   */
  public void paintCentre() {
    this.getBlock(1, 1).setIfCenter();
  }

  /**
   * get the array of blocks
   *
   * @return the array of blocks
   */
  public GameBlock[][] getBlocks() {
    return blocks;
  }

  /**
   * only the main board could be added hover
   */
  public void Hover() {
    for (GameBlock[] block : blocks) {
      for (GameBlock gameBlock : block) {
        //set hover effect only on main board while not other board
        gameBlock.setIfHover();
      }
    }
  }

  /**
   * call the listener of the blocks in the lines which need to be clear
   *
   * @param gameBlockCoordinates the coordinates set of block
   */
  public void fadeOut(Set<GameBlockCoordinate> gameBlockCoordinates) {

    for (GameBlockCoordinate gameBlockCoordinate : gameBlockCoordinates) {
      blocks[gameBlockCoordinate.getX()][gameBlockCoordinate.getY()].fade();//the blocks we need to set fade effect
    }
  }

}
