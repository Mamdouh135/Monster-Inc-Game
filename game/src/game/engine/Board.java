package game.engine;

import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		
		// MUST call these to populate the decks when the game starts!
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}

	private int[] indexToRowCol(int index){
		int col = index % 10;
		int row = index / 10;
		if (row % 2 == 1) col = 9 - col;
		return new int[]{row, col};
	}

	private Cell getCell(int index){
		int[] pair = indexToRowCol(index);
		return boardCells[pair[0]][pair[1]];
	}

	private void setCell(int index, Cell cell){
		int[] pair = indexToRowCol(index);
		boardCells[pair[0]][pair[1]] = cell;
	}

	public void initializeBoard(ArrayList<Cell> specialCells) {
		ArrayList<Monster> sMonsters = getStationedMonsters();
		int[] monsterI = Constants.MONSTER_CELL_INDICES;
		ArrayList<Cell> monstercell = new ArrayList<Cell>();
		
		for (int i = 0; i < sMonsters.size(); i++) {
			sMonsters.get(i).setPosition(monsterI[i]);
			monstercell.add(new MonsterCell(sMonsters.get(i).getName(), sMonsters.get(i)));
		}
		
		// 1. Group the special cells by type so we don't rely on hardcoded indices!
		ArrayList<Cell> doors = new ArrayList<>();
		ArrayList<Cell> conveyors = new ArrayList<>();
		ArrayList<Cell> socks = new ArrayList<>();
		
		for (Cell c : specialCells) {
			if (c instanceof DoorCell) doors.add(c);
			else if (c instanceof ConveyorBelt) conveyors.add(c);
			else if (c instanceof ContaminationSock) socks.add(c);
		}
		
		int doorIdx = 0, beltIdx = 0, sockIdx = 0, monsterIdx = 0;
		
		// 2. Safely place everything on the board
		for (int index = 0; index < 100; index++) {
			if (index % 2 == 1) {
				setCell(index, doors.get(doorIdx++));
			} 
			else if (contains(Constants.MONSTER_CELL_INDICES, index)) {
				setCell(index, monstercell.get(monsterIdx++));
			} 
			else if (contains(Constants.CARD_CELL_INDICES, index)) {
				setCell(index, new CardCell("Card-Cell:" + index));
			} 
			else if (contains(Constants.CONVEYOR_CELL_INDICES, index)) {
				setCell(index, conveyors.get(beltIdx++));
			} 
			else if (contains(Constants.SOCK_CELL_INDICES, index)) {
				setCell(index, socks.get(sockIdx++));
			} 
			else {
				setCell(index, new Cell("Cell:" + index));
			}
		}
	}

	// Helper method to prevent Array Index Out Of Bounds!
	private boolean contains(int[] array, int value) {
		for (int i : array) {
			if (i == value) return true;
		}
		return false;
	}

	private void setCardsByRarity() {
		// Expand the originalCards array based on the rarity of each card
		ArrayList<Card> expanded = new ArrayList<>();
		for (Card card : originalCards) {
			for (int j = 0; j < card.getRarity(); j++) {
				expanded.add(card);
			}
		}
		originalCards = expanded;
	}

	public static void reloadCards() {
		cards.clear();
		cards.addAll(originalCards); // Copy the expanded original deck into the active deck
		Collections.shuffle(cards);
	}

	public static Card drawCard() {
		if (cards.isEmpty()) {
			reloadCards();
		}
		return cards.remove(0);
	}

	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		int newPos = (currentMonster.getPosition() + roll) % 100;
		int oldPos = currentMonster.getPosition();
		
		if (newPos == opponentMonster.getPosition()) {
			throw new InvalidMoveException();
		}
		
		currentMonster.setPosition(newPos);
		Cell c = getCell(newPos);
		
		if (currentMonster.getConfusionTurns() > 0) {
			int confusionTurns = currentMonster.getConfusionTurns();
			currentMonster.setConfusionTurns(confusionTurns - 1);
			opponentMonster.setConfusionTurns(confusionTurns - 1);
		}
		
		c.onLand(currentMonster, opponentMonster);
		
		if (currentMonster.getPosition() == opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPos);
			throw new InvalidMoveException();
		}
		
		updateMonsterPositions(currentMonster, opponentMonster);
	}

	private void updateMonsterPositions(Monster player, Monster opponent) {
		int pIndex = player.getPosition();
		int oIndex = opponent.getPosition();
		for (int i = 0; i < 100; i++) {
			Cell c = getCell(i);
			if (i == pIndex) {
				c.setMonster(player);
			} else if (i == oIndex) {
				c.setMonster(opponent);
			} else {
				c.setMonster(null);
			}
		}
	}
}














