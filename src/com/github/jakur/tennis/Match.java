package com.github.jakur.tennis;

/**
 * Created by jakur on 4/10/2017.
 */
public class Match {

    public final Player winner;
    public final Player loser;

    public Match(Player winner, Player loser) {
        this.winner = winner;
        this.loser = loser;
    }

    @Override
    public String toString() {
        return "Winner: " + winner.getName() + " Loser: " + loser.getName();
    }
}
