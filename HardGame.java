package com.example.spacesooterproject1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.util.*;

public class HardGame extends Application {

    private static final String URL = "jdbc:mysql://localhost:3306/space_shooter_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    List<Integer> leaderboard = new ArrayList<>();

    class Bullet {
        double x, y, width, height;
        int type;
        int damage;

        Bullet(double x, double y, double width, double height, int type, int damage) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.damage = damage;
        }
    }

    class EnemyBullet {
        double x, y;
        double speed = 4;
        int damage = 100;

        EnemyBullet(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void update() {
            y += speed;
        }
    }

    class BossBullet {
        double x, y;
        double dx, dy;
        double size = 6;
        int damage = 175;

        BossBullet(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        void update() {
            x += dx;
            y += dy;
        }
    }

    class BossBigBullet {
        double x, y;
        double size = 50;
        int damage = 220;
        double speed = 3;

        BossBigBullet(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void update() {
            y += speed;
        }
    }

    class Enemy {
        double x, y, width = 30, height = 30;
        int hp, maxHp, type;
        double speed;
        boolean stopped = false;

        Enemy(double x, double y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
            switch (type) {
                case 0 -> {
                    hp = (int) (50 * 1.3);
                    speed = 0.7;
                    width = 40;
                    height = 55;
                }
                case 1 -> {
                    hp = (int) (150 * 1.3);
                    speed = 0.7;
                    width = 50;
                    height = 50;
                }
                case 2 -> {
                    hp = (int) (370 * 1.3);
                    speed = 0.4;
                    width = 50;
                    height = 50;
                }
                case 3 -> {
                    hp = 15000;
                    speed = 0.3;
                    width = 80;
                    height = 80;
                }
            }
            maxHp = hp;
        }
    }

    final int WIDTH = 500, HEIGHT = 600;
    final int PLAYER_WIDTH = 55, PLAYER_HEIGHT = 55;
    double playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
    int playerHP = 1000;

    Set<KeyCode> keys = new HashSet<>();
    List<Bullet> bullets = new ArrayList<>();
    List<Enemy> enemies = new ArrayList<>();
    List<EnemyBullet> enemyBullets = new ArrayList<>();
    List<BossBullet> bossBullets = new ArrayList<>();
    List<BossBigBullet> bossBigBullets = new ArrayList<>();
    List<double[]> stars = new ArrayList<>();

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

    int shotsFired = 0;

    String gameState = "PLAYING";

    Image playerImage, redEnemyImage, blueEnemyImage, greenEnemyImage, bossImage;

    long lastShotTime = 0;
    long lastBossShotTime = 0;
    int smallShotCount = 0;
    int bossZigzagDir = 1;
    @Override
    public void start(Stage stagePrimary) {
        loadLeaderboard();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        loadImages();

        for (int i = 0; i < 100; i++) {
            stars.add(new double[]{Math.random() * WIDTH, Math.random() * HEIGHT});
        }

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
            if (code == KeyCode.DIGIT4 && stage >= 4) selectedWeapon = 4;

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
                    enemyBullets.clear();
                    bossBullets.clear();
                    bossBigBullets.clear();
                    frameCount = 0;
                    playerHP = 1000;
                    bossAlive = false;
                    smallShotCount = 0;
                    if (stage == 4) paidForBoss = true;
                } else if (code == KeyCode.N) {
                    System.exit(0);
                }
            }
                if (gameState.equals("VICTORY") || gameState.equals("GAMEOVER")) {
                    if (code == KeyCode.ENTER) {
                        saveScore(score);
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

    void loadLeaderboard() {
        leaderboard.clear();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT score FROM leaderboard ORDER BY score DESC LIMIT 10")) {
            while (rs.next()) {
                leaderboard.add(rs.getInt("score"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void saveScore(int newScore) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO leaderboard (score) VALUES (?)")) {
            ps.setInt(1, newScore);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        int limit = switch (selectedWeapon) {
            case 1 -> 10;
            case 2 -> 6;
            case 3 -> 3;
            case 4 -> 5;
            default -> 0;
        };

        if (shotsFired >= limit && now - lastShotTime < 1000) return;
        if (now - lastShotTime < 150) return;

        if (shotsFired >= limit) shotsFired = 0;
        lastShotTime = now;
        shotsFired++;

        int damage = switch (selectedWeapon) {
            case 1 -> 50;
            case 2 -> 180   ;
            case 3 -> 230;
            case 4 -> 400;
            default -> 0;
        };

        if (selectedWeapon == 1) {
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 2, HEIGHT - 50, 4, 10, 1, damage));
        } else if (selectedWeapon == 2) {
            weapon2Counter++;
            if (weapon2Counter % 3 == 0) {
                bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 3, HEIGHT - 50, 6, 20, 3, damage));
            } else {
                bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 4, HEIGHT - 40, 8, 8, 2, damage));
            }
        } else if (selectedWeapon == 3) {
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 10, HEIGHT - 40, 4, 10, 4, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 + 6, HEIGHT - 40, 4, 10, 4, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 2, HEIGHT - 40, 4, 10, 4, damage));
        } else if (selectedWeapon == 4) {
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 2, HEIGHT - 45, 4, 12, 5, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 10, HEIGHT - 45, 4, 12, 5, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 + 6, HEIGHT - 45, 4, 12, 5, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - 16, HEIGHT - 45, 4, 12, 5, damage));
            bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 + 12, HEIGHT - 45, 4, 12, 5, damage));
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

        Iterator<EnemyBullet> itEB = enemyBullets.iterator();
        while (itEB.hasNext()) {
            EnemyBullet eb = itEB.next();
            eb.update();
            if (eb.y > HEIGHT) itEB.remove();
            else if (checkCollisionPlayer(eb.x, eb.y, 6, 15)) {
                playerHP -= eb.damage;
                itEB.remove();
                if (playerHP <= 0) gameState = "GAMEOVER";
            }
        }

        Iterator<BossBullet> itBB = bossBullets.iterator();
        while (itBB.hasNext()) {
            BossBullet bb = itBB.next();
            bb.update();
            if (bb.y > HEIGHT || bb.x < 0 || bb.x > WIDTH) itBB.remove();
            else if (checkCollisionPlayer(bb.x, bb.y, bb.size, bb.size)) {
                playerHP -= bb.damage;
                itBB.remove();
                if (playerHP <= 0) gameState = "GAMEOVER";
            }
        }

        Iterator<BossBigBullet> itBBB = bossBigBullets.iterator();
        while (itBBB.hasNext()) {
            BossBigBullet bbb = itBBB.next();
            bbb.update();
            if (bbb.y > HEIGHT) itBBB.remove();
            else if (checkCollisionPlayer(bbb.x, bbb.y, bbb.size, bbb.size)) {
                playerHP -= bbb.damage;
                itBBB.remove();
                if (playerHP <= 0) gameState = "GAMEOVER";
            }
        }

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
                if (b.x < e.x + e.width && b.x + b.width > e.x &&
                        b.y < e.y + e.height && b.y + b.height > e.y) {
                    e.hp -= b.damage;
                    itB.remove();
                    if (e.hp <= 0) {
                        if (e.type == 0) score += 10;
                        else if (e.type == 1) score += 25;
                        else if (e.type == 2) score += 50;
                        else if (e.type == 3){
                            score += 1500;
                            gameState = "VICTORY";
                        }
                        itE.remove();
                        if (e.type == 3) bossAlive = false;
                    }
                    break;
                }
            }
        }

        for (Enemy e : enemies) {
            if (e.type == 3) {
                long now = System.currentTimeMillis();
                if (e.stopped) {
                    if (smallShotCount < 5) {
                        if (now - lastBossShotTime >= 500) {
                            lastBossShotTime = now;
                            smallShotCount++;
                            double cx = e.x + e.width / 2;
                            double cy = e.y + e.height;
                            bossBullets.add(new BossBullet(cx - 3, cy, 0, 5));
                            bossBullets.add(new BossBullet(cx - 3, cy, 3.5, 3.5));
                            bossBullets.add(new BossBullet(cx - 3, cy, -3.5, 3.5));
                        }
                    } else {
                        if (now - lastBossShotTime >= 1000) {
                            lastBossShotTime = now;
                            smallShotCount = 0;
                            bossBigBullets.add(new BossBigBullet(e.x + e.width / 2 - 25, e.y + e.height));
                        }
                    }
                }

                if (frameCount % 60 == 0) e.stopped = !e.stopped;

                if (!e.stopped) {
                    e.x += bossZigzagDir * 1.5;
                    if (e.x <= 0 || e.x + e.width >= WIDTH) bossZigzagDir *= -1;
                    e.y += 0.2;
                }

                if (e.y + e.height >= HEIGHT ||
                        (playerX < e.x + e.width && playerX + PLAYER_WIDTH > e.x &&
                                HEIGHT - 50 < e.y + e.height && HEIGHT - 50 + PLAYER_HEIGHT > e.y)) {
                    gameState = "GAMEOVER";
                }
            }
        }

        int frontCounter = 0;
        for (Enemy e : enemies) {
            if (e.type != 3 && isFrontEnemy(e)) {
                frontCounter++;
                if (frontCounter % 2 == 0 && frameCount % 90 == 0) {
                    enemyBullets.add(new EnemyBullet(e.x + e.width / 2, e.y + e.height));
                }
            }
        }
    }
    boolean isFrontEnemy(Enemy e) {
        for (Enemy other : enemies) {
            if (other == e || other.type == 3) continue;
            if (Math.abs(other.x - e.x) < e.width &&
                    other.y > e.y && Math.abs(other.y - e.y) < e.height * 1.5) {
                return false;
            }
        }
        return true;
    }

    boolean checkCollisionPlayer(double x, double y, double w, double h) {
        double playerY = HEIGHT - 50;
        return x < playerX + PLAYER_WIDTH && x + w > playerX &&
                y < playerY + PLAYER_HEIGHT && y + h > playerY;
    }

    void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.WHITE);
        for (double[] star : stars) {
            gc.fillRect(star[0], star[1], 2, 2);
        }

        gc.drawImage(playerImage, playerX, HEIGHT - 50, PLAYER_WIDTH, PLAYER_HEIGHT);

        gc.setFill(Color.GRAY);
        gc.fillRect(10, 70, 100, 10);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(10, 70, playerHP * 100.0 / 1000, 10);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(10, 70, 100, 10);

        for (Bullet b : bullets) {
            gc.setFill(switch (b.type) {
                case 1 -> Color.YELLOW;
                case 2 -> Color.RED;
                case 3 -> Color.LIME;
                case 4 -> Color.CYAN;
                case 5 -> Color.PURPLE;
                default -> Color.WHITE;
            });
            gc.fillRect(b.x, b.y, b.width, b.height);
        }

        for (EnemyBullet eb : enemyBullets) {
            gc.setFill(Color.PURPLE);
            gc.fillOval(eb.x - 3, eb.y, 6, 15);
        }

        for (BossBullet bb : bossBullets) {
            gc.setFill(Color.ORANGE);
            gc.fillOval(bb.x, bb.y, bb.size, bb.size);
        }

        for (BossBigBullet bbb : bossBigBullets) {
            gc.setFill(Color.DARKORANGE);
            gc.fillOval(bbb.x, bbb.y, bbb.size, bbb.size);
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

        gc.setFill(Color.WHITE);
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
        gc.setFill(Color.WHITE);
        gc.fillText("Leaderboard:", WIDTH - 150, 20);
        int y = 40;
        for (int i = 0; i < leaderboard.size(); i++) {
            gc.fillText((i + 1) + ". " + leaderboard.get(i), WIDTH - 150, y);
            y += 20;
        }



    }



    public static void main(String[] args) {
        launch(args);
    }
}
