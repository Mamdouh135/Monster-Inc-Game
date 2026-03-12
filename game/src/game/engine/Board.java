package game.engine;

import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;

import java.util.ArrayList;

public class Board {
	private Cell[][] boardCells;

	private static ArrayList<Monster> stationedMonsters;

	private static ArrayList<Card> originalCards;

	public static ArrayList<Card> cards;

	public Board(ArrayList<Card> readCards) {
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];

		stationedMonsters = new ArrayList<>();

		cards = new ArrayList<>();

		originalCards = readCards;

	}

	
	public static ArrayList<Card> getCards() {
		return cards;
	}

	public static void setCards(ArrayList<Card> newcards) {
		cards = newcards;
	}

	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}

	public Cell[][] getBoardCells() {
		return boardCells;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}

	public static void setStationedMonsters(ArrayList<Monster> newMonsters) {
		stationedMonsters = newMonsters;
	}

}
