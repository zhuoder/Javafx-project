package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  /**
   * use to play music in game
   */
  private Multimedia multimedia = new Multimedia();

  /**
   * create object of challengeScene
   */
  private ChallengeScene challengeScene = new ChallengeScene(gameWindow);

  /**
   * menuPane which is used to hold all the element
   */
  private StackPane menuPane;

  /**
   * new pane which is used to change the background
   */
  public static StackPane changePane = new StackPane();


  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }


  /**
   * Build the menu layout
   */
  @Override
  public void build() {

    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    root.getChildren().add(menuPane);

    Pane darkPane = new StackPane();
    darkPane.getStyleClass().setAll("menu-background");
    Pane lightPane = new StackPane();
    lightPane.getStyleClass().setAll("menu-background-light");
    ObservableList<String> challengeStyleClasses = changePane.getStyleClass();
    ObservableList<String> darkStyleClasses = darkPane.getStyleClass();
    ObservableList<String> lightStyleClasses = lightPane.getStyleClass();
    if (challengeStyleClasses.equals(darkStyleClasses)) {
      menuPane.getStyleClass().setAll("menu-background");
    } else if (challengeStyleClasses.equals((lightStyleClasses))) {
      menuPane.getStyleClass().setAll("menu-background-light");
    } else {
      menuPane.getStyleClass().setAll("menu-background");
    }

    //improved title
    Image title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
    ImageView imageView = new ImageView(title);
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(100);
    imageView.setTranslateY(-100);
    menuPane.getChildren().add(imageView);

    //add rotate animation
    RotateTransition rotateTransition = new RotateTransition(Duration.millis(4000), imageView);
    rotateTransition.setFromAngle(-180);
    rotateTransition.setToAngle(180);
    rotateTransition.setCycleCount(Animation.INDEFINITE);
    rotateTransition.setAutoReverse(true);
    rotateTransition.play();


    //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
    var play = new Button("Single");
    play.getStyleClass().add("menuItem");
    var multiPlayer = new Button("Multi");
    multiPlayer.getStyleClass().add("menuItem");
    var instructions = new Button("How to Play");
    instructions.getStyleClass().add("menuItem");
    var setting = new Button("Settings");
    setting.getStyleClass().add("menuItem");

    var hbox = new HBox(10, play, multiPlayer, instructions, setting);
    menuPane.getChildren().add(hbox);
    hbox.setAlignment(Pos.BOTTOM_CENTER);
    //Bind the button action to the startGame method in the menu
    play.setOnAction(this::startGame);
    instructions.setOnAction(this::startInstructions);
    setting.setOnAction(this::startSetting);
    multiPlayer.setOnAction(this::startMultiPlayer);

  }

  /**
   * Initialise the menu
   */
  @Override
  public void initialise() {
    //initialise here because we first create scene and then initialise the menu
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        logger.info("Escape Pressed");
        System.exit(0);
      }
    });
    //start to play music
    multimedia.playBackgroundMusic("menu.mp3");
  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    //startChallenge方法会打开游戏
    gameWindow.startChallenge();
    multimedia.playAudio("transition.wav");
    multimedia.stopPlaying();
  }

  /**
   * Handel when instruction button is pressed
   *
   * @param event instruction event
   */
  private void startInstructions(ActionEvent event) {
    gameWindow.startInstructions();
    multimedia.playAudio("transition.wav");
    multimedia.stopPlaying();
  }

  /**
   * handle when setting button is pressed
   *
   * @param event setting event
   */
  public void startSetting(ActionEvent event) {
    gameWindow.startSetting();
    multimedia.playAudio("transition.wav");
    multimedia.stopPlaying();
  }

  /**
   * handle when setting button is pressed
   *
   * @param event setting event
   */
  public void startMultiPlayer(ActionEvent event) {
    gameWindow.startLobby();
    multimedia.playAudio("transition.wav");
    multimedia.stopPlaying();
  }


}
