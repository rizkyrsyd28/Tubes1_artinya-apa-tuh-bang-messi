package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import javax.lang.model.type.NullType;

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
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("-------------------------");
        System.out.println("current tick : "+gameState.world.getCurrentTick());;
        System.out.println("size: "+bot.getSize());
        System.out.println("action: "+playerAction.action);
        System.out.println("heading: "+playerAction.heading);
        System.out.println("radius: "+gameState.getWorld().getRadius());
        System.out.println("posisi x: "+bot.getPosition().x);
        System.out.println("posisi y: "+bot.getPosition().y);
        System.out.println("-------------------------");
        
        if(bot.getSize()<25){
            if(inWorld()){
                playerAction=greedByFood(playerAction);
            }
            else{
                playerAction.action=PlayerActions.FORWARD;
                playerAction.heading=getHeadingToMid();
            }
        }
        else{
            var torpedoList=gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                    Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=45)
                    .collect(Collectors.toList());
            var gasList=gameState.getGameObjects().stream()
                    .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                    .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                    .collect(Collectors.toList());
            int differenceHeading;
            if(inWorld()){
                int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*60;

                if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
                    differenceHeading=torpedoList.get(0).getCurrentHeading()-getHeadingBetweenObject(torpedoList.get(0),bot);
                    if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getShieldCount()>0 && bot.getSize()>50){
                        playerAction.action=PlayerActions.ACTIVATESHIELD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                    }
                    else if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getTorpedoCount()>0 && bot.getSize()>40){
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(torpedoList.get(0));
                    }
                    else if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+90)%360;
                    }
                    
                }
                else if(getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<10){
                    differenceHeading=bot.getCurrentHeading()-getHeadingBetween( gasList.get(0));
                    if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()+100-Math.abs(differenceHeading)+360)%360;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()-100+Math.abs(differenceHeading)+360)%360;
                    }
                }
                
                else{
                    playerAction=greedByFood(playerAction);
                }
            }
            else{
                playerAction.action=PlayerActions.FORWARD;
                playerAction.heading=getHeadingToMid();
            }
            
        }
    }
    private boolean inWorld(){
        if(gameState.world.radius!=null){
            if(Math.sqrt(bot.getPosition().x*bot.getPosition().x+bot.getPosition().y*bot.getPosition().y+5*bot.getSize()*bot.getSize())+100<gameState.getWorld().getRadius()){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
        
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

    private int getHeadingBetweenObject(GameObject object2,GameObject object1) {
        var direction = toDegrees(Math.atan2(object1.getPosition().y - object2.getPosition().y,
                object1.getPosition().x - object2.getPosition().x));
        return (direction + 360) % 360;
    }
    private int getHeadingToMid(){
        int direction;
        if(gameState.getWorld().getCenterPoint()!=null){
            direction = toDegrees(Math.atan2(gameState.world.getCenterPoint().y- bot.getPosition().y,
                    gameState.world.getCenterPoint().x - bot.getPosition().x));

        }
        else{
            direction = toDegrees(Math.atan2(0- bot.getPosition().y,
                    0 - bot.getPosition().x));

        }
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
        var playerelse=gameState.getPlayerGameObjects().stream()
                    .filter(item->item.size!=bot.size).sorted(Comparator.comparing(item ->getDistanceBetween(item, bot)))
                    .collect(Collectors.toList());
        Comparator<GameObject> comparator = Comparator.comparing(item -> getDistanceBetween(bot, item));
        comparator=comparator.thenComparing(Comparator.comparing(item -> item.getPosition().x));
        // comparator=Comparator.thenComparing(Comparator.comparing(item -> item.getPosition().y));
        var food =gameState.getGameObjects()
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.FOOD) && 
            item.getPosition().x*item.getPosition().x+item.getPosition().y*item.getPosition().y+5*bot.getSize()*bot.getSize()<gameState.getWorld().getRadius()*gameState.getWorld().getRadius()
            )
            .sorted(comparator)
            .collect(Collectors.toList());
        if(!playerelse.isEmpty()){
            food =gameState.getGameObjects()
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.FOOD) && 
            item.getPosition().x*item.getPosition().x+item.getPosition().y*item.getPosition().y+5*bot.getSize()*bot.getSize()<gameState.getWorld().getRadius()*gameState.getWorld().getRadius()
            && getDistanceBetween(bot, playerelse.get(0))>50)
            .sorted(comparator)
            .collect(Collectors.toList());
        }
        else{
            playerAction.heading=getHeadingToMid();
        }
        if (bot.getSize() < 1000 && !gameState.getGameObjects().isEmpty()){
            playerAction.action = PlayerActions.FORWARD;
            
            playerAction.heading = getHeadingBetween(food.get(0));
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


