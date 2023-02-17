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
        System.out.println("SIZE : " + bot.getSize());
        System.out.println("ACTION : " );
        System.out.println("current tick : "+gameState.world.getCurrentTick());;
        System.out.println("size: "+bot.getSize());
        System.out.println("action: "+playerAction.action);
        System.out.println("heading: "+playerAction.heading);
        System.out.println("radius: "+gameState.getWorld().getRadius());
        System.out.println("posisi x: "+bot.getPosition().x);
        System.out.println("posisi y: "+bot.getPosition().y);
        System.out.println("torpedo count: "+bot.getTorpedoCount());
        System.out.println("tele count: "+bot.getTeleCount());
        System.out.println("shield count: "+bot.getShieldCount());
        System.out.println("supernova: "+bot.getSupernovaAvailable());
        System.out.println("-------------------------");
        int cntutama=0;
        if(gameState.world.currentTick!=null){
            var playersortdistance=gameState.getPlayerGameObjects()
                .stream().filter(item->item.getId()!=bot.getId())
                .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            var playersortsize=gameState.getPlayerGameObjects()
                .stream().filter(item->item.getId()!=bot.getId())
                .sorted(Comparator.comparing(item->item.getSize()))
                .collect(Collectors.toList());
            var biggestplayer=bot;
            if(!playersortsize.isEmpty()){
                biggestplayer=playersortsize.get(0);
                for(int i=0;i<playersortsize.size();i++){
                    if(playersortsize.get(i).getSize()>bot.getSize()){
                        biggestplayer=playersortsize.get(i);
                    }
                }

            }
            var torpedoList=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=45)
                .collect(Collectors.toList());
            var gasList=gameState.getGameObjects().stream()
            .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
            .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
            .collect(Collectors.toList());
            int differenceHeading;
            int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*60;
            if(gameState.world.currentTick>900){
                cntutama++;
                System.out.println("hardcore");
                if(bot.getSize()>40){
                    if(!playersortdistance.isEmpty()){
                        if(1.5*playersortdistance.get(0).getSize()<bot.getSize() && getDistanceBetween(bot, playersortdistance.get(0))<70){
                            playerAction.action=PlayerActions.FORWARD;
                            playerAction.heading=getHeadingBetween(playersortdistance.get(0));
                        }
                        else{
                            playerAction.action=PlayerActions.FIRETORPEDOES;
                            playerAction.heading=getHeadingBetween(biggestplayer);
                        }
                    }
                    
                    else{
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(biggestplayer);
                    }
                }
                else{
                    if(biggestplayer.getSize()>80 && bot.getSize()>20){
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(biggestplayer);
                    }
                    
                    else if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
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
                    if(!gasList.isEmpty()){
                        if(getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<10){
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
                        
                    }
                    else{
                        playerAction=greedByFood(playerAction);
                    }
                }
            }
        }
        if(bot.getSize()<15 && cntutama==0){
            System.out.println("1");
            cntutama++;
            playerAction=greedByFood(playerAction);
        }
        else if(bot.getSize()<25 && cntutama==0){
            System.err.println("2");
            if(inWorld()){
                System.out.println("didalam dunia");
                cntutama++;
                var torpedoList=gameState.getGameObjects().stream()
                .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=45)
                .collect(Collectors.toList());
                var gasList=gameState.getGameObjects().stream()
                .sorted(Comparator.comparing(item->getDistanceBetween(bot, item)))
                .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                .collect(Collectors.toList());
                int differenceHeading;
                int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*60;
                int cntres=0;
                if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
                    differenceHeading=torpedoList.get(0).getCurrentHeading()-getHeadingBetweenObject(torpedoList.get(0),bot);
                    if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getShieldCount()>0 && bot.getSize()>50){
                        playerAction.action=PlayerActions.ACTIVATESHIELD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getTorpedoCount()>0 && bot.getSize()>40){
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
                        System.out.println("menghindari torpedo1");
                    }
                    
                }
                if(!gasList.isEmpty() && cntres==0){
                    if(getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<10){
                        System.out.println("menghindari gas1");
                        differenceHeading=bot.getCurrentHeading()-getHeadingBetween( gasList.get(0));
                        if(differenceHeading>0){
                            playerAction.action=PlayerActions.FORWARD;
                            playerAction.heading=(bot.getCurrentHeading()+100-Math.abs(differenceHeading)+360)%360;
                        }
                        else{
                            playerAction.action=PlayerActions.FORWARD;
                            playerAction.heading=(bot.getCurrentHeading()-100+Math.abs(differenceHeading)+360)%360;
                        }
                        cntres++;
                    }
                    
                }

                if(cntres==0){
                    playerAction=greedByFood(playerAction);

                }

            } else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingToMid();
            }
            else{
                cntutama++;
                System.out.println("menuju mid1");
                playerAction.action=PlayerActions.FORWARD;
                playerAction.heading=getHeadingToMid();
            }
        }
        else if(cntutama==0){
            System.out.println("3");
            var torpedoList=gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.TORPEDOSALVO && 
                    Math.abs(item.getCurrentHeading()-getHeadingBetweenObject(item,bot))<=45)
                    .collect(Collectors.toList());
            var playerListBySize = gameState.getPlayerGameObjects().stream()
                    .filter(item -> item.getSize() > bot.getSize())
                    .sorted(Comparator.comparing(item -> item.getSize()))
                    .collect(Collectors.toList());

            int differenceHeading;
            if(inWorld()){
                int cntres=0;
                int dangerFromEpidermis=((int) bot.getSize()/bot.getSpeed())*30;
                if(jumpTeleport()){
                    playerAction.action=PlayerActions.TELEPORT;
                    playerAction.heading=getHeadingToMid();
                    cntres++;
                }
                if(!torpedoList.isEmpty() && getDistanceBetween(bot, torpedoList.get(0))<1.5*dangerFromEpidermis){
                    differenceHeading=torpedoList.get(0).getCurrentHeading()-getHeadingBetweenObject(torpedoList.get(0),bot);
                    if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getShieldCount()>0 && bot.getSize()>50){
                        playerAction.action=PlayerActions.ACTIVATESHIELD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    else if(Math.abs(differenceHeading)<=5 && getDistanceBetween(bot, torpedoList.get(0))<dangerFromEpidermis && bot.getTorpedoCount()>0 && bot.getSize()>40){
                        playerAction.action=PlayerActions.FIRETORPEDOES;
                        playerAction.heading=getHeadingBetween(torpedoList.get(0));
                        cntres++;
                    }
                    else if(differenceHeading>0){
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=(torpedoList.get(0).getCurrentHeading()+270)%360;
                        cntres++;
                    }
                    if(cntres==1){
                        System.out.println("menghindari torpedo2");
                    }
                    // else{
                    //     playerAction.action=PlayerActions.FORWARD;
                    //     playerAction.heading=(torpedoList.get(0).getCurrentHeading()+90)%360;
                    // }
                    
                }
                if(!gasList.isEmpty() && cntres==0){
                    if(getDistanceBetween(bot,gasList.get(0))-(bot.getSize()+gasList.get(0).getSize())<15){
                        System.out.println("menghindari gas2");
                        differenceHeading=bot.getCurrentHeading()-getHeadingBetween( gasList.get(0));
                        if(differenceHeading>0){
                            playerAction.action=PlayerActions.FORWARD;
                            playerAction.heading=(bot.getCurrentHeading()+100-Math.abs(differenceHeading)+360)%360;
                        }
                        else{
                            playerAction.action=PlayerActions.FORWARD;
                            playerAction.heading=(bot.getCurrentHeading()-100+Math.abs(differenceHeading)+360)%360;
                        }
                        cntres++;
                    }
                    // else{
                    //     playerAction.action=PlayerActions.FORWARD;
                    //     playerAction.heading=new Random().nextInt(360);
                    // }
                }
                if (bot.getSize() > 25 && !gameState.getGameObjects().isEmpty() && cntres==0) {
                    playerAction = greedByOffense(playerAction);
                } else {
                    playerAction = greedByFood(playerAction);
                }
                else if(cntres==0){
                    playerAction=greedByFood(playerAction);
                }
            }
            else{
                System.out.println("menuju mid2");
                playerAction.action=PlayerActions.FORWARD;
                playerAction.heading=getHeadingToMid();
            }

        }
        System.out.println("akhirrrrr");
        System.out.println("akhirrrrr");
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

        // System.out.println(player);
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
            var tele = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && item.getCurrentHeading() == this.teleHeading)
                    .sorted(Comparator
                    .comparing(item -> getHeadingBetweenTwo(item, bot)))
                    .collect(Collectors.toList());
            if(tele.isEmpty()){
                return false;
            }
            var playerlist=gameState.getPlayerGameObjects()
                    .stream()
                    .sorted(Comparator.comparing(item -> getDistanceBetween(tele.get(0), item)))
                    .collect(Collectors.toList());
            for(int i=0;i<playerlist.size();i++){
                System.out.println("jarak player "+getDistanceBetween(playerlist.get(i), bot));
            }
            for(int i=0;i<playerlist.size();i++){
                if(getDistanceBetween(playerlist.get(i), bot)<50 && playerlist.get(i).size*1.2<bot.size){
                    return true;
                }
            }
        }
        return false;
    }

    private PlayerAction greedByFood(PlayerAction playerAction){
        var playerelse=gameState.getPlayerGameObjects().stream()
                    .filter(item->item.size!=bot.size).sorted(Comparator.comparing(item ->getDistanceBetween(item, bot)))
                    .collect(Collectors.toList());
        // Comparator<GameObject> comparator = Comparator.comparing(item -> getDistanceBetween(bot, item));
        // comparator=comparator.thenComparing(Comparator.comparing(item -> item.getPosition().x));
        //first name comparator
        Comparator<GameObject> ukuran = Comparator.comparing(item -> getDistanceBetween(bot, item));
        var gaslist=gameState.getGameObjects().stream()
                    .filter(item->item.getGameObjectType()==ObjectTypes.GAS_CLOUD)
                    .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
        //last name comparator
        // Comparator<GameObject> acak = Comparator.comparing(item -> getHeadingBetween(item));
        
        //Compare by first name and then last name (multiple fields)
        // Comparator<GameObject> full = ukuran.thenComparing(acak);
        var food=gaslist;
        //Using Comparator - pseudo code
        // list.stream().sorted( comparator ).collect();
        // comparator=comparator.thenComparing(Comparator.comparing(item -> getHeadingBetween(item)));
        // comparator=Comparator.thenComparing(Comparator.comparing(item -> item.getPosition().y));
        if(gaslist.size()>0){
            food =gameState.getGameObjects()
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.FOOD) && 
            Math.sqrt(item.getPosition().x*item.getPosition().x+item.getPosition().y*item.getPosition().y)+150<gameState.getWorld().getRadius() &&
            getDistanceBetween(item, gaslist.get(0))>30
            )
            .sorted(ukuran)
            .collect(Collectors.toList());
        }
        else{
            food =gameState.getGameObjects()
            .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.FOOD) && 
            Math.sqrt(item.getPosition().x*item.getPosition().x+item.getPosition().y*item.getPosition().y)+150<gameState.getWorld().getRadius())
            .sorted(ukuran)
            .collect(Collectors.toList());
        }
        
        if(food.size()>1){
             System.out.println("jarak food "+getDistanceBetween(food.get(0), bot));
             System.out.println("jarak food "+getDistanceBetween(food.get(1), bot));

             System.out.println("heading food 1"+getHeadingBetween(food.get(0))%360);
             System.out.println("heading food 2"+getHeadingBetween(food.get(1))%360);
        }
        var supernovalist=gameState.getGameObjects()
            .stream().collect(Collectors.toList());
        
        if(bot.getSupernovaAvailable()>0){
            if(getDistanceBetween(supernovalist.get(0), bot)<200){
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(supernovalist.get(0))%360;

            }
            else{
                if (!food.isEmpty()){
                    if(food.size()>1){
                        if(getDistanceBetween(food.get(0), bot)-getDistanceBetween(food.get(1), bot)<20){
                            
                            playerAction.action=PlayerActions.FORWARD;
                            System.out.println("samaaaaaaa");
                            System.out.println("samaaaaaaa");
                            playerAction.heading = (getHeadingBetween(food.get(2)))%360;
                        }
                        else{
                            System.out.println("id 1"+food.get(0).getId());
                            System.out.println("id 2"+food.get(1).getId());
                            System.out.println("bolak balik");
                            playerAction.action = PlayerActions.FORWARD;
                            playerAction.heading = (getHeadingBetween(food.get(0)))%360;
                        }
                    }
                }
            }
        }
        else if (!food.isEmpty()){
            if(food.size()>1){
                if(getDistanceBetween(food.get(0), bot)-getDistanceBetween(food.get(1), bot)<20 &&food.size()>=2){
                    playerAction.action=PlayerActions.FORWARD;
                    System.out.println("samaaaaaaa");
                    System.out.println("samaaaaaaa");
                    playerAction.heading = (getHeadingBetween(food.get(2)))%360;
                }
                else{
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(food.get(0))%360;
                }
            }
        }
        else{
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getHeadingToMid()%360;
        }
        // else {
        // System.out.println("NO EAT");
        // }

        return playerAction;
    }

    private PlayerAction greedByOffense(PlayerAction playerAction){
        
        
        if (bot.getSize() >= 25 && !gameState.getGameObjects().isEmpty()){
            System.out.println("nembak1");
            var all = getPlayerInRadius(3000);
            var candidate = getPlayerInRadius(400);
            playerAction.heading = new Random().nextInt(8);
            int cand=0;
            if(candidate.isEmpty()){
                for(int i=0;i<candidate.size();i++){
                    if(getDistanceBetween(candidate.get(i), bot)<100){
                        System.out.println("makan");
                        playerAction.action=PlayerActions.FORWARD;
                        playerAction.heading=getHeadingBetween(candidate.get(i));
                        cand++;
                        break;
                    }
                }

            }
            if(bot.getTeleCount()>0 && bot.getSize()>40){
                var tele = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && item.getCurrentHeading() == this.teleHeading)
                .sorted(Comparator
                .comparing(item -> getHeadingBetweenTwo(item, bot)))
                .collect(Collectors.toList());                
                if(tele.isEmpty() && bot.getSize()>1.5*all.get(0).getSize()){
                    cand++;
                    playerAction.action = PlayerActions.FIRETELEPORT;
                    playerAction.heading = getHeadingBetween(all.get(0));
                    System.out.println("Fire teleport");
                }
                
            }
            if (jumpTeleport() && cand==0){
                System.out.println("tele");
                playerAction.action = PlayerActions.TELEPORT;
            }
            else if (bot.getTorpedoCount() > 0 && !candidate.isEmpty()){
                playerAction.action = PlayerActions.FIRETORPEDOES;
                playerAction.heading = getHeadingBetween(candidate.get(0));
                System.out.println("FIRE");
            }
            
            // else if (bot.getSize() > 40 && all.get(0).getSize() < bot.getSize() && bot.getTorpedoCount() == 0 && bot.getTeleCount() > 0 && !all.isEmpty()){
            //     playerAction.action = PlayerActions.FIRETELEPORT;
            //     playerAction.heading = getHeadingBetween(all.get(0));
            //     System.out.println("Fire teleport");
            // }
            else if (bot.getTorpedoCount() == 0 && !candidate.isEmpty()) {
                if (getDistanceOutter(candidate.get(0), bot) < 10 && candidate.get(0).getSize() + 13 < bot.getSize()) {
                    playerAction.action = PlayerActions.FORWARD;
                    int offset = new Random().nextInt(3);
                    playerAction.heading = getHeadingBetween(candidate.get(0)) + offset;
                    System.out.println("FORWARD");
                }
                else{
                    playerAction=greedByFood(playerAction);
                    System.out.println("aneh");
                }
            }
            else if (bot.getSupernovaAvailable() > 0 && !all.isEmpty()){
                playerAction.action = PlayerActions.FIRESUPERNOVA;
                playerAction.heading = getHeadingBetween(all.get(0));
                System.out.println("SUVERNOPA");
            }
            else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingToMid();
                System.out.println("NO ATTACK");
            }

        }

        return playerAction;
    }
}
