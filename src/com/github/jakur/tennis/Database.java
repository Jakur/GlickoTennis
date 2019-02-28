package com.github.jakur.tennis;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by jakur on 4/10/2017.
 */
public class Database {

    public static void readPlayers() {
        String line = "";
        try {
            String fileString = "players/atp_players.csv";
            FileInputStream file = new FileInputStream(fileString);
            InputStreamReader reader = new InputStreamReader(file, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(reader);
            for(; (line = br.readLine()) != null; ) {
                String[] strings = line.split(",");
                int id = Integer.parseInt(strings[0]);
                if(strings.length > 5) {
                    Player.players.put(id, new Player(id, strings[1] + " " + strings[2], strings[5], strings[4]));
                }
                else if(strings.length > 2) {
                    Player.players.put(id, new Player(id, strings[1] + " " + strings[2]));
                }
            }
            br.close();
            reader.close();
            file.close();
        } catch(Exception e) {
            System.out.println("The players db line that caused the error: \n" + line);
            e.printStackTrace();
        }
    }

    public static SortedSet<Tournament> readMatchesFile(String fileString) {
        try {
            FileInputStream file = new FileInputStream(fileString);
            InputStreamReader reader = new InputStreamReader(file, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(reader);
            String line;
            br.readLine();
            TreeSet<Tournament> tournaments = new TreeSet<>();
            /*
            This whole Tournament / Match system only exists because the tournaments are not properly ordered by date
            in all of the files.
            line[0] = tournament id, line[2] = surface, line[5] = date, line[7] = winnerId, line[17] = loserId
             */
            String tournamentString = "";
            Tournament tournament = new Tournament(0); //Placeholder tournament
            while((line = br.readLine()) != null) {
                String[] strings = line.split(",");
                if(strings.length < 18) { //Handles random blank lines or otherwise too short lines
                    continue;
                }
                if(strings[10].equals("U Unknown") || strings[20].equals("U Unknown")) {
                    continue;
                }
                try {
                    Player player1 = Player.players.get(Integer.parseInt(strings[7]));
                    Player player2 = Player.players.get(Integer.parseInt(strings[17]));

                    Match match = new Match(player1, player2);
                    if(strings[0].equals(tournamentString)) { //Same tournament as last iteration(s)
                        tournament.matches.add(match);
                    }
                    else {
                        tournament = new Tournament(Integer.parseInt(strings[5]));
                        tournaments.add(tournament);
                        tournament.matches.add(match);
                        tournamentString = strings[0];
                    }
                    if(player1.status != Player.Status.ACTIVE) {
                        player1.recentMatches = new HashSet<>();
                        if(player1.status == Player.Status.UNDISCOVERED) {
                            Player.discoveredPlayers.put(player1.getId(), player1);
                        }
                    }

                    if(player2.status != Player.Status.ACTIVE) {
                        player2.recentMatches = new HashSet<>();
                        if(player2.status == Player.Status.UNDISCOVERED) {
                            Player.discoveredPlayers.put(player2.getId(), player2);
                        }
                    }
                    player1.status = Player.Status.INACTIVE;
                    player2.status = Player.Status.INACTIVE;
                } catch(Exception e) {
                    System.out.println("Failed to process following line:");
                    System.out.println(line);
                }

            }
            br.close();
            reader.close();
            file.close();
            return tournaments;
        } catch(Exception e) {
            System.out.println("ATP matches file(s) not detected!");
            return null;
        }
    }

    public static void writeHead(String fileString, int numberToPrint) {
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileString, true), "utf-8"));
            ArrayList<Player> sortedPlayers = new ArrayList<>(Player.discoveredPlayers.values());
            sortedPlayers.sort(Comparator.comparing(p -> p.currentRating.rating *-1));
            writer.write(Integer.toString(Main.year) + "\n");
            for(int i = 0; i < numberToPrint; i++) {
                Player p = sortedPlayers.get(i);
                if(p.status == Player.Status.MISSING) {
                    numberToPrint++;
                    continue;
                }
                Glicko g = p.currentRating;
                writer.write(p.getName() + "," + g.rating + "," + g.deviation + "," + g.volatility + "\n");
            }
            writer.close();

        }
        catch(Exception e) {
            System.out.println("Failed to write to file");
        }
    }
}
