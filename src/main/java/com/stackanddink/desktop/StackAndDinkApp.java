package com.stackanddink.desktop;

import com.stackanddink.core.OpenPlaySession;
import com.stackanddink.core.model.Court;
import com.stackanddink.core.model.Player;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Stack-N-Dink UI milestone: visual court cards and a paddle rack.
 * Existing OpenPlaySession remains the source of truth for players/courts.
 * Court assignments, stacking, and drag-and-drop are intentionally NOT
 * implemented in this visual milestone.
 */
public class StackAndDinkApp extends Application {

    // Court card and paddle dimensions are deliberately fixed.
    private static final double COURT_CARD_WIDTH = 688;
    private static final double COURT_CARD_HEIGHT = 460;
    private static final double FIELD_WIDTH = 590;
    private static final double FIELD_HEIGHT = 292;

    private final OpenPlaySession session = new OpenPlaySession();
    private final VBox courtsColumn = new VBox(24);
    private final HBox waitingRow = new HBox(12);
    private final Label waitingCount = new Label();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        VBox top = buildHeader();
        root.setTop(top);

        courtsColumn.setAlignment(Pos.TOP_CENTER);
        courtsColumn.setPadding(new Insets(16, 12, 24, 12));
        courtsColumn.getStyleClass().add("courts-column");

        ScrollPane courtsScroll = new ScrollPane(courtsColumn);
        courtsScroll.getStyleClass().add("courts-scroll");
        courtsScroll.setFitToWidth(true);
        courtsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        courtsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        courtsScroll.setPannable(true);
        root.setCenter(courtsScroll);

        root.setBottom(buildQueuePanel());
        session.addCourt(); // Show Court 1 when a new session opens.
        refreshCourts();
        refreshQueue();

        Scene scene = new Scene(root, 1000, 870);
        URL stylesheet = getClass().getResource("/com/stackanddink/desktop/app.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        } else {
            System.err.println("Warning: CSS not found in src/main/resources.");
        }

        stage.setTitle("Stack-N-Dink | Digital Paddle Rack");
        stage.setMinWidth(820);
        stage.setMinHeight(690);
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildHeader() {
        Label title = new Label("Stack-N-Dink");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("your courts, your rotation.");
        subtitle.getStyleClass().add("app-subtitle");
        VBox brand = new VBox(0, title, subtitle);

        Button history = iconButton("↶", "Match history (coming later)");
        history.setDisable(true);
        Button audio = iconButton("♫", "Offline announcements (coming later)");
        audio.setDisable(true);
        Button settings = iconButton("⚙", "About this version");
        settings.setOnAction(e -> showInfo("Stack-N-Dink", 
            "UI preview milestone. Player registration and court creation work now. " +
            "Paddle movement, stacking modes, and match results come next."));

        HBox actions = new HBox(10, history, audio, settings);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(brand, Priority.ALWAYS);
        HBox heading = new HBox(20, brand, actions);
        heading.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> modes = new ComboBox<>(FXCollections.observableArrayList(
            "Win-Win-Lose-Lose", "FIFO", "Winner Stays", "Rotating Partners",
            "Skill-Based", "Round Robin", "Tournament"
        ));
        modes.getSelectionModel().selectFirst();
        modes.getStyleClass().add("mode-picker");
        modes.setMaxWidth(Double.MAX_VALUE);
        modes.setTooltip(new Tooltip(
            "UI preview only: choosing a mode does not run its algorithm yet."
        ));
        Label preview = new Label("MODE SELECTOR — VISUAL PREVIEW ONLY");
        preview.getStyleClass().add("helper-note");

        VBox top = new VBox(14, heading, modes, preview);
        top.getStyleClass().add("app-header");
        return top;
    }

    private VBox buildQueuePanel() {
        waitingCount.getStyleClass().add("section-heading");
        Button addPlayer = new Button("+ Add Player");
        addPlayer.getStyleClass().addAll("outline-button", "add-player-button");
        addPlayer.setOnAction(e -> promptAddPlayer());
        HBox header = new HBox(20, waitingCount, new Pane(), addPlayer);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        waitingRow.setAlignment(Pos.TOP_LEFT);
        waitingRow.setPadding(new Insets(12, 12, 10, 10));
        ScrollPane queueScroll = new ScrollPane(waitingRow);
        queueScroll.getStyleClass().add("queue-scroll");
        queueScroll.setFitToHeight(true);
        queueScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        queueScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        queueScroll.setPannable(true);
        queueScroll.setPrefHeight(166);
        queueScroll.setMinHeight(166);
        queueScroll.setMaxHeight(166);

        VBox queuePanel = new VBox(8, header, queueScroll);
        queuePanel.setPadding(new Insets(18, 26, 10, 26));
        queuePanel.getStyleClass().add("queue-panel");
        return queuePanel;
    }

