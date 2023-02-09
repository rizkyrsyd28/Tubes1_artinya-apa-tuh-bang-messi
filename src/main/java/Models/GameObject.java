package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer effects;
  public Integer torpedoCount;
  public Integer supernovaAvailable;
  public Integer teleCount;
  public Integer shieldCount;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position,
      ObjectTypes gameObjectType, Integer effects, Integer torpedoCount, Integer supernovaAvailable,
      Integer teleCount, Integer shieldCount) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effects = effects;
    this.torpedoCount = torpedoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleCount = teleCount;
    this.shieldCount = shieldCount;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }

  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }

  public Integer getCurrentHeading() {
    return currentHeading;
  }

  public void setCurrentHeading(Integer currentHeading) {
    this.currentHeading = currentHeading;
  }

  public Integer getEffects() {
    return effects;
  }

  public void setEffects(Integer effects) {
    this.effects = effects;
  }

  public Integer getTorpedoCount() {
    return torpedoCount;
  }

  public void setTorpedoCount(Integer torpedoCount) {
    this.torpedoCount = torpedoCount;
  }

  public Integer getSupernovaAvailable() {
    return supernovaAvailable;
  }

  public void setSupernovaAvailable(Integer supernovaAvailable) {
    this.supernovaAvailable = supernovaAvailable;
  }

  public Integer getTeleCount() {
    return teleCount;
  }

  public void setTeleCount(Integer teleCount) {
    this.teleCount = teleCount;
  }

  public Integer getShieldCount() {
    return shieldCount;
  }

  public void setShieldCount(Integer shieldCount) {
    this.shieldCount = shieldCount;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList) {
    Position position = new Position(stateList.get(4), stateList.get(5));
    if (stateList.get(3) == 1) {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position,
          ObjectTypes.valueOf(stateList.get(3)), stateList.get(6), stateList.get(7),
          stateList.get(8), stateList.get(9), stateList.get(10));
    } else {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position,
          ObjectTypes.valueOf(stateList.get(3)), 0, 0, 0, 0,
          0);
    }
  }

}