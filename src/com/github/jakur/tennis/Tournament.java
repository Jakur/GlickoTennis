package com.github.jakur.tennis;

import java.util.HashSet;

/**
 * Created by jakur on 4/10/2017.
 */
public class Tournament implements Comparable<Tournament> {

    public int dateInteger;
    public HashSet<Match> matches;

    public Tournament(int dateInteger) {
        this.dateInteger = dateInteger;
        matches = new HashSet<>();
    }

    @Override
    public int compareTo(Tournament o) {
        if(this.dateInteger < o.dateInteger) {
            return -1;
        }
        else if(this.dateInteger > o.dateInteger) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
