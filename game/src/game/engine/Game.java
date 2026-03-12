package game.engine;

import game.engine.cards.Card;
import java.util.ArrayList;
import java.io.IOException;
import game.engine.monsters.Monster;
import game.engine.dataloader.DataLoader;
import java.util.Random;

public class Game {
	private Board board;

	private ArrayList<Monster> allMonsters;

	private Monster player;

	private Monster opponent;

	private Monster current;

	public Game(Role playerRole) throws IOException {

		this.board = new Board(DataLoader.readCards());

		this.allMonsters = DataLoader.readMonsters();

		this.player = selectRandomMonsterByRole(playerRole);

		Role opponentRole = (playerRole == Role.SCARER) ? Role.LAUGHER
				: Role.SCARER;
		this.opponent = selectRandomMonsterByRole(opponentRole);

		this.current = this.player;

	}

	private Monster selectRandomMonsterByRole(Role role) {

		ArrayList<Monster> matchingMonsters = new ArrayList<>();

		for (Monster currentMonster : this.allMonsters) {

			if (currentMonster.getRole() == role) {
				matchingMonsters.add(currentMonster);
			}
		}

		if (matchingMonsters.isEmpty()) {
			return null;
		}

		Random randomGenerator = new Random();
		int randomIndex = randomGenerator.nextInt(matchingMonsters.size());

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
