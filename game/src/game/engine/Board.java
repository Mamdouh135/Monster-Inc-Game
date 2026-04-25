package game.engine;

import java.util.ArrayList;

import game.engine.cards.Card;
import game.engine.cells.*;
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
		int col = index%10;
		int row = index/10;
		if(row%2 == 1) col = 9 - col;
		int[] pair = new int[]{row,col};
		return pair;
	}

	private Cell getCell(int index){
		int[] pair = indexToRowCol(index);
		return boardCells[pair[0]][pair[1]];
	}

	private void setCell(int index, Cell cell){
		int[] pair = indexToRowCol(index);
		boardCells[pair[0]][pair[1]] = cell;
	}

	public void initializeBoard(ArrayList<Cell> specialCells){
		ArrayList<Monster> sMonsters = getStationedMonsters();
		int[] monsterI = Constants.MONSTER_CELL_INDICES;
		ArrayList<Cell> monstercell = new ArrayList<Cell>();
		for (int i = 0; i < sMonsters.size(); i++) {
			sMonsters.get(i).setPosition(monsterI[i]);
			monstercell.add(new MonsterCell(sMonsters.get(i).getName(), sMonsters.get(i)));
		}
		
		//indices to set the cells on the board
		int scDoor = 0, scCont = 51, scConv = 50, monsterIndex = 0, cardIndex = 0,
			contIndex = 0, convIndex = 0;
		
		//get the constant indices arrays
		int[] cardI = Constants.CARD_CELL_INDICES;
		int[] convI = Constants.CONVEYOR_CELL_INDICES;
		int[] contI = Constants.SOCK_CELL_INDICES;
		
		for (int index = 0; index < 100; index++) {
			if(index%2 == 1)
			{
				setCell(index, specialCells.get(scDoor++));
			}
			else{
				if(index == monsterI[monsterIndex])
				{
					setCell(index, monstercell.get(monsterIndex++));
				}
				else if(index == cardI[cardIndex])
				{
					setCell(index, new CardCell("Card-Cell:"+index));
					cardIndex++;
				}
				else if(index == convI[convIndex])
				{
					setCell(index, specialCells.get(scConv));
					scConv+=2; convIndex++;
				}
				else if(index == contI[contIndex])
				{
					setCell(index, specialCells.get(scCont));
					scCont+=2; contIndex++;
				}
				else{
					setCell(index, new Cell("Cell:" + index));
				}
			}
		}
		
	}
	
}
