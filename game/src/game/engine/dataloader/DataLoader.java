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
	public static String CARDS_FILE_NAME = "cards.csv"; // A String
																// containing
																// the name of
																// the card’s
																// csv file.
	public static String CELLS_FILE_NAME = "cells.csv"; // A String
																// containing
																// the name of
																// the cell’s
																// csv file.
	public static String MONSTERS_FILE_NAME = "monsters.csv";// A String
																	// containing
																	// the name
																	// of the
																	// monster’s
																	// csv file.

	public static ArrayList<Card> readCards() throws IOException {

		ArrayList<Card> cards = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(
				CARDS_FILE_NAME))) {
			String line;
			while ((line = br.readLine()) != null) {
				// split by comma
				String[] data = line.split(",");
				// Extract data based on the format :((cardType, name,
				// description, rarity)) as basic,
				// after that we check based on each card what extra info we
				// need to take;
				String cardType = data[0];
				String name = data[1];
				String description = data[2];
				int rarity = Integer.parseInt(data[3]);
				// Switch based on the specific type of card to handle the extra
				// columns correctly
				switch (cardType) {
				case "SwapperCard":
					// (cardType, name, description, rarity)
					cards.add(new SwapperCard(name, description, rarity));
					break;

				case "ShieldCard":
					// (cardType, name, description, rarity)
					cards.add(new ShieldCard(name, description, rarity));
					break;

				case "EnergyStealCard":
					// (cardType, name, description, rarity, energy)
					int energy = Integer.parseInt(data[4]);
					cards.add(new EnergyStealCard(name, description, rarity,
							energy));
					break;

				case "StartOverCard":
					// (cardType, name, description, rarity, lucky)
					boolean lucky = Boolean.parseBoolean(data[4]);
					cards.add(new StartOverCard(name, description, rarity,
							lucky));
					break;

				case "ConfusionCard":
					// (cardType, name, description, rarity, duration)
					int duration = Integer.parseInt(data[4]);
					cards.add(new ConfusionCard(name, description, rarity,
							duration));
					break;
				}

			}
			br.close();
		}
		return cards;

	}

	public static ArrayList<Monster> readMonsters() throws IOException {
		ArrayList<Monster> monsters = new ArrayList<>();

		// Open the file. (Assumes the file is right outside the src folder )
		try (BufferedReader br = new BufferedReader(new FileReader(
				MONSTERS_FILE_NAME))) {
			String line;

			// Read line by line
			while ((line = br.readLine()) != null) {
				// Split by comma
				String[] data = line.split(",");

				// Extract data based on the format: (monsterType, name,
				// description, role, energy)
				String monsterType = data[0];
				String name = data[1];
				String description = data[2];
				Role role = Role.valueOf(data[3]); // Converts String to the
													// Enum
				int energy = Integer.parseInt(data[4]);

				// Create the specific subclass based on monsterType
				switch (monsterType) {
				case "Dasher":
					monsters.add(new Dasher(name, description, role, energy));
					break;
				case "Dynamo":
					monsters.add(new Dynamo(name, description, role, energy));
					break;
				case "MultiTasker":
					monsters.add(new MultiTasker(name, description, role,
							energy));
					break;
				case "Schemer":
					monsters.add(new Schemer(name, description, role, energy));
					break;
				}
			}
			br.close();
		}
		return monsters;
	}

	public static ArrayList<Cell> readCells() throws IOException {
		ArrayList<Cell> cells = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(
				CELLS_FILE_NAME))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				if (data.length == 3) {
					String name = data[0];
					Role role = Role.valueOf(data[1]);
					int energy = Integer.parseInt(data[2]);
					cells.add(new DoorCell(name, role, energy));

				}
				// the easy part is that each type has his own length, easier
				// than above in readCards for instance
				if (data.length == 2) {
					String name = data[0];
					int effect = Integer.parseInt(data[1]);
					if (effect > 0) {
						cells.add(new ConveyorBelt(name, effect));
					} else {
						cells.add(new ContaminationSock(name, effect));
					}
				}
			}
			br.close();
		}
		
		return cells;

	}

}