    private void promptAddPlayer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Player | Stack-N-Dink");
        dialog.setHeaderText("Register a player");
        dialog.setContentText("Player name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                session.addPlayer(name);
                refreshQueue();
            } catch (IllegalArgumentException ex) {
                showInfo("Player not added", ex.getMessage());
            }
        });
    }

    private void refreshQueue() {
        List<Player> players = session.getPlayers();
        waitingCount.setText("WAITING QUEUE (" + players.size() + ")");
        waitingRow.getChildren().clear();
        for (int i = 0; i < players.size(); i++) {
            waitingRow.getChildren().add(PaddleView.playerTile(players.get(i), i));
        }
        if (players.isEmpty()) {
            Label empty = new Label("No players yet. Choose + Add Player to begin.");
            empty.getStyleClass().add("empty-queue-message");
            waitingRow.getChildren().add(empty);
        }
    }

    private void refreshCourts() {
        courtsColumn.getChildren().clear();
        for (Court court : session.getCourts()) {
            courtsColumn.getChildren().add(buildCourtCard(court));
        }

        Button addCourt = new Button("＋   Add Court");
        addCourt.getStyleClass().add("add-court-button");
        addCourt.setMinSize(COURT_CARD_WIDTH, 76);
        addCourt.setPrefSize(COURT_CARD_WIDTH, 76);
        addCourt.setMaxSize(COURT_CARD_WIDTH, 76);
        addCourt.setOnAction(e -> {
            session.addCourt();
            refreshCourts();
        });
        courtsColumn.getChildren().add(addCourt);

        if (session.getCourts().isEmpty()) {
            Label hint = new Label("Create your first court to get started.");
            hint.getStyleClass().add("helper-note");
            courtsColumn.getChildren().add(0, hint);
        }
    }

    private VBox buildCourtCard(Court court) {
        Label name = new Label("COURT " + court.number());
        name.getStyleClass().add("court-title");
        Label available = new Label("AVAILABLE");
        available.getStyleClass().add("court-status");

        Button remove = iconButton("×", "Remove Court " + court.number());
        remove.getStyleClass().add("remove-court");
        remove.setOnAction(e -> {
            Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Remove Court " + court.number() + " from this session?",
                ButtonType.CANCEL, ButtonType.OK
            );
            confirmation.setHeaderText("Remove Court " + court.number());
            Optional<ButtonType> choice = confirmation.showAndWait();
            if (choice.isPresent() && choice.get() == ButtonType.OK) {
                session.removeCourt(court.number());
                refreshCourts();
            }
        });

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox cardHeader = new HBox(12, name, spacer, available, remove);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Pane field = buildCourtField();
        Label footer = new Label("Drag player paddles onto the court — coming next");
        footer.getStyleClass().add("court-footnote");
        StackPane foot = new StackPane(footer);
        foot.getStyleClass().add("court-footer");
        foot.setPrefHeight(42);
        foot.setMaxHeight(42);

        VBox card = new VBox(15, cardHeader, field, foot);
        card.getStyleClass().add("court-card");
        card.setPadding(new Insets(20, 24, 18, 24));
        card.setMinSize(COURT_CARD_WIDTH, COURT_CARD_HEIGHT);
        card.setPrefSize(COURT_CARD_WIDTH, COURT_CARD_HEIGHT);
        card.setMaxSize(COURT_CARD_WIDTH, COURT_CARD_HEIGHT);
        card.setAlignment(Pos.TOP_CENTER);
        return card;
    }

    private Pane buildCourtField() {
        Pane field = new Pane();
        field.getStyleClass().add("court-field");
        field.setMinSize(FIELD_WIDTH, FIELD_HEIGHT);
        field.setPrefSize(FIELD_WIDTH, FIELD_HEIGHT);
        field.setMaxSize(FIELD_WIDTH, FIELD_HEIGHT);

        // A doubles court from the top: white lines, central net, four empty slots.
        Rectangle courtOutline = new Rectangle(24, 20, 542, 250);
        courtOutline.getStyleClass().add("court-outline");
        Line middle = new Line(295, 20, 295, 270);
        middle.getStyleClass().add("court-line");
        Line netCenter = new Line(24, 145, 566, 145);
        netCenter.getStyleClass().add("court-line");

        field.getChildren().addAll(courtOutline, middle, netCenter);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                Pane ghost = PaddleView.emptySlotArt();
                ghost.relocate(col == 0 ? 137 : 390, row == 0 ? 23 : 162);
                field.getChildren().add(ghost);
            }
        }

        // Decorative net grid across center of court.
        Rectangle netBg = new Rectangle(16, 135, 558, 22);
        netBg.getStyleClass().add("net-background");
        field.getChildren().add(netBg);
        for (int x = 18; x <= 572; x += 12) {
            Line thread = new Line(x, 135, x, 157);
            thread.getStyleClass().add("net-thread");
            field.getChildren().add(thread);
        }
        for (int y = 140; y <= 153; y += 7) {
            Line thread = new Line(16, y, 574, y);
            thread.getStyleClass().add("net-thread");
            field.getChildren().add(thread);
        }
        Rectangle leftPost = new Rectangle(10, 131, 8, 30);
        Rectangle rightPost = new Rectangle(572, 131, 8, 30);
        leftPost.getStyleClass().add("net-post");
        rightPost.getStyleClass().add("net-post");
        field.getChildren().addAll(leftPost, rightPost);

        return field;
    }

    private Button iconButton(String symbol, String tooltip) {
        Button button = new Button(symbol);
        button.getStyleClass().add("icon-button");
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
