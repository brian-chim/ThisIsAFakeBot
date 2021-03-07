package com.thisisafakecom.thisisafakebot.commands.points;

import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.entities.Message;

public class PointsHandler {

  private static int genRate = 10;

  // each user will slowly gain points
  public static void autoGenPoints(Message message) {
    // rng from 0 to 100
    double random = Math.random() * 100;
    if (random <= genRate) {
      PointsDBHandler.addPoints(message.getAuthor(), 1);
    }
  }

  /*
   * // will have the bot say a msg and user does a cmd to claim pts public static
   * void pointsMessage(Message message) { }
   */

}
