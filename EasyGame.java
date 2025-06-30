package com.example.spacesooterproject1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.stage.Stage;

import java.util.*;

public class EasyGame extends Application {

    class Bullet {
        double x, y, width, height;
        int type;

        Bullet(double x, double y, double width, double height, int type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }
    }

    class Enemy {
        double x, y, width = 30, height = 30;
        int hp, maxHp, type;
        double speed;

        Enemy(double x, double y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
            switch (type) {
                case 0 -> { hp = 50; speed = 0.7; width = 40; height = 55; }
                case 1 -> { hp = 150; speed = 0.5; width = 50; height = 50; }
                case 2 -> { hp = 370; speed = 0.3; width = 50; height = 50; }
                case 3 -> { hp = 15000; speed = 0.3; width = 80; height = 80; }
            }
            maxHp = hp;
        }
    }

    final int WIDTH = 500, HEIGHT = 600;
    final int PLAYER_WIDTH = 55, PLAYER_HEIGHT = 55;
    double playerX = WIDTH / 2 - PLAYER_WIDTH / 2;

    Set<KeyCode> keys = new HashSet<>();
    List<Bullet> bullets = new ArrayList<>();
    List<Enemy> enemies = new ArrayList<>();

    int stage = 1;
    int frameCount = 0;
    boolean bossAlive = false;

    int waveCooldown = 0;
    int waveDelayFrames = 240;
    boolean spawningWave = true;
    int waveIntervalCounter = 0;
    int waveIntervalFrames = 120;

    int score = 0;
    int scoreBeforeStage2 = 0;
    boolean paidForBoss = false;

    int redSpawned = 0, blueSpawned = 0, greenSpawned = 0;

    int selectedWeapon = 1;
    int weapon2Counter = 0;

    String gameState = "PLAYING";

    Image playerImage, redEnemyImage, blueEnemyImage, greenEnemyImage, bossImage;

    long lastShotTime = 0;

    class Star {
        double x, y, radius;

