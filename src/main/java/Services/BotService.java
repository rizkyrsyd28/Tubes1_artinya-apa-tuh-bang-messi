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
        System.out.println("SIZE : " + bot.getSize());
        System.out.print("ACTION : " );
        
        // if ()

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
                    stream().filter(item->getDistanceOutter(item, bot) < rad && item != bot)
                    .sorted(Comparator.comparing(item-> getDistanceOutter(item, bot)))
                    .collect(Collectors.toList());
                    
                    // System.out.println(player);
                    return player;
    }

    private int getHeadingBetweenTwo(GameObject obj1, GameObject obj2) {
        var direction = toDegrees(Math.atan2(obj2.getPosition().y - obj1.getPosition().y,
        obj2.getPosition().x - obj1.getPosition().x));
        return (direction + 360) % 360;
    }
    
    private boolean isSave(GameObject obj){
        var objects = gameState.getGameObjects().stream()
        .filter(item->getDistanceOutter(item, obj) < bot.getSize() + 15 
        && item.getGameObjectType() != ObjectTypes.FOOD && item.getGameObjectType() != ObjectTypes.SUPERFOOD
        && item.getGameObjectType() != ObjectTypes.WORMHOLE && item.getGameObjectType() != ObjectTypes.TELEPORTER
        && item.getGameObjectType() != ObjectTypes.SUPERNOVAPICKUP && item.getGameObjectType() != ObjectTypes.SHIELD)
        .collect(Collectors.toList());

        boolean save = true;

        if (!objects.isEmpty()){
            for (int o = 0; o < objects.size(); o++){
                if (objects.get(o).getGameObjectType() != ObjectTypes.ASTEROID_FIELD 
                || objects.get(o).getGameObjectType() != ObjectTypes.GAS_CLOUD
                || objects.get(o).getGameObjectType() != ObjectTypes.SUPERNOVABOMB
                || objects.get(o).getGameObjectType() != ObjectTypes.TORPEDOSALVO){
                    return false;
                }
                else if (objects.get(o).getGameObjectType() != ObjectTypes.PLAYER){
                    if (objects.get(o).getSize() + 5 > bot.getSize()){
                        return false;
                    }
                    else {
                        save = save && true;
                    }
                }
            }
        }

        return save;

    }

    // private boolean jumpTeleport(){
    //     if (!gameState.getGameObjects().isEmpty()){
    //         var tele = gameState.getGameObjects()
    //                 .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
    //                 .sorted(Comparator
    //                 .comparing(item -> getHeadingBetweenTwo(item, bot)))
    //                 .collect(Collectors.toList());
    //         for (int i)
    //     }
    //     return false;
    // }

    private PlayerAction greedByFood(PlayerAction playerAction){
        if (bot.getSize() < 30 && !gameState.getGameObjects().isEmpty()){
            playerAction.action = PlayerActions.FORWARD;
            
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                    .comparing(item -> getDistanceOutter(bot, item)))
                    .collect(Collectors.toList());
            
            playerAction.heading = getHeadingBetween(foodList.get(0));
            System.out.println("EAT");
        }
        // else {
        //     System.out.println("NO EAT");
        // }
    
        return playerAction;
    }

    private PlayerAction greedByOffense(PlayerAction playerAction){
        
        
        if (bot.getSize() >= 30 && !gameState.getGameObjects().isEmpty()){

            var all = getPlayerInRadius(3000);
            var candidate = getPlayerInRadius(700);
            playerAction.heading = new Random().nextInt(8);
            
            if (bot.getTorpedoCount() > 0 && !candidate.isEmpty()){
                playerAction.action = PlayerActions.FIRETORPEDOES;
                playerAction.heading = getHeadingBetween(candidate.get(0));
                System.out.println("FIRE");
            }
            else if (bot.getTorpedoCount() == 0 && bot.getTeleCount() > 0 && !all.isEmpty()){
                playerAction.action = PlayerActions.FIRETELEPORT;
                playerAction.heading += getHeadingBetween(all.get(0));
            }
            else if (bot.getTorpedoCount() == 0 && !candidate.isEmpty()){
                if (getDistanceOutter(candidate.get(0), bot) < 10 && candidate.get(0).getSize() + 13 < bot.getSize()){
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(candidate.get(0));
                    System.out.println("FORWARD");
                }
            }
            else if (bot.getSupernovaAvailable() > 0 && !all.isEmpty()){
                playerAction.action = PlayerActions.FIRESUPERNOVA;
                playerAction.heading = getHeadingBetween(all.get(0));
                System.out.println("SUVERNOPA");
            }
            else {
                System.out.println("NO ATTACK");
            }


        }

        return playerAction;
    }
}


