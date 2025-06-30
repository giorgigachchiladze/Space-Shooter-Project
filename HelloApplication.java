package com.example.spacesooterproject1;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Random;

public class HelloApplication extends Application {
    private static final String URL = "jdbc:mysql://localhost:3306/space_shooter_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    @Override
    public void start(Stage primaryStage) {
        showLogin(primaryStage);
    }

    private void showLogin(Stage primaryStage) {
        StackPane root = new StackPane();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        createStarryBackground(gc, WIDTH, HEIGHT);

        VBox loginBox = new VBox(15);
        loginBox.setMaxWidth(400);
        loginBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 40; -fx-background-radius: 15;");
        loginBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("LOG IN");
        title.setFont(Font.font(30));
        title.setTextFill(Color.LIGHTGREEN);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleTextField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleTextField(passwordField);

        Button loginBtn = new Button("Log In");
        styleButton(loginBtn);

        Button signUpBtn = new Button("Sign Up");
        styleButton(signUpBtn);

        HBox buttonsBox = new HBox(20, loginBtn, signUpBtn);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        loginBox.getChildren().addAll(title, emailField, passwordField, buttonsBox);

        root.getChildren().addAll(canvas, loginBox);

        loginBtn.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {
                ps.setString(1, emailField.getText());
                ps.setString(2, passwordField.getText());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    showMainMenu(primaryStage);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Invalid email or password").showAndWait();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        signUpBtn.setOnAction(e -> showSignUpScreen(primaryStage));

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setTitle("Space Shooter - Login");
        primaryStage.show();
    }

    private void showSignUpScreen(Stage primaryStage) {
        StackPane root = new StackPane();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        createStarryBackground(gc, WIDTH, HEIGHT);

        VBox signUpBox = new VBox(15);
        signUpBox.setMaxWidth(400);
        signUpBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 40; -fx-background-radius: 15;");
        signUpBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("SIGN UP");
        title.setFont(Font.font(30));
        title.setTextFill(Color.LIGHTGREEN);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleTextField(emailField);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        styleTextField(nameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleTextField(passwordField);

        Button registerBtn = new Button("Register");
        styleButton(registerBtn);

        Button backBtn = new Button("Back to Login");
        styleButton(backBtn);

        HBox buttonsBox = new HBox(20, registerBtn, backBtn);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        signUpBox.getChildren().addAll(title, emailField, nameField, passwordField, buttonsBox);

        root.getChildren().addAll(canvas, signUpBox);

        registerBtn.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (email, name, password) VALUES (?, ?, ?)")) {
                ps.setString(1, emailField.getText());
                ps.setString(2, nameField.getText());
                ps.setString(3, passwordField.getText());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Registration successful").showAndWait();
                showLogin(primaryStage);
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Error registering user").showAndWait();
                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> showLogin(primaryStage));

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setTitle("Space Shooter - Sign Up");
        primaryStage.show();
    }

    private void showMainMenu(Stage primaryStage) {
        StackPane root = new StackPane();

        Canvas canvas = new Canvas(500, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        createStarryBackground(gc, 500, 600);

        VBox menuBox = new VBox(20);
        menuBox.setMaxWidth(400);
        menuBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 40; -fx-background-radius: 15;");
        menuBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("SPACE SHOOTER");
        title.setFont(Font.font(30));
        title.setTextFill(Color.LIGHTGREEN);

        Button playButton = new Button("PLAY");
        styleButton(playButton);

        Button leaderboardButton = new Button("LEADERBOARD");
        styleButton(leaderboardButton);

        menuBox.getChildren().addAll(title, playButton, leaderboardButton);

        root.getChildren().addAll(canvas, menuBox);

        playButton.setOnAction(e -> showDifficultyScreen(primaryStage));
        leaderboardButton.setOnAction(e -> showLeaderboard(primaryStage));

        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.setTitle("Space Shooter Menu");
        primaryStage.show();
    }

    // Updated difficulty screen with consistent style
    private void showDifficultyScreen(Stage primaryStage) {
        StackPane root = new StackPane();

        Canvas canvas = new Canvas(500, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        createStarryBackground(gc, 500, 600);

        VBox difficultyBox = new VBox(25);
        difficultyBox.setMaxWidth(400);
        difficultyBox.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-padding: 40; -fx-background-radius: 15;");
        difficultyBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("SELECT DIFFICULTY");
        title.setFont(Font.font(30));
        title.setTextFill(Color.LIGHTGREEN);

        Button easyBtn = new Button("EASY");
        Button mediumBtn = new Button("MEDIUM");
        Button hardBtn = new Button("HARD");

        styleButton(easyBtn);
        styleButton(mediumBtn);
        styleButton(hardBtn);

        difficultyBox.getChildren().addAll(title, easyBtn, mediumBtn, hardBtn);

        root.getChildren().addAll(canvas, difficultyBox);

        easyBtn.setOnAction(e -> {
            try {
                new EasyGame().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close();
        });

        mediumBtn.setOnAction(e -> {
            try {
                new MediumGame().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close();
        });

        hardBtn.setOnAction(e -> {
            try {
                new HardGame().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            primaryStage.close();
        });

        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.setTitle("Select Difficulty");
        primaryStage.show();
    }

    private void showLeaderboard(Stage primaryStage) {
        StackPane root = new StackPane();

        Canvas canvas = new Canvas(500, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        createStarryBackground(gc, 500, 600);

        VBox rootBox = new VBox(10);
        rootBox.setMaxWidth(400);
        rootBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 40; -fx-background-radius: 15;");
        rootBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("LEADERBOARD");
        title.setFont(Font.font(30));
        title.setTextFill(Color.GOLD);

        rootBox.getChildren().add(title);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT score, recorded_at FROM leaderboard ORDER BY score DESC LIMIT 10")) {

            while (rs.next()) {
                String playerName = rs.getString("player_name");
                int score = rs.getInt("score");

                HBox entryBox = new HBox();
                entryBox.setSpacing(20);
                entryBox.setPrefWidth(300);

                Label nameLabel = new Label(playerName);
                nameLabel.setTextFill(Color.WHITE);
                nameLabel.setPrefWidth(150);

                Label scoreLabel = new Label(String.valueOf(score));
                scoreLabel.setTextFill(Color.WHITE);
                scoreLabel.setPrefWidth(150);
                scoreLabel.setAlignment(Pos.CENTER_RIGHT);

                entryBox.getChildren().addAll(nameLabel, scoreLabel);
                rootBox.getChildren().add(entryBox);
            }
        } catch (SQLException e) {
            Label error = new Label("No scores yet or DB error.");
            error.setTextFill(Color.GRAY);
            rootBox.getChildren().add(error);
            e.printStackTrace();
        }

        Button backButton = new Button("BACK");
        styleButton(backButton);
        backButton.setOnAction(e -> showMainMenu(primaryStage));

        rootBox.getChildren().add(backButton);
        root.getChildren().addAll(canvas, rootBox);

        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.setTitle("Leaderboard");
        primaryStage.show();
    }

    private void styleTextField(TextField tf) {
        tf.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: gray; " +
                        "-fx-font-size: 16; " +
                        "-fx-border-color: lightgreen; " +
                        "-fx-border-radius: 5;"
        );
        tf.setPrefHeight(40);
    }

    private void styleButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: lightgreen; " +
                        "-fx-text-fill: black; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-pref-width: 140; " +
                        "-fx-pref-height: 40;"
        );
    }

    private void createStarryBackground(GraphicsContext gc, int width, int height) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        Random rand = new Random();

        gc.setFill(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            double x = rand.nextDouble() * width;
            double y = rand.nextDouble() * height;
            double radius = rand.nextDouble() * 2 + 0.5;
            gc.fillOval(x, y, radius, radius);
        }
    }

    public static void saveScore(String playerName, int score) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO leaderboard (player_name, score) VALUES (?, ?)")) {
            ps.setString(1, playerName);
            ps.setInt(2, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
