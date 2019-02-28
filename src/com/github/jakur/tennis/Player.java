package com.github.jakur.tennis;

import java.util.*;

/**
 * Created by jakur on 4/10/2017.
 */
public class Player  {
    //Collection of players
    public static HashMap<Integer, Player> players = new HashMap<>(1000, (float) 0.75);
    public static HashMap<Integer, Player> discoveredPlayers = new HashMap<>();

    public HashSet<Match> recentMatches;

    public static void printPlayersByRating() {
        ArrayList<Player> sortedPlayers = new ArrayList<>(discoveredPlayers.values());
        sortedPlayers.sort(Comparator.comparing(p -> p.currentRating.rating));
        sortedPlayers.forEach(p -> {
            if(p.status != Status.MISSING) {
                System.out.println(p.getName() + ": " + p.currentRating);
            }
        });
    }

    public static void printHeadByRating(int numberToPrint) {
        ArrayList<Player> sortedPlayers = new ArrayList<>(discoveredPlayers.values());
        sortedPlayers.sort(Comparator.comparing(p -> p.currentRating.rating * -1));
        for(int i = 0; i < numberToPrint; i++) {
            Player p = sortedPlayers.get(i);
            if(p.status == Status.MISSING) {
                numberToPrint++;
                continue;
            }
            System.out.println(p.getName() + ": " + p.currentRating);
        }
        System.out.println("--------------");
    }

    public enum Status {
        ACTIVE, INACTIVE, UNDISCOVERED, MISSING
    }

    public Status status;
    public int inactiveCount = 0;

    //Player's data
    private final int id;
    private final String name;
    private String country;
    private final String dob;

    //Rating data
    private int matches;

    Glicko currentRating;
    Glicko nextRating;

    public Player(int id, String name, String country, String dob) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.dob = dob;
        this.status = Status.UNDISCOVERED;
        currentRating = new Glicko();
    }

    public Player(int id, String name) {
        this(id, name, "UNK", "UNK");
    }

    public void addMatch(Match m) {
        recentMatches.add(m);
        status = Status.ACTIVE;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getDob() {
        return dob;
    }

    public Glicko getCurrentRating() {
        return currentRating;
    }

    public void setNextRating(Glicko nextRating) {
        this.nextRating = nextRating;
    }

}

class Glicko {
    final double rating;
    final double deviation;
    final double volatility;

    @Override
    public String toString() {
        return rating + " " + deviation + " " + volatility;
    }

    Glicko() {
        this.rating = 1500;
        this.deviation = 200;
        this.volatility = 0.06;
    }

    Glicko(double rating, double deviation, double volatility) {
        this.rating = rating;
        this.deviation = deviation;
        this.volatility = volatility;
    }
}
