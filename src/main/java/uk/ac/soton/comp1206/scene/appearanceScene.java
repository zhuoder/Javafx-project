package uk.ac.soton.comp1206.scene;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * this class is used to create a new scene which could control the appearance of game
 */
public class appearanceScene extends BaseScene {
  /**
   * logger for appearanceScene
   */
  private static final Logger logger = LogManager.getLogger(appearanceScene.class);
  /**
   * object of challengeScene
   */
  private ChallengeScene challengeScene = new ChallengeScene(gameWindow);

  /**
   * object of menuScene
   */
  private MenuScene menuScene = new MenuScene(gameWindow);

  /**
   * object of lobbyScene
   */
  private LobbyScene lobbyScene = new LobbyScene(gameWindow);


  /**
   * object of multimedia
   */
  Multimedia controlMultimedia = new Multimedia();

  /**
   * control the appearance's background
   */
  private static MediaPlayer backgroundPlayer;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public appearanceScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Setting Scene");
  }

  /**
   * initialise the appearance scene
   */
  @Override
  public void initialise() {
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        controlMultimedia.playAudio("transition.wav");
        backgroundPlayer.stop();
        controlMultimedia.stopPlaying();
        //use gameWindow to return to menu
        gameWindow.startMenu();
        logger.info("Escape Pressed");
      }
    });

    String musicToPlay = Multimedia.class.getResource("/music/" + "menu.mp3").toExternalForm();

    try {
      Media media = new Media(musicToPlay);
      backgroundPlayer = new MediaPlayer(media);
      //set cycle play
      backgroundPlayer.setAutoPlay(true);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);

      backgroundPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.toString());
    }


  }

  /**
   * build the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var settingPane = new BorderPane();
    settingPane.setMaxWidth(gameWindow.getWidth());
    settingPane.setMaxHeight(gameWindow.getHeight());
    settingPane.getStyleClass().add("menu-background");
    root.getChildren().add(settingPane);

    //set title
    Text title = new Text("Game Setting");
    title.getStyleClass().add("title");
    BorderPane.setAlignment(title, Pos.CENTER);
    settingPane.setTop(title);

    //create mode selection section
    Text modeText = new Text("Choose your mode");
    modeText.getStyleClass().add("heading");
    var settingBackgroundLight = new Button("Light");
    var settingBackgroundDark = new Button("Dark");
    settingBackgroundLight.getStyleClass().add("menuItem");
    settingBackgroundDark.getStyleClass().add("menuItem");
    HBox modeSelectionBox = new HBox(20, settingBackgroundLight, settingBackgroundDark);
    modeSelectionBox.setAlignment(Pos.CENTER);
    VBox modeSelectionSection = new VBox(10, modeText, modeSelectionBox);
    modeSelectionSection.setAlignment(Pos.CENTER);
    settingPane.setLeft(modeSelectionSection);

    //create volume control section
    Text backgroundVolumeText = new Text("Set the volume of background music");
    backgroundVolumeText.getStyleClass().add("heading");
    Slider backgroundVolumeSlider = new Slider(0.0, 1.0, 0.5);
    backgroundVolumeSlider.setPrefWidth(200);
    backgroundVolumeSlider.getStyleClass().add("slider");

// Add a listener to the volume slider's value property
    backgroundVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) {
        // Update the volume of the background player
        Multimedia.setBackgroundVolume(newVal.doubleValue());
        backgroundPlayer.setVolume(newVal.doubleValue());
      }
    });

    //now set the audio player
    Text audioVolumeText = new Text("Set the volume of audio music");
    audioVolumeText.getStyleClass().add("heading");
    Slider audioVolumeSlider = new Slider(0.0, 1.0, 0.5);
    audioVolumeSlider.setPrefWidth(200);
    audioVolumeSlider.getStyleClass().add("slider");

    audioVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newVal) {
        Multimedia.setAudioVolume(newVal.doubleValue());
      }
    });

    // Add the volume text and slider to your scene

    VBox volumeControlSection = new VBox(10, backgroundVolumeText, backgroundVolumeSlider, audioVolumeText, audioVolumeSlider);
    volumeControlSection.setAlignment(Pos.CENTER);
    settingPane.setRight(volumeControlSection);

    //add event handler to change background color
    settingBackgroundLight.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        challengeScene.changePane.getStyleClass().setAll("challenge-background-light");
        menuScene.changePane.getStyleClass().setAll("menu-background-light");
        lobbyScene.changePane.getStyleClass().setAll("lobby-background-light");

      }
    });
    settingBackgroundDark.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        challengeScene.changePane.getStyleClass().setAll("challenge-background");
        menuScene.changePane.getStyleClass().setAll("menu-background");
        lobbyScene.changePane.getStyleClass().setAll("lobby-background");
      }
    });
  }
}
