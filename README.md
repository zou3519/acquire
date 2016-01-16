Acquire
================================

![Image of Acquire game in progress](/readme/game.png)

This is a computer clone of the old-school multi-player board game Acquire which was originally designed by [Sid Sackson](https://en.wikipedia.org/wiki/Acquire). The objective of Acquire is to earn the most money through investing in and developing hotel chains. 

The game features intelligent AIs for humans to play against. The AIs run variants of an imperfect information [Monte Carlo Tree Search](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search) algorithm that simulates random game play to make decisions. This type of algorithm is significant in that it is able to develop strategy by being given the rules of the game; one can take the algorithm and modify it minimally to work for other games. The game is written in Scala 2.11 and uses JavaFX (Java 8) for graphical rendering.

I chose to make this Acquire clone so that I could better understand the game and have intelligent AI's to play with in my free time. 

Building and Running 
-----------------------
Building requires Scala [2.11](http://www.scala-lang.org/index.html) and [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). The main class of the project is `acquire.gui.main`.
Running requires the [Java 8 runtime](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). If you have a prebuilt JAR file you will only need the Java 8 runtime. Simply double-click the JAR or run `java -jar acquire.jar` on the command line.

Rules
-----------------------

The main objective of Acquire is to earn the most money by the end of the game. To do this, players can form hotel (corporation) chains, buy shares of the hotel chains, merge chains to receive payouts, and expand hotel chains to increase the valuation of their shares.

Player start each game with $6,000 in hand. Each player receives 6 location tiles that are hidden from the other players. Play proceeds in a circular manner.

During his/her turn, a player performs the following actions, in order:

1. **Place one of his/her location tiles on the board.** This may result in the creation of a new chain (founding a new corporation), the merger of two or more chains, or the expansion of a hotel chain. All actions pertaining to the founding of a new chain or the merger of chains need to be completed before the next point.

2. **Buy shares.** A player may buy shares in hotel chains (corporations) after he/she has placed his/her tile. Each hotel chain has a total of 25 shares that may be purchased.

3. **End turn.** The player may decide to end his turn, or end the game if the end game conditions are met. The player draws a random unplayed location tile after he/she has ended his turn.

See the below sections for more details on what happens in each of these phases.

### Placing tiles
Each tile has four neighbors that may or may not be present (depending on if they have been played) There are four things that can happen when a tile is placed:

1. If the placed tile has no neighbors, then it becomes an orphaned tile that may later be added to a hotel chain

2. If the placed tile's neighbors are all orphaned (not assigned to a hotel chain), then a new hotel chain will be founded with the tile and its neighbors.

3. If the placed tile's neighbors are all members of one hotel chain or are orphaned, then the placed tile will extend the hotel chain; becoming a part of that specific chain. Its neighbors will also become a part of that hotel chain.

4. If the placed tile connects two or more different hotel chains, then a merger will occur.

Sometimes, tiles cannot be played. A tile cannot be played if:

* It would found an 8th chain
* It would merge two or more safe chains (hotel chains that have >= 11 tiles)

If a player cannot place any of his/her tiles, then he proceeds to choose which shares to purchase.

### Founding new chains
When a player places a tile next to one that is already on the board (that is not a part of a chain), a new chain will be formed. The player who played that tile picks from seven possible chains to form. Then, the player receives one free share of the chain he/she formed (if available).

### Merging hotel chains
When a player places a tile that is next to two or more chains, a merger occurs. The hotel chain that is the largest will take over the other(s). If multiple hotel chain have the same size, the player who initiated the merger chooses which survives the merger. 

A hotel chain that has 11 or more tiles is deemed "safe" and cannot be taken over in a merger but may still take over other un-safe hotel chains.

When a merger is occurring, the following occurs, in order:

1. The player decides which hotel chain survives, if necessary. 

2. **Pay out bonuses.** The two shareholders with the most shares of the defunct hotel chain receive merger bonuses from the bank. The largest shareholder receives the majority bonus (of the defunct chain) and the second-largest shareholder receives the minority bonus (of the defunct chain). If there are ties for largest shareholder, those players will split the majority and minority bonuses evenly (and no other shareholders will receive bonuses). If there are ties for second-largest shareholders, those players will split the minority bonus evenly. If only one player owns shares of the defunct chain, he/she receives both the majority and minority bonuses.

3. **Dispose of shares.** Players may choose to dispose of their shares in the defunct hotel chain. Starting with the player who began the merger, going around in a circle, each player may chose to handle his/her shares of the defunct chain with a combination of the following options:

  * **Keep**: A player may keep some of the shares of the defunct chain. They will not be worth any money unless the defunct chain is reformed.

  * **Sell**: A player may sell some of the shares of the defunct chain back to the bank for the current share price.

  * **Trade**: A player may trade in shares of the defunct chain to the bank to receive half the amount of shares of the surviving chain, if the bank has enough shares to provide.

4. The defunct chain's tiles are changed to be those of the hotel chain that took it over.

In the case of multiple mergers, the largest hotel chain takes over the next-largest chain first (with the player making decisions if there are ties in chain size). Then, the largest hotel chain takes over the next-largest chain, and so on.

### Hotel Chains 101

Each chain is denoted with a different color and name. The shares of each chain are priced differently as a function of the chain and the current chain size. The majority and minority bonuses are also a function of the chain and the current chain size. Please refer to [this well-done information sheet](http://www.webnoir.com/bob/sid/acquirecard.htm) for a reference as to how the chains are priced.

### Buying shares

After he/she has placed a location tile on the board, the player may choose to buy up to three shares of hotel chain stock. Hotel chain stock is available for purchase only if the hotel currently has a chain on the board and if the bank has remaining shares. Players may not transfer away their shares unless a merger occurs.

### Ending the game

A player may end the game at the end of his/her turn if one of the following end game conditions has been met:

1. There exists one chain with more than 41 tiles

2. All existing hotel chains are safe, ie, have at least 11 shares, and there exists at least one hotel chain.

When the game ends, the bank pays out majority and minority bonuses for each hotel chain in existence. The player with the highest net worth (cash + shares) after bonuses have been paid out wins!

References
---------------------

The rules were referenced from [Avalon Hill's website](https://www.wizards.com/avalonhill/rules/acquire.pdf). Avalon Hill and Hasbro own the rights to the game of Acquire.
