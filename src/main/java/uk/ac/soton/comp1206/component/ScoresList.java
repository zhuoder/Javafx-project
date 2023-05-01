package uk.ac.soton.comp1206.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * used to show all the gamers' score and online high score
 */
public class ScoresList extends VBox {

  /**
   * use a simple list to hold all the local scores
   */
  protected SimpleListProperty<Pair<String, Integer>> local = new SimpleListProperty<>();


  /**
   * use to hold all the players who need to cross
   */

  protected ArrayList<String> multiplayer = new ArrayList<>();

  /**
   * constructor of scoreList
   */
  public ScoresList() {


    //我们添加了一个listener在localScore上，那么每一次我们更新localScores时，就会调用reveal方法来展示一个动效分数
    local.addListener((observableValue, pairs, newVal) -> show(newVal));

    local.set(FXCollections.observableArrayList(new ArrayList<>()));
  }

  /**
   * when localScore is updated, we call this method, this class would have a object which need to added
   * to scoreScene,therefore, every time we update the data, we have to clear the previous data
   */
  public void show(ObservableList<Pair<String, Integer>> newVal) {
    getChildren().clear();
    showScore(newVal);
  }

  /**
   * used to assist show
   *
   * @param newVal the new score value
   */
  public void showScore(ObservableList<Pair<String, Integer>> newVal) {
    for (Pair<String, Integer> show : newVal) {
      //use this to represent the name and the score
      Text scoreText = new Text(show.getKey() + " : " + show.getValue());
      setStyle(show, scoreText);
      scoreText.setFill(Color.YELLOW);
      //add current data to the scene
      getChildren().add(scoreText);

      //this would have a new animation to show the scores
      Timeline timeline = new Timeline(
              new KeyFrame(Duration.ZERO, new KeyValue(scoreText.opacityProperty(), 0)),
              new KeyFrame(Duration.seconds(1), new KeyValue(scoreText.opacityProperty(), 1))
      );
      timeline.play();
    }
  }

  /**
   * set the style of scoreList
   * @param show the pair we need to show on the screen
   * @param scoreText the score text which contains information of player's score
   */
  public void setStyle(Pair<String, Integer> show, Text scoreText) {
    if (multiplayer.contains(show.getKey())) {
      scoreText.getStyleClass().add("cross");
    } else {
      scoreText.getStyleClass().add("scorelist");
    }
  }

  /**
   * return the listProperty of localScores
   *
   * @return localScores
   */
  public SimpleListProperty<Pair<String, Integer>> localProperty() {
    return local;
  }


}