        Star(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    List<Star> stars = new ArrayList<>();

    @Override
    public void start(Stage stagePrimary) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        generateStars();

        loadImages();

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            keys.add(code);
            if (code == KeyCode.Q) {
                stagePrimary.close();
                new HelloApplication().start(new Stage());
            }
            if (code == KeyCode.P) gameState = "PAUSED";
            if (code == KeyCode.C && gameState.equals("PAUSED")) gameState = "PLAYING";

            if (code == KeyCode.DIGIT1) selectedWeapon = 1;
            if (code == KeyCode.DIGIT2 && stage >= 2) selectedWeapon = 2;
            if (code == KeyCode.DIGIT3 && stage >= 3) selectedWeapon = 3;

            if (code == KeyCode.SPACE) shoot();

            if (gameState.equals("AWAITING_CONTINUE")) {
                if (code == KeyCode.Y) {
                    if (stage == 1 && score < 120 ||
                            stage == 2 && (score - scoreBeforeStage2) < 500 ||
                            stage == 3 && score < 1000) {
                        gameState = "GAMEOVER";
                        return;
                    }
                    if (stage == 3) score -= 1000;
                    if (stage == 2) scoreBeforeStage2 = score;

                    gameState = "PLAYING";
                    stage++;
                    enemies.clear();
                    bullets.clear();
                    frameCount = 0;
                    if (stage == 4) paidForBoss = true;
                } else if (code == KeyCode.N) {
                    System.exit(0);
                }
            }
        });
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;
                if (gameState.equals("PLAYING")) updateGameLogic();
                render(gc);
            }
        }.start();

        stagePrimary.setTitle("Space Shooter FX");
        stagePrimary.setScene(scene);
        stagePrimary.show();
    }

    void generateStars() {
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            double x = rand.nextDouble() * WIDTH;
            double y = rand.nextDouble() * HEIGHT;
            double radius = rand.nextDouble() * 2 + 0.5;
            stars.add(new Star(x, y, radius));
        }
    }

    void loadImages() {
        playerImage = new Image(getClass().getResourceAsStream("/player.png"));
        redEnemyImage = new Image(getClass().getResourceAsStream("/red_enemy.png"));
        blueEnemyImage = new Image(getClass().getResourceAsStream("/blue_enemy.png"));
        greenEnemyImage = new Image(getClass().getResourceAsStream("/green_enemy.png"));
        bossImage = new Image(getClass().getResourceAsStream("/boss.png"));
    }

    void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < 150) return;
        lastShotTime = now;

        if (selectedWeapon == 1) {
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 2, HEIGHT - 50, 4, 10, 1));
        } else if (selectedWeapon == 2) {
            weapon2Counter++;
            if (weapon2Counter % 3 == 0) {
                bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 3, HEIGHT - 50, 6, 20, 3));
            } else {
                bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 4, HEIGHT - 40, 8, 8, 2));
            }
        } else if (selectedWeapon == 3) {
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 10, HEIGHT - 40, 4, 10, 4));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 + 6, HEIGHT - 40, 4, 10, 4));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 2, HEIGHT - 40, 4, 10, 4));
        }
    }

    void spawnHorizonWave(int count, int type) {
        double spacing = WIDTH / (count + 1);
        for (int i = 0; i < count; i++) {
            double x = spacing * (i + 1) - 20;
            enemies.add(new Enemy(x, -50, type));
        }
    }

    void updateGameLogic() {
        if (keys.contains(KeyCode.LEFT)) playerX -= 5;
        if (keys.contains(KeyCode.RIGHT)) playerX += 5;
        playerX = Math.max(0, Math.min(WIDTH - PLAYER_WIDTH, playerX));

        bullets.removeIf(b -> (b.y -= 10) < 0);

        if (stage < 4) {
            if (spawningWave) {
                waveIntervalCounter++;
                if (waveIntervalCounter >= waveIntervalFrames) {
                    waveIntervalCounter = 0;
                    int type = switch (stage) {
                        case 1 -> 0;
                        case 2 -> 1;
                        case 3 -> 2;
                        default -> 0;
                    };
                    boolean canSpawn = switch (stage) {
                        case 1 -> redSpawned < 30;
                        case 2 -> blueSpawned < 50;
                        case 3 -> greenSpawned < 50;
                        default -> false;
                    };
                    if (canSpawn) {
                        int amount = 6 + new Random().nextInt(3);
                        spawnHorizonWave(amount, type);
                        if (stage == 1) redSpawned += amount;
                        else if (stage == 2) blueSpawned += amount;
                        else if (stage == 3) greenSpawned += amount;
                    } else {
                        spawningWave = false;
                        waveCooldown = 0;
                    }
                }
            } else {
                waveCooldown++;
                if (waveCooldown > waveDelayFrames && enemies.isEmpty()) {
                    boolean completedAll = (stage == 1 && redSpawned >= 30) ||
                            (stage == 2 && blueSpawned >= 50) ||
                            (stage == 3 && greenSpawned >= 50);
                    if (completedAll) gameState = "AWAITING_CONTINUE";
                    else {
                        spawningWave = true;
                        waveIntervalCounter = 0;
                    }
                }
            }
        } else if (!bossAlive && paidForBoss) {
            enemies.add(new Enemy(WIDTH / 2 - 40, 0, 3));
            bossAlive = true;
        }

        enemies.removeIf(e -> (e.y += e.speed) > HEIGHT && e.type != 3);

        Iterator<Bullet> itB = bullets.iterator();
        while (itB.hasNext()) {
            Bullet b = itB.next();
            Iterator<Enemy> itE = enemies.iterator();
            while (itE.hasNext()) {
                Enemy e = itE.next();
                if (b.x < e.x + e.width && b.x + b.width > e.x && b.y < e.y + e.height && b.y + b.height > e.y) {
                    int damage = switch (b.type) {
                        case 1 -> 30;
                        case 2, 3 -> 65;
                        case 4 -> 180;
                        default -> 0;
                    };
                    e.hp -= damage;
                    itB.remove();
                    if (e.hp <= 0) {
                        if (e.type == 0) score += 10;
                        else if (e.type == 1) score += 25;
                        else if (e.type == 2) score += 50;
                        else if (e.type == 3) {
                            score += 500;
                            gameState = "VICTORY";
                        }
                        itE.remove();
                    }
                    break;
                }
            }
        }
    }

    void renderSun(GraphicsContext gc) {
        double sunRadius = 150;
        double sunCenterX = sunRadius * 0.17;
        double sunCenterY = HEIGHT * 0.6;

        RadialGradient gradient = new RadialGradient(
                0, 0,
                sunCenterX, sunCenterY,
                sunRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fff866")),
                new Stop(0.5, Color.web("#ffdb4d")),
                new Stop(1, Color.web("#ff9900", 0))
        );

        gc.setFill(gradient);
        gc.fillOval(sunCenterX - sunRadius, sunCenterY - sunRadius, sunRadius * 2, sunRadius * 2);
    }

    void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        renderSun(gc);

        gc.setFill(Color.WHITE);
        for (Star star : stars) {
            gc.fillOval(star.x, star.y, star.radius, star.radius);
        }

        gc.drawImage(playerImage, playerX, HEIGHT - 50, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (Bullet b : bullets) {
            gc.setFill(switch (b.type) {
                case 1 -> Color.YELLOW;
                case 2 -> Color.RED;
                case 3 -> Color.LIME;
                case 4 -> Color.CYAN;
                default -> Color.WHITE;
            });
            gc.fillRect(b.x, b.y, b.width, b.height);
        }

        for (Enemy e : enemies) {
            Image img = switch (e.type) {
                case 0 -> redEnemyImage;
                case 1 -> blueEnemyImage;
                case 2 -> greenEnemyImage;
                case 3 -> bossImage;
                default -> redEnemyImage;
            };
            if (e.type != 3) {
                gc.save();
                gc.translate(e.x + e.width / 2, e.y + e.height / 2);
                gc.rotate(180);
                gc.drawImage(img, -e.width / 2, -e.height / 2, e.width, e.height);
                gc.restore();
            } else {
                gc.drawImage(img, e.x, e.y, e.width, e.height);
            }

            if (e.hp < e.maxHp) {
                gc.setStroke(Color.WHITE);
                gc.strokeRect(e.x, e.y - 6, e.width, 4);
                gc.setFill(Color.MAGENTA);
                gc.fillRect(e.x, e.y - 6, e.width * (e.hp / (double) e.maxHp), 4);
            }
        }

        gc.setFill(Color.LIGHTGREEN);
        gc.fillText("Stage: " + stage, 10, 20);
        gc.fillText("Score: " + score, 10, 40);
        gc.fillText("Weapon: " + selectedWeapon, 10, 60);

        if (gameState.equals("AWAITING_CONTINUE"))
            gc.fillText("Continue playing? (Y/N)", WIDTH / 2.0 - 70, HEIGHT / 2.0);

        if (gameState.equals("VICTORY")) {
            gc.setFill(Color.GOLD);
            gc.fillText("You win!", WIDTH / 2.0 - 30, HEIGHT / 2.0);
        }

        if (gameState.equals("GAMEOVER")) {
            gc.setFill(Color.RED);
            gc.fillText("Game Over!", WIDTH / 2.0 - 40, HEIGHT / 2.0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
