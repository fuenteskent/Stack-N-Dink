package com.stackanddink.desktop;

import com.stackanddink.core.OpenPlaySession;
import com.stackanddink.core.model.Player;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StackAndDinkApp extends Application {

    private final OpenPlaySession session = new OpenPlaySession();

    @Override
    public void start(Stage stage) {

        Label title = new Label("Stack-N-Dink");
        title.setStyle(
            "-fx-font-size: 28px; -fx-font-weight: bold;"
        );

        Label subtitle = new Label(
            "Offline Pickleball Court Management"
        );

        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter player name");

        Button addButton = new Button("Add Player");

        ListView<String> playerList = new ListView<>();

        Label playerCount = new Label(
            "Registered Players: 0"
        );

        
        Label courtCount = new Label("Courts: 0");

        Button addCourtButton = new Button("Add Court");
        Button removeCourtButton = new Button("Remove Selected Court");

        ListView<String> courtList = new ListView<>();

        Runnable refreshCourts = () -> {
            courtList.getItems().setAll(
                session.getCourts()
                    .stream()
                    .map(c -> "Court " + c.number() + " - Available")
                    .toList()
            );

            courtCount.setText(
                "Courts: " + session.getCourts().size()
            );
        };

        addCourtButton.setOnAction(e -> {
            session.addCourt();
            refreshCourts.run();
        });

        removeCourtButton.setOnAction(e -> {
            int selected = courtList.getSelectionModel()
                                    .getSelectedIndex();

            if (selected >= 0) {
                int courtNumber = session.getCourts()
                                        .get(selected)
                                        .number();

                session.removeCourt(courtNumber);
                refreshCourts.run();
            }
        });


        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Runnable addPlayerAction = () -> {
            try {
                session.addPlayer(nameInput.getText());

                playerList.getItems().setAll(
                    session.getPlayers()
                           .stream()
                           .map(Player::name)
                           .toList()
                );

                playerCount.setText(
                    "Registered Players: "
                    + session.getPlayers().size()
                );

                nameInput.clear();
                errorLabel.setText("");
                nameInput.requestFocus();

            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        };

        addButton.setOnAction(e -> addPlayerAction.run());
        nameInput.setOnAction(e -> addPlayerAction.run());

        HBox inputRow = new HBox(
            10, nameInput, addButton
        );

        HBox.setHgrow(nameInput, Priority.ALWAYS);

        
        VBox root = new VBox(
            15,
            title,
            subtitle,
            inputRow,
            errorLabel,
            playerCount,
            playerList,
            courtCount,
            addCourtButton,
            removeCourtButton,
            courtList
        );

        root.setPadding(new Insets(25));

        Scene scene = new Scene(root, 750, 700);

        stage.setTitle("Stack-N-Dink v0.1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
