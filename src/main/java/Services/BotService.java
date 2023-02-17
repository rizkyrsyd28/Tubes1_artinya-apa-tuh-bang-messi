package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private int teleHeading;

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
        System.out.println("size : " + bot.getSize());
        System.out.println("current tick : "+gameState.world.getCurrentTick());;
        System.out.println("action: "+playerAction.action);
        System.out.println("radius: "+gameState.getWorld().getRadius());
        System.out.println("torpedo count: "+bot.getTorpedoCount());
        System.out.println("tele count: "+bot.getTeleCount());
        System.out.println("shield count: "+bot.getShieldCount());
        int cntutama=0;
        var playersortdistance=gameState.getPlayerGameObjects()
        .stream().filter(item->item.getId()!=bot.getId())
        .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
        
        System.out.println("-------------------------");
        
        if(bot.getSize()<15 && cntutama==0){
            cntutama++;
            playerAction=greedByFood(playerAction);
        }
        else if(bot.getSize()<25 && cntutama==0){
            System.err.println("2");
            if(inWorld()){
                cntutama++;
                var torpedoList=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=60)
                .collect(Collectors.toList());
                var gasList=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
                var teleList  = gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
                var playerListBySize = gameState.getPlayerGameObjects().stream()
                .filter(item->item.getSize()>bot.getSize())
                .sorted(Comparator.comparing(item->item.getSize()))
                .collect(Collectors.toList());

                int differenceHeading;
                int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*60;
                int lengthPlayer = 0;
                if(!playerListBySize.isEmpty()){
                    lengthPlayer = playerListBySize.size();
                }
                int cntres=0;
                if(jumpTeleport()){
                    playerAction.action=PlayerActions.TELEPORT;
                }
                else if (!teleList.isEmpty() && !playerListBySize.isEmpty() && getDistanceBetween(bot,
                        playerListBySize.get(lengthPlayer - 1)) <= playerListBySize.get(lengthPlayer - 1).getSize()) {
                    differenceHeading = teleList.get(0).getCurrentHeading()
                            - getHeadingBetweenObject(teleList.get(0), bot);
                    if (differenceHeading > 0) {
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = (teleList.get(0).getCurrentHeading() + 270) % 360;
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = (teleList.get(0).getCurrentHeading() + 90) % 360;
                    }
                } else if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
                    differenceHeading=torpedoList.get(0).getCurrentHeading()-getHeadingBetweenObject(torpedoList.get(0),bot);
                    if(Math.tan(Math.toRadians(Math.abs(differenceHeading))) <= Math
                    .abs(bot.getSize() / getDistanceBetween(bot, torpedoList.get(0)))
                    && getDistanceBetween(bot, torpedoList.get(0)) <= 180 && bot.getShieldCount() > 0
                    && bot.getSize() > 50){
                        playerAction.action=PlayerActions.ACTIVATESHIELD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else if(Math.tan(Math.toRadians(Math.abs(differenceHeading))) <= Math
                    .abs(bot.getSize() / getDistanceBetween(bot, torpedoList.get(0)))
                    && getDistanceBetween(bot, torpedoList.get(0)) <= 180 && bot.getTorpedoCount() > 0
                    && bot.getSize() > 40){
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(torpedoList.get(0));
                        cntres++;
                    }
                    else if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+90)%360;
                        cntres++;
                    }
                    if(cntres==1){
                    }
                    
                }
                else if(!gasList.isEmpty() && getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<10){
                    differenceHeading=bot.getCurrentHeading()-getHeadingBetween( gasList.get(0));
                    if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()+90-Math.abs(differenceHeading)+360)%360;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()-90+Math.abs(differenceHeading)+360)%360;
                    }
                    cntres++;
                    
                    
                    
                }
               else{
                    playerAction=greedByFood(playerAction);

                }

            } 
            else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingToMid();
            }
            
        }
        else{
            var torpedoList=gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                    Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=60)
                    .collect(Collectors.toList());
            var teleList  = gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                    .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var playerListBySize = gameState.getPlayerGameObjects().stream()
                    .filter(item->item.getSize()>bot.getSize())
                    .sorted(Comparator.comparing(item->item.getSize()))
                    .collect(Collectors.toList());
    
            var gasList=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD && 
                Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=45)
                .collect(Collectors.toList());
            int differenceHeading;
            int lengthPlayer = 0;
            if(inWorld()){
                if(!playerListBySize.isEmpty()){
                    lengthPlayer = playerListBySize.size();
                }
                int cntres=0;
                int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*30;
                
                if(jumpTeleport()){
                    playerAction.action=PlayerActions.TELEPORT;
                    // playerAction.heading=getHeadingToMid();
                    // cntres++;
                }
                else if (!teleList.isEmpty() && !playerListBySize.isEmpty() && getDistanceBetween(bot,
                        playerListBySize.get(lengthPlayer - 1)) <= playerListBySize.get(lengthPlayer - 1).getSize()) {
                    differenceHeading = teleList.get(0).getCurrentHeading()
                            - getHeadingBetweenObject(teleList.get(0), bot);
                    if (differenceHeading > 0) {
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = (teleList.get(0).getCurrentHeading() + 270) % 360;
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = (teleList.get(0).getCurrentHeading() + 90) % 360;
                    }
                } else if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
                    differenceHeading=torpedoList.get(0).getCurrentHeading()-getHeadingBetweenObject(torpedoList.get(0),bot);
                    if(Math.tan(Math.toRadians(Math.abs(differenceHeading))) <= Math
                    .abs(bot.getSize() / getDistanceBetween(bot, torpedoList.get(0)))
                    && getDistanceBetween(bot, torpedoList.get(0)) <= 180 && bot.getShieldCount() > 0
                    && bot.getSize() > 50){
                        playerAction.action=PlayerActions.ACTIVATESHIELD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else if(Math.tan(Math.toRadians(Math.abs(differenceHeading))) <= Math
                    .abs(bot.getSize() / getDistanceBetween(bot, torpedoList.get(0)))
                    && getDistanceBetween(bot, torpedoList.get(0)) <= 180 && bot.getTorpedoCount() > 0
                    && bot.getSize() > 40){
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(torpedoList.get(0));
                        cntres++;
                    }
                    else if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+90)%360;
                        cntres++;
                    }
                    if(cntres==1){
                    }
                    
                }
                else if(!gasList.isEmpty() && getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<15){
                    differenceHeading=bot.getCurrentHeading()-getHeadingBetween( gasList.get(0));
                    if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()+90-Math.abs(differenceHeading)+360)%360;
                    }
                    else{
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(bot.getCurrentHeading()-90+Math.abs(differenceHeading)+360)%360;
                    }
                    cntres++;
                    
                }
                else if (bot.getSize() > 35 && !gameState.getGameObjects().isEmpty() && cntres==0) {
                    playerAction = greedByOffense(playerAction);
                } 
                else {
                    playerAction = greedByFood(playerAction);
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
            if(Math.sqrt(bot.getPosition().x*bot.getPosition().x+bot.getPosition().y*bot.getPosition().y+5*bot.getSize()*bot.getSize())+50<gameState.getWorld().getRadius()){
                return true;
            } else {
                return false;
            }
        } else {
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
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
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

    private int getHeadingBetweenObject(GameObject object2, GameObject object1) {
        var direction = toDegrees(Math.atan2(object1.getPosition().y - object2.getPosition().y,
                object1.getPosition().x - object2.getPosition().x));
        return (direction + 360) % 360;
    }

    private int getHeadingToMid() {
        int direction;
        if (gameState.getWorld().getCenterPoint() != null) {
            direction = toDegrees(Math.atan2(gameState.world.getCenterPoint().y - bot.getPosition().y,
                    gameState.world.getCenterPoint().x - bot.getPosition().x));

        } else {
            direction = toDegrees(Math.atan2(0 - bot.getPosition().y,
                    0 - bot.getPosition().x));

        }
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    private double getDistanceOutter(GameObject obj1, GameObject obj2) {
        return getDistanceBetween(obj1, obj2) - obj1.getSize() - obj2.getSize();
    }

    private List<GameObject> getPlayerInRadius(double rad) {
        var player = gameState.getPlayerGameObjects().stream()
                .filter(item -> getDistanceOutter(item, bot) < rad && item != bot)
                .sorted(Comparator.comparing(item -> getDistanceOutter(item, bot)))
                .collect(Collectors.toList());

        return player;
    }

    private int getHeadingBetweenTwo(GameObject obj1, GameObject obj2) {
        var direction = toDegrees(Math.atan2(obj2.getPosition().y - obj1.getPosition().y,
                obj2.getPosition().x - obj1.getPosition().x));
        return (direction + 360) % 360;
    }

    private boolean isSave(GameObject obj) {
        var objects = gameState.getGameObjects().stream()
                .filter(item -> getDistanceOutter(item, obj) < bot.getSize() + 15
                        && item.getGameObjectType() != ObjectTypes.FOOD
                        && item.getGameObjectType() != ObjectTypes.SUPERFOOD
                        && item.getGameObjectType() != ObjectTypes.WORMHOLE
                        && item.getGameObjectType() != ObjectTypes.TELEPORTER
                        && item.getGameObjectType() != ObjectTypes.SUPERNOVAPICKUP
                        && item.getGameObjectType() != ObjectTypes.SHIELD)
                .collect(Collectors.toList());

        boolean save = true;

        if (!objects.isEmpty()) {
            for (int o = 0; o < objects.size(); o++) {
                if (objects.get(o).getGameObjectType() != ObjectTypes.ASTEROID_FIELD
                        || objects.get(o).getGameObjectType() != ObjectTypes.GAS_CLOUD
                        || objects.get(o).getGameObjectType() != ObjectTypes.SUPERNOVABOMB
                        || objects.get(o).getGameObjectType() != ObjectTypes.TORPEDOSALVO) {
                    return false;
                } else if (objects.get(o).getGameObjectType() != ObjectTypes.PLAYER) {
                    if (objects.get(o).getSize() + 5 > bot.getSize()) {
                        return false;
                    } else {
                        save = save && true;
                    }
                }
            }
        }

        return save;

    }

    private boolean jumpTeleport(){
        if (!gameState.getGameObjects().isEmpty()){
            var teles=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.TELEPORTER && Math.abs(getHeadingBetweenObject(item,bot)-item.getCurrentHeading())>=140)
                .collect(Collectors.toList());
            var playerlist=teles;
            if(!teles.isEmpty()){
                playerlist=gameState.getPlayerGameObjects()
                        .stream()
                        .filter(item -> item != bot)
                        .sorted(Comparator.comparing(item -> getDistanceBetween(teles.get(0), item)))
                        .collect(Collectors.toList());
            }
            for(int i=0;i<playerlist.size();i++){
                if(getDistanceBetween(playerlist.get(i), teles.get(0))<50 && bot.getSize()*1.2>playerlist.get(i).getSize() ){
                    return true;
                }
            }
        }
        return false;
    }

    private PlayerAction greedByFood(PlayerAction playerAction){
        Comparator<GameObject> ukuran = Comparator.comparing(item -> getDistanceBetween(bot, item));
        var gaslist=gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                    .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
        Comparator<GameObject> acak = Comparator.comparing(item -> item.getPosition().y);
        
        Comparator<GameObject> full = ukuran.thenComparing(acak);
        var food=gameState.getGameObjects().stream()
                .filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType()==ObjectTypes.SUPERFOOD) 
                && Math.sqrt(item.getPosition().x*item.getPosition().x+item.getPosition().y*item.getPosition().y+5*item.getSize()*item.getSize())+50<gameState.getWorld().getRadius()
                && getDistanceBetween(item, gaslist.get(0))>50) 
                .sorted(full)
                .collect(Collectors.toList());
        var supernovalist=gameState.getGameObjects()
            .stream().collect(Collectors.toList());
        
        if(bot.getSupernovaAvailable()>0){
            if(getDistanceBetween(supernovalist.get(0), bot)<200){
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(supernovalist.get(0))%360;

            }
            else{
                if (food.size()>1){
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(food.get(0));
                }
            }
        }
        else if (food.size()>1){
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(food.get(0));

                if(getDistanceBetween(food.get(0), bot)-getDistanceBetween(food.get(1), bot)<15){
                    if(bot.getCurrentHeading()!=getHeadingBetween(food.get(0)) && bot.getCurrentHeading()!=getHeadingBetween(food.get(1))){
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = getHeadingBetween(food.get(0));
                    }
                    else{
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = bot.getCurrentHeading();
                    }
                }
                else{
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(food.get(0));
                }
        }
        else{
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getHeadingRandom()%360;
        }

        return playerAction;
    }
    private Integer getHeadingRandom(){
        return new Random().nextInt(360);
    }
    private PlayerAction greedByOffense(PlayerAction playerAction){
        
        
        if (bot.getSize() >= 25 && !gameState.getGameObjects().isEmpty()){
            var all = getPlayerInRadius(3000);
            var candidate = getPlayerInRadius(400);
            playerAction.heading = new Random().nextInt(8);
            int cand=0;
            var temptele=gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER &&
            getDistanceBetween(item,bot)<400)
                        .collect(Collectors.toList());
            if(bot.getTeleCount()>0 && bot.getSize()>30 && cand==0 && temptele.isEmpty()){
                if(temptele.isEmpty()){

                    if(all.get(0).getSize()+25<bot.getSize()){
                        cand++;
                        
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        playerAction.heading = getHeadingBetween(all.get(0));
                    }
                }
                
            }
            if(jumpTeleport()){
                cand++;
                playerAction.action = PlayerActions.TELEPORT;
            }
            if (bot.getTorpedoCount() > 0 && !candidate.isEmpty() && cand==0){
                playerAction.action = PlayerActions.FIRETORPEDOES;
                playerAction.heading = getHeadingBetween(candidate.get(0));
            }
            
            else if (bot.getTorpedoCount() == 0 && !candidate.isEmpty() && cand==0) {
                if (getDistanceOutter(candidate.get(0), bot) < 50 && candidate.get(0).getSize() + 13 < bot.getSize()) {
                    playerAction.action = PlayerActions.FORWARD;
                    int offset = new Random().nextInt(3);
                    playerAction.heading = getHeadingBetween(candidate.get(0)) + offset;
                }
                else{
                    playerAction=greedByFood(playerAction);
                }
            }
            else if (bot.getSupernovaAvailable() > 0 && !all.isEmpty() && cand==0){
                playerAction.action = PlayerActions.FIRESUPERNOVA;
                playerAction.heading = getHeadingBetween(all.get(0));
            }
            else if(cand==0) {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingToMid();
            }

        }

        return playerAction;
    }
}
