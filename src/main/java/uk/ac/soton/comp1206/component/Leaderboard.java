package uk.ac.soton.comp1206.component;

import javafx.scene.Node;
import javafx.util.Pair;


/**
 * used to create cross on the eliminated player
 */
public class Leaderboard extends ScoresList {

  /**
   * use to add a cross to the eliminated players
   *
   * @param cross the name of eliminated players
   */
  public void cross(String cross) {

    //add the cross to the list which could use to check if we have this gamer,
    //if we have,we set the name a cross
    multiplayer.add(cross);
    for (Pair<String, Integer> pair : local) {
      //cross is a gamePlayer's name
      if (pair.getKey().equals(cross)) {
        //which means we have that player in our list
        Node childNode = this.getChildren().get(local.indexOf(pair));
        childNode.getStyleClass().add("cross");
      }
    }
  }
}
