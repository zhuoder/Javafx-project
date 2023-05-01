package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;


/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {
  /**
   * The game board we would put our black in.
   */
  private final GameBoard gameBoard;

  /**
   * The width of the block
   */
  private final double width;
  /**
   * The height of the block
   */
  private final double height;

  /**
   * a boolean value which indicates if the block is center block
   */
  private boolean ifCenter = false;

  /**
   * the boolean variable, true for we're now hovering over a block
   */
  private boolean ifHover = false;
  /**
   * the opacity of the line
   */
  private double opacity;

  /**
   * This function is utilized to generate an animation demonstrating the process of block deletion.
   */
  private AnimationTimer animationTimer;

  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
          Color.TRANSPARENT,
          Color.DEEPPINK,
          Color.RED,
          Color.ORANGE,
          Color.YELLOW,
          Color.YELLOWGREEN,
          Color.LIME,
          Color.GREEN,
          Color.DARKGREEN,
          Color.DARKTURQUOISE,
          Color.DEEPSKYBLUE,
          Color.AQUA,
          Color.AQUAMARINE,
          Color.BLUE,
          Color.MEDIUMPURPLE,
          Color.PURPLE
  };


  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);
  //The listener is set to the value, so whenever the value changes, it will recolor

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);

  }

  /**
   * set hover on the main board by adding a new listener to hoverProperty
   * when move in we set it to semi-transparent
   */
  public void setIfHover() {
    //This is equivalent to adding a listener to each block as it initializes,
//and changing the color of the current block when the cursor moves in
    hoverProperty().addListener((observableValue, moveOut, moveIn) -> {

      if (moveIn) {
        ifHover = true;
        hoverPaint();
        if (gameBoard.boardXProperty().get() != 0 || gameBoard.boardYProperty().get() != 0) {
          gameBoard.getBlock(gameBoard.boardXProperty().get(), gameBoard.boardYProperty().get()).paint();
        }
      } else if (moveOut) {
        paint();
        ifHover = false;
      }
    });
  }

  /**
   * When the value of this block is updated, we would paint the block accordingly
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }
    if (ifCenter) {
      //paints centre dot
      var gc = getGraphicsContext2D();
      gc.setFill(Color.WHITE); // Set fill color to red
      gc.setStroke(Color.WHITE);
      gc.fillOval(width / 4, height / 4, width / 2, height / 2);
    }
  }

  /**
   * Paint this canvas empty
   */
  private void paintEmpty() {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Fill
    gc.setFill(Color.WHITE);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.LIGHTGRAY);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint this canvas with the given colour
   *
   * @param colour the colour to paint
   */
  private void paintColor(Paint colour) {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Colour fill
    Stop[] stops = new Stop[]{new Stop(0, Color.WHITE), new Stop(1, (Color) colour)};
    LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

    gc.setFill(gradient);
    gc.fillRoundRect(0, 0, width, height, 10, 10);

    //Border
    gc.setStroke(Color.BLACK);
    gc.strokeRoundRect(0, 0, width, height, 10, 10);
  }

  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing its colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

  /**
   * set the ifCenter to true to show that current block is at the center of the board
   */
  public void setIfCenter() {
    ifCenter = true;
  }

  /**
   * set the color to semi-transparent when hovering over a block
   */
  public void hoverPaint() {
    //Place your cursor over the block and change the color to translucent
    var gc = getGraphicsContext2D();
    gc.setFill(COLOURS[value.get()].deriveColor(0, 0, 1, 0.6));
    gc.fillRect(0, 0, width, height);
    if (value.get() == 0) {
      gc.setFill(Color.GREY.deriveColor(0, 0, 1, 0.6));
      gc.fillRect(0, 0, width, height);
    }
  }



  /**
   * use to show a process of clearing a line
   * first let the block turn to empty and then decrease the opacity
   * let the block show the turning color
   */
  public void fade() {
    animationTimer = new AnimationTimer() {
      double opacity = 0.8;

      @Override
      public void handle(long m) {
        //handel method would loop until we meet stop
        paintEmpty();
        opacity = opacity - 0.01;
        if (opacity <= 0.1) {
          stop();
          animationTimer = null;
        } else {
          var gc = getGraphicsContext2D();
          gc.setFill(Color.color(0, 0, 1, opacity));
          gc.fillRect(0, 0, width, height);
        }
      }
    };
    animationTimer.start();
  }

  /**
   * return if we've hovered over a block
   *
   * @return true for we've hovered a block
   */
  public boolean getHover() {
    return ifHover;
  }

}



