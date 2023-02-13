package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // playerAction.action = PlayerActions.FORWARD;
        // playerAction.heading = new Random().nextInt(360);
        
        // if (!gameState.getGameObjects().isEmpty()) {
            //     var foodList = gameState.getGameObjects()
            //             .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
            //             .sorted(Comparator
            //                     .comparing(item -> getDistanceBetween(bot, item)))
            //             .collect(Collectors.toList());
            
            //     playerAction.heading = getHeadingBetween(foodList.get(0));
            
            //     var x = getPlayerInRadius(10);
            // }
            
        this.playerAction = greedByFood(playerAction);
        this.playerAction = greedByOffense(playerAction);
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }
    
    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    private double getDistanceOutter(GameObject obj1, GameObject obj2){
        return getDistanceBetween(obj1, obj2) - obj1.getSize() - obj2.getSize();
    }

    private List<GameObject> getPlayerInRadius(double rad){
        var player = gameState.getPlayerGameObjects().
                    stream().filter(item->getDistanceOutter(item, bot) <= rad).
                    filter(item->getDistanceOutter(item, bot) > 0).collect(Collectors.toList());

        System.out.println(player);
        return player;
    }

    private PlayerAction greedByFood(PlayerAction playerAction){
        if (bot.getSize() < 1000 && !gameState.getGameObjects().isEmpty()){
            playerAction.action = PlayerActions.FORWARD;
            
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                    .comparing(item -> getDistanceOutter(bot, item)))
                    .collect(Collectors.toList());
            
            playerAction.heading = getHeadingBetween(foodList.get(0));
        }
        return playerAction;
    }

    // private boolean isObstacleBetween(GameObject object){
    //     int opX = object.getPosition().x;
    //     int opY = object.getPosition().y;

    //     var obs = gameState.getGameObjects().
    //         stream().filter(item->item.getPosition().x < opX).
    //         filter(item->item.getPosition().x <).collect(Collectors.toList());

    //     if (obs.isEmpty()){
    //         return false;
    //     }
    //     else {
    //         return true;
    //     }
    // }



    private PlayerAction greedByOffense(PlayerAction playerAction){
        if (bot.getSize() > 50 && !gameState.getGameObjects().isEmpty()){
            var candidate = getPlayerInRadius(200);
            // BASIC ATTACK
            if (!candidate.isEmpty() && bot.getSupernovaAvailable() == 0){

                if (bot.getSize() > candidate.get(0).getSize() + 10) {
                    playerAction.heading = getHeadingBetween(candidate.get(0));
                    playerAction.action = PlayerActions.FORWARD;
                }

                else if (bot.getTorpedoCount() > 0){
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    GameObject target = candidate.get(0);
                
                    if (target.getCurrentHeading() > 0 && target.getCurrentHeading() < 180){
                        playerAction.heading = getHeadingBetween(target) + 10;
                    }  
                    else {
                        playerAction.heading = getHeadingBetween(target) - 10;
                    }
                }
            }
            // SUTACK SUPER ATTACK

        }

        return playerAction;
    }
}


