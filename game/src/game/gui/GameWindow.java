package game.gui;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
public class GameWindow {
    private Stage stage;
    private Game game;
    private GridPane boardGrid;
    private Label playerInfo, opponentInfo, turnInfo;
    private Button rollButton, powerupButton;
    private StackPane[] cellPanes = new StackPane[100];
    private VBox logBox;

    public GameWindow(Stage stage, String side) {
        this.stage = stage;
        try {
            Role role = Role.valueOf(side);
            this.game = new Game(role);
        } catch (Exception e) {
            showError("Failed to initialize game engine: " + e.getMessage());
            return;
        }
        setupUI();
    }

    private void setupUI() {
        BorderPane mainLayout = new BorderPane();

        // 1. Setup Board Grid
        boardGrid = new GridPane();
        boardGrid.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10; -fx-hgap: 4; -fx-vgap: 4;");
        
        for (int i = 0; i < 100; i++) {
            int row = i / 10;
            int col = i % 10;
            if (row % 2 == 1) col = 9 - col; // Zigzag logic

            StackPane cellPane = new StackPane();
            cellPane.setMinSize(65, 65);
            
            Label idxLabel = new Label(String.valueOf(i));
            idxLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            StackPane.setAlignment(idxLabel, Pos.TOP_LEFT);
            
            cellPane.getChildren().add(idxLabel);
            boardGrid.add(cellPane, col, 9 - row); // Render bottom-up
            cellPanes[i] = cellPane;
        }
        mainLayout.setCenter(boardGrid);

        // 2. Setup Info Panel
        turnInfo = new Label();
        playerInfo = new Label();
        opponentInfo = new Label();
        
        turnInfo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));

        rollButton = new Button("ROLL DICE");
        powerupButton = new Button("USE POWERUP (500)");
        styleButton(rollButton, "#3498db");
        styleButton(powerupButton, "#2ecc71");
        
        rollButton.setOnAction(e -> handleRollDice());
        powerupButton.setOnAction(e -> handlePowerup());

        VBox playerCard = createStatusCard(playerInfo, "#e8f8f5", "#1abc9c");
        VBox oppCard = createStatusCard(opponentInfo, "#fdedec", "#e74c3c");

        VBox infoPanel = new VBox(20, turnInfo, playerCard, rollButton, powerupButton, oppCard);
        infoPanel.setAlignment(Pos.TOP_CENTER);
        infoPanel.setStyle("-fx-padding: 20; -fx-min-width: 350; -fx-background-color: #ecf0f1;");
        mainLayout.setRight(infoPanel);

        // 3. Setup Action Log
        logBox = new VBox(5);
        logBox.setStyle("-fx-background-color: #fff; -fx-padding: 10; -fx-border-color: #bdc3c7; -fx-pref-height: 150;");
        VBox bottomPanel = new VBox(new Label("Action Log:"), logBox);
        bottomPanel.setStyle("-fx-padding: 10;");
        mainLayout.setBottom(bottomPanel);

        updateBoard();

        Scene scene = new Scene(mainLayout, 1100, 850);
        stage.setScene(scene);
    }

    private void updateBoard() {
        Monster player = game.getPlayer();
        Monster opponent = game.getOpponent();
        Cell[][] engineCells = game.getBoard().getBoardCells();

        for (int i = 0; i < 100; i++) {
            StackPane pane = cellPanes[i];
            
            // --- THE FIX: Clear everything EXCEPT the cell index number (which is always index 0)
            if (pane.getChildren().size() > 1) {
                javafx.scene.Node indexLabel = pane.getChildren().get(0);
                pane.getChildren().clear();
                pane.getChildren().add(indexLabel);
            }

            int row = i / 10;
            int col = (row % 2 == 1) ? 9 - (i % 10) : (i % 10);
            Cell engineCell = engineCells[row][col];

            // Render Cell Type Colors & Info
            String bgColor = "#ffffff";
            String cellText = "";

            if (engineCell instanceof DoorCell) {
                DoorCell dc = (DoorCell) engineCell;
                bgColor = dc.getRole() == Role.SCARER ? "#d2b4de" : "#f9e79f"; // Purple or Yellow
                cellText = "Door\nEnergy: " + dc.getEnergy();
            } else if (engineCell instanceof MonsterCell) {
                bgColor = "#aed6f1"; // Light Blue
                // Get the stationed monster and display its name
                Monster stationed = ((MonsterCell) engineCell).getCellMonster();
                if (stationed != null) {
                    cellText = stationed.getName();
                } else {
                    cellText = "Stationed\nMonster";
                }
            } else if (engineCell instanceof CardCell) {
                bgColor = "#f5b7b1"; // Light Red
                cellText = "Card";
            } else if (engineCell instanceof ConveyorBelt) {
                bgColor = "#abebc6"; // Green
                cellText = "Belt +" + ((ConveyorBelt)engineCell).getEffect();
            } else if (engineCell instanceof ContaminationSock) {
                bgColor = "#edbb99"; // Orange
                cellText = "Sock -" + Math.abs(((ContaminationSock)engineCell).getEffect());
            }

            pane.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: #7f8c8d;");
            
            if (!cellText.isEmpty()) {
                Label typeLabel = new Label(cellText);
                typeLabel.setFont(Font.font("Arial", 10));
                // Center align text for longer monster names
                typeLabel.setStyle("-fx-text-alignment: center;"); 
                pane.getChildren().add(typeLabel);
                StackPane.setAlignment(typeLabel, Pos.CENTER);
            }

            // Render the TWO Active Players
            if (i == player.getPosition() || i == opponent.getPosition()) {
                // HBox groups the tokens so they sit side-by-side if they land on the same cell
                HBox monsterBox = new HBox(4); 
                monsterBox.setAlignment(Pos.BOTTOM_CENTER);
                monsterBox.setStyle("-fx-padding: 0 0 5 0;"); // Give some padding from the bottom
                
                if (i == player.getPosition()) {
                    Circle pToken = new Circle(10, Color.DODGERBLUE);
                    pToken.setStroke(Color.DARKBLUE);
                    pToken.setStrokeWidth(2);
                    monsterBox.getChildren().add(pToken);
                }
                if (i == opponent.getPosition()) {
                    Circle oToken = new Circle(10, Color.CRIMSON);
                    oToken.setStroke(Color.DARKRED);
                    oToken.setStrokeWidth(2);
                    monsterBox.getChildren().add(oToken);
                }
                pane.getChildren().add(monsterBox);
            }
        }

        // Update Info Text
        playerInfo.setText(formatMonsterStats(player, "PLAYER"));
        opponentInfo.setText(formatMonsterStats(opponent, "OPPONENT"));
        
        boolean isPlayerTurn = (game.getCurrent() == player);
        turnInfo.setText(isPlayerTurn ? "YOUR TURN" : "OPPONENT'S TURN");
        rollButton.setDisable(!isPlayerTurn);
        powerupButton.setDisable(!isPlayerTurn || player.getEnergy() < 500);
    }

    private void handleRollDice() {
        try {
            java.lang.reflect.Method rollMethod = game.getClass().getDeclaredMethod("rollDice");
            rollMethod.setAccessible(true);
            int roll = (int) rollMethod.invoke(game);
            
            logAction("You rolled a " + roll);
            
            // Execute your turn
            game.playTurn(); 
            
        } catch (Exception e) {
            showError("Invalid Move or Engine Error.");
        }
        
        updateBoard();
        checkWinState();
        
        // NEW: If the game isn't over and it's the opponent's turn, trigger their move
        if (game.getWinner() == null && game.getCurrent() == game.getOpponent()) {
            triggerOpponentTurn();
        }
    }

    private void handlePowerup() {
        try {
            game.usePowerup();
            logAction("Powerup Activated!");
            updateBoard();
            
            // Just in case activating a powerup ends the turn in the future
            if (game.getCurrent() == game.getOpponent()) {
                triggerOpponentTurn();
            }
        } catch (OutOfEnergyException e) {
            showError("Not enough energy to use powerup!");
        }
    }

    private void checkWinState() {
        Monster winner = game.getWinner();
        if (winner != null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("We have a winner!");
            alert.setContentText(winner.getName() + " (" + winner.getRole() + ") has won the game with " + winner.getEnergy() + " energy!");
            alert.showAndWait();
            stage.close(); // Return to main menu logic goes here
        }
    }

    private String formatMonsterStats(Monster m, String title) {
        return title + "\n" +
               "Name: " + m.getName() + "\n" +
               "Role: " + m.getRole() + (m.isConfused() ? " (CONFUSED!)" : "") + "\n" +
               "Type: " + m.getClass().getSimpleName() + "\n" +
               "Energy: " + m.getEnergy() + "\n" +
               "Shield: " + (m.isShielded() ? "ACTIVE" : "None");
    }

    private VBox createStatusCard(Label targetLabel, String bgColor, String borderColor) {
        VBox box = new VBox(targetLabel);
        targetLabel.setFont(Font.font("Consolas", 14));
        box.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor + "; -fx-border-width: 3; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
        return box;
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-min-width: 200;");
    }

    private void logAction(String msg) {
        Label l = new Label(msg);
        logBox.getChildren().add(0, l);
        if (logBox.getChildren().size() > 8) {
            logBox.getChildren().remove(8);
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(AlertType.WARNING, msg);
        alert.setHeaderText("Action Invalid");
        alert.showAndWait();
    }
    private void triggerOpponentTurn() {
        logAction("Opponent is thinking...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            try {
                // Check if they should use a powerup (simple logic: use if they have 500+ energy)
                if (game.getOpponent().getEnergy() >= 500) {
                    game.usePowerup();
                    logAction("Opponent used a powerup!");
                }
                // Execute the opponent's roll and move
                game.playTurn(); 
                logAction("Opponent finished their turn.");
                
            } catch (Exception e) {
                logAction("Opponent encountered an issue.");
            }
            
            updateBoard();
            checkWinState();
        });
        pause.play();
    }
    
    
    
    
    
}