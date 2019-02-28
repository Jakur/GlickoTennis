# GlickoTennis
This is an implementation of Glicko 2 ratings (as outlined at http://www.glicko.net/glicko/glicko2.pdf) for ATP matches.

It is intended for use with ATP matches history provided by https://github.com/JeffSackmann/tennis_atp . 

Put the matches data in a subdirectory "matches" and the players data in a subdirectory "players" and then run the program
with an argument of what year you want the ratings computed to. The top ten players' ratings at the end of the year for each year
will be exported to glicko.csv
