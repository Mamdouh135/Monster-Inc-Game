package game.engine;
import game.engine.cards.Card;
import java.util.ArrayList;
import java.io.IOException;
import game.engine.monsters.Monster;
import game.engine.dataloader.DataLoader;
import java.util.Random;

//Main game engine class that manages the flow of the game

public class Game {
	private Board board;
	// the game board

	private ArrayList<Monster> allMonsters;
	// list of all available moonsters read from the CSV

	private Monster player;
	// the player's monster

	private Monster opponent;
	// The opponent's monster

	private Monster current;

	// The monster whose currently playing the turn , read and write;

	public Game(Role playerRole) throws IOException {
		// creating the baord with the loaded cards from the csv
		this.board = new Board(DataLoader.readCards());

		this.player = selectRandomMonsterByRole(playerRole);

		// Using the DataLoader class to read the monsters [cite: 378]
		this.allMonsters = DataLoader.readMonsters();

		// randomly selecting opponent based on the opposite to player's chosen
		// role
		// Find the opposite role first
		Role opponentRole = (playerRole == Role.SCARER) ? Role.LAUGHER
				: Role.SCARER;
		this.opponent = selectRandomMonsterByRole(opponentRole);

		// setting the current with the player
		this.current = this.player;

	}

	private Monster selectRandomMonsterByRole(Role role) {
		// Step 1: Create a temporary list to hold all monsters that match the
		// requested role
		ArrayList<Monster> matchingMonsters = new ArrayList<>();

		// Step 2: Loop through the allMonsters list
		for (Monster currentMonster : this.allMonsters) {
			// Check if the monster's role matches the role we are looking for
			// (Assuming you have a standard getter for the role attribute
			// [cite: 129, 36])
			if (currentMonster.getRole() == role) {
				matchingMonsters.add(currentMonster);
			}
		}

		// Safety check: if there are no monsters of that role, return null
		if (matchingMonsters.isEmpty()) {
			return null;
		}

		// Step 3: Randomly pick an index from our matching list
		Random randomGenerator = new Random();
		int randomIndex = randomGenerator.nextInt(matchingMonsters.size());

		// Step 4: Return the chosen monster
		return matchingMonsters.get(randomIndex);

	}

	public Monster getCurrent() {
		return current;
	}

	public void setCurrent(Monster current) {
		this.current = current;
	}

	public Board getBoard() {
		return board;
	}

	public ArrayList<Monster> getAllMonsters() {
		return allMonsters;
	}

	public Monster getPlayer() {
		return player;
	}

	public Monster getOpponent() {
		return opponent;
	}

}
