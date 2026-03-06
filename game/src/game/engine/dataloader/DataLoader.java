package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import game.engine.Role;
import game.engine.monsters.*;
import game.engine.cards.*;
import game.engine.cells.*;

public class DataLoader {

	// --- Attributes ---
	// Initialized once and accessed at class level as constants
	private static final String CARDS_FILE_NAME = "cards.csv";
	private static final String CELLS_FILE_NAME = "cells.csv";
	private static final String MONSTERS_FILE_NAME = "monsters.csv";

	// --- Methods ---

	// 1. Reads the MONSTERS_FILE_NAME CSV
	public static ArrayList<Monster> readMonsters() throws IOException {
		ArrayList<Monster> monsters = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(
				MONSTERS_FILE_NAME))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");

				// Format: (monsterType, name, description, role, energy) [cite:
				// 396]
				String monsterType = data[0];
				String name = data[1];
				String description = data[2];
				Role role = Role.valueOf(data[3]);
				int energy = Integer.parseInt(data[4]);

				switch (monsterType.trim().toUpperCase()) {
				case "DASHER":
					monsters.add(new Dasher(name, description, role, energy));
					break;
				case "DYNAMO":
					monsters.add(new Dynamo(name, description, role, energy));
					break;
				case "MULTITASKER":
					monsters.add(new MultiTasker(name, description, role,
							energy));
					break;
				case "SCHEMER":
					monsters.add(new Schemer(name, description, role, energy));
					break;
				}
			}
		}
		return monsters;
	}

	// 2. Reads the CARDS_FILE_NAME CSV
	public static ArrayList<Card> readCards() throws IOException {
		ArrayList<Card> cards = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(
				CARDS_FILE_NAME))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");

				String cardType = data[0].trim().toUpperCase();
				String name = data[1];
				String description = data[2];
				int rarity = Integer.parseInt(data[3]);

				// Format varies based on the cardType
				switch (cardType) {
				case "SWAPPER":
					cards.add(new SwapperCard(name, description, rarity));
					break;
				case "SHIELD":
					cards.add(new ShieldCard(name, description, rarity));
					break;
				case "ENERGYSTEAL":
					int energy = Integer.parseInt(data[4]);
					cards.add(new EnergyStealCard(name, description, rarity,
							energy));
					break;
				case "STARTOVER":
					boolean lucky = Boolean.parseBoolean(data[4]);
					cards.add(new StartOverCard(name, description, rarity,
							lucky));
					break;
				case "CONFUSION":
					int duration = Integer.parseInt(data[4]);
					cards.add(new ConfusionCard(name, description, rarity,
							duration));
					break;
				}
			}
		}
		return cards;
	}

	// 3. Reads the CELLS_FILE_NAME CSV [cite: 377]
	public static ArrayList<Cell> readCells() throws IOException {
		ArrayList<Cell> cells = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(
				CELLS_FILE_NAME))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");

				// DoorCell format: (name, role, energy)
				if (data.length == 3) {
					String name = data[0];
					Role role = Role.valueOf(data[1]);
					int energy = Integer.parseInt(data[2]);
					cells.add(new DoorCell(name, role, energy));
				}
				// Transport Cells format: (name, effect)
				else if (data.length == 2) {
					String name = data[0];
					int effect = Integer.parseInt(data[1]);

					// Positive effect (Conveyor Belt) or negative effect
					// (Contamination Sock) [cite: 392]
					if (effect > 0) {
						cells.add(new ConveyorBelt(name, effect));
					} else if (effect < 0) {
						cells.add(new ContaminationSock(name, effect));
					}
				}
			}
		}
		return cells;
	}
}