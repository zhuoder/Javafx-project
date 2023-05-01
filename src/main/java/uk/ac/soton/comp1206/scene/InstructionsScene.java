package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * this class is for instruction scene
 */
public class InstructionsScene extends BaseScene {
  /**
   * the logger for instruction
   */
  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
  /**
   * object of multimedia
   */
  Multimedia multimedia = new Multimedia();

  /**
   * the grid pane
   */
  GridPane gridPane;
  /**
   * the mainPane, which is a borderPane
   */
  BorderPane mainPane;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  /**
   * initialise the scene
   */
  @Override
  public void initialise() {
    //Escape Key Event,we return to menu window
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        multimedia.playAudio("transition.wav");
        //use gameWindow to return to menu
        gameWindow.startMenu();
        logger.info("Escape Pressed");
      }
    });
    multimedia.playBackgroundMusic("menu.mp3");

  }

  /**
   * build the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    var instructions = new Text("Instructions");
    instructions.getStyleClass().add("heading");
    BorderPane.setAlignment(instructions, Pos.CENTER);
    mainPane.setTop(instructions);

    createImage();

    var pieces = new Text("Pieces");
    mainPane.getChildren().add(pieces);
    pieces.getStyleClass().add("heading");
    StackPane.setAlignment(pieces, Pos.CENTER);
    pieces.setLayoutY(500);
    pieces.setLayoutX(400 - pieces.getLayoutBounds().getWidth());

    gridPane = new GridPane();
    gridPane.setPrefSize(100, gameWindow.getWidth());
    createShowPiece();
    mainPane.getChildren().add(gridPane);
    gridPane.setLayoutY(450);
    gridPane.setLayoutX(90);
    gridPane.setVgap(5);
    gridPane.setHgap(20);
  }

  /**
   * method used to create the piece
   */
  public void createShowPiece() {
    for (int i = 0; i < 15; i++) {
      PieceBoard pieceBoard = new PieceBoard(3, 3, 60, 60);
      pieceBoard.setPiece(GamePiece.createPiece(i));
      if (i < 7) {
        gridPane.add(pieceBoard, i, 0);
      } else {
        gridPane.add(pieceBoard, i - 7, 1);
      }
    }
  }

  public void createImage() {
    Image instruction = new Image(MenuScene.class.getResource("/images/Instructions.png").toExternalForm());
    ImageView picture = new ImageView(instruction);
    picture.setPreserveRatio(true);
    picture.setFitWidth(500);
    picture.setX(100);
    picture.setY(45);
    mainPane.getChildren().add(picture);
  }

}
