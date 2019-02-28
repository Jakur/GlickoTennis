package com.github.jakur.tennis;

import java.util.Collection;

/**
 * Created by jakur on 4/11/2017.
 */
public class Ranking {

    public static void evaluatePeriod(Collection<Tournament> tournaments) {
        tournaments.forEach(t -> t.matches.forEach(match -> {
            match.winner.addMatch(match);
            match.loser.addMatch(match);
        }));
        evaluateAll(Player.discoveredPlayers.values());
    }

    static void evaluateAll(Collection<Player> players) {
        players.forEach(Ranking::evaluate);
        players.forEach(p -> {
            if(p.status == Player.Status.ACTIVE) {
                p.currentRating = p.nextRating;
                p.nextRating = null;
                p.inactiveCount = 0;
                p.status = Player.Status.INACTIVE;
            }
            else {
                p.inactiveCount++;
                if(p.inactiveCount > 4) {
                    p.status = Player.Status.MISSING;
                }
            }
        });
    }

    static void evaluate(Player player) {
        switch(player.status) {
            case ACTIVE:
                Ranking r = new Ranking(player);
                r.calculate();
                break;
            case INACTIVE:
                Glicko g = player.currentRating;
                double phiStar = getPhiStar(g.volatility, g.deviation);
                player.currentRating = new Glicko(g.rating, phiStar, g.volatility);
                player.inactiveCount++;
                break;
        }

    }

    private static final double TAO = 0.7;

    private Player primaryPlayer;
    private double v;
    private double delta;
    private double mainMu;
    private double mainPhi;
    private double mainSigma;
    private double smallA;

    private Ranking(Player player) {
        this.primaryPlayer = player;
        this.v = 0; //Estimated variance
        this.delta = 0; //Estimated improvement
        Glicko playerRating = player.getCurrentRating();
        mainMu = (playerRating.rating - 1500) / 173.7178;
        mainPhi = playerRating.deviation / 173.7178;
        mainSigma = playerRating.volatility;
    }

    private static double getPhiStar(double sigma, double phi) {
        return Math.sqrt(phi*phi + sigma*sigma);
    }

    private void calculate() {
        primaryPlayer.recentMatches.forEach(this::evaluateMatch); //Computes the sums for v and delta
        v = 1 / v;
        delta *= v;

        //Begin the Illinois Algorithm to find the new sigma value
        smallA = Math.log(mainSigma * mainSigma);
        double epsilon = 0.000001; //Convergence tolerance
        //The values A and B are chosen to bracket ln(sigma*sigma)
        double bigA = smallA;
        double bigB;
        if(delta * delta > mainPhi * mainPhi + v) {
            bigB = Math.log(delta*delta - mainPhi*mainPhi - v);
        }
        else {
            int k;
            for(k = 1; functionF(smallA - k * TAO) < 0; k++) {
                System.out.println(k);
            }
            bigB = smallA - k * TAO;
        }

        //The remainder of the algorithm iteratively narrows this bracket

        double fa = functionF(bigA);
        double fb = functionF(bigB);
        while(Math.abs(bigB - bigA) > epsilon) {
            double bigC = bigA + ((bigA - bigB) * fa) / (fb - fa);
            double fc = functionF(bigC);
            if(fc*fb < 0) {
                bigA = bigB;
                fa = fb;
            } else {
                fa /= 2;
            }
            bigB = bigC;
            fb = fc;
        }

        double sigmaPrime = Math.exp(bigA / 2);
        double phiStar = getPhiStar(sigmaPrime, mainPhi);
        double phiPrime = 1 / Math.sqrt(1/(phiStar*phiStar) + 1 / v);
        double muPrime = mainMu + (phiPrime * phiPrime) * (delta / v);
        primaryPlayer.setNextRating(new Glicko(173.7178 * muPrime + 1500,
                173.7178 * phiPrime, sigmaPrime));

    }

    private double functionF(double x) {
        double phiSquared = mainPhi * mainPhi;
        double eX = Math.exp(x);
        double topLeft = eX * (delta*delta - phiSquared - v - eX);
        double bottomLeft = 2 * (phiSquared + v + eX) * (phiSquared + v + eX);
        return topLeft / bottomLeft - ((x - smallA) / (TAO * TAO));
    }

    private void evaluateMatch(Match match) {
        double result;
        Player winner = match.winner;
        Glicko secondaryPlayerRating;
        if(primaryPlayer == winner) {
            result = 1;
            Player loser = match.loser;
            secondaryPlayerRating = loser.getCurrentRating();
        }
        else {
            result = 0;
            secondaryPlayerRating = winner.getCurrentRating();
        }
        double otherPhi = secondaryPlayerRating.deviation / 173.7178;
        double eReturn = functionE(mainMu, (secondaryPlayerRating.rating - 1500) / 173.7178, otherPhi);
        double gReturn = functionG(otherPhi);
        v += ( (gReturn * gReturn) * eReturn * (1 - eReturn) );
        delta += ( gReturn * (result - eReturn));
    }

    private double functionG(double phi) {
        return 1 / Math.sqrt(1 + (3*phi*phi) / (Math.PI * Math.PI));
    }

    //The expected outcome of the match for the player, ranging from 0 = guaranteed loss to 1 = guaranteed win
    private double functionE(double mainMu, double otherMu, double otherPhi) {
        return 1 / (1 + Math.exp(-1 * functionG(otherPhi) * (mainMu - otherMu)));
    }
}
