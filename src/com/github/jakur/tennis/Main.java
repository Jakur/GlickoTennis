package com.github.jakur.tennis;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.SortedSet;

/**
 * Created by jakur on 4/10/2017.
 */
public class Main {

    public static int year;

    public static void main(String[] args) {
        //Clearing the output file
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("glicko.csv"), "utf-8"));
            writer.write("");
            System.out.println("Old output file cleared!");
        } catch(Exception e) {
            System.out.println("No old output file detected!");
        }
        int endYear = 2016;
        if (args.length >= 1) {
            try {
                endYear = Integer.parseInt(args[0]);
                System.out.println("End year set to " + endYear);
            } catch(Exception e) {
                System.out.println("Invalid argument: cannot parse as year! Using default year 2016.");
            }
        }
        else {
            System.out.println("No end year argument given: using default year 2016");
        }
	    Database.readPlayers();
	    computeThrough(endYear);
    }

    public static void computeThrough(int endYear) {


        for(int year = 1968; year <= endYear; year++) {
            Main.year = year;
            String fileString = "matches/atp_matches_" + year + ".csv";
            SortedSet<Tournament> tournamentSet = Database.readMatchesFile(fileString);
            Tournament june = new Tournament(year*10000 + 600); //Represents June of that year
            //SortedSet<Tournament> head = tournamentSet.headSet(june);
            Tournament t1 = new Tournament(year*10000 + 400);
            Tournament t3 = new Tournament(year*10000 + 900);
            SortedSet<Tournament> q1 = tournamentSet.headSet(t1);
            SortedSet<Tournament> q2 = tournamentSet.subSet(t1, june);
            SortedSet<Tournament> q3 = tournamentSet.subSet(june, t3);
            SortedSet<Tournament> q4 = tournamentSet.tailSet(t3);

            Ranking.evaluatePeriod(q1);
            Ranking.evaluatePeriod(q2);
            Ranking.evaluatePeriod(q3);
            Ranking.evaluatePeriod(q4);

            Database.writeHead("glicko.csv", 10);
        }
        System.out.println("Ratings at the end of " + endYear);
        Player.printHeadByRating(10);
    }
}
