package com.javarush.games.minesweeper;

import com.javarush.engine.cell.Color;
import com.javarush.engine.cell.Game;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperGame extends Game {
    private static final int SIDE = 9;
    private static final String MINE = "\uD83D\uDCA3";
    private static final String FLAG = "\uD83D\uDEA9";

    private GameObject[][] gameField = new GameObject[SIDE][SIDE];
    private int countMinesOnField = 0;
    private int countFlags = 0;
    private int countClosedTiles = SIDE * SIDE;

    private boolean isGameStopped;

    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        createGame();
    }

    @Override
    public void onMouseLeftClick(int x, int y) {
        openTile(x, y);
    }

    @Override
    public void onMouseRightClick(int x, int y) {
        markTile(x, y);
    }

    private void createGame() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                boolean isMine = getRandomNumber(10) < 1;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
                setCellColor(x, y, Color.ORANGE);
            }
        }
        countFlags = countMinesOnField;

        countMineNeighbors();

        isGameStopped = false;
    }

    private List<GameObject> getNeighbors(GameObject gameObject) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void countMineNeighbors() {
        for (int x = 0; x < SIDE; x++)  {
            for (int y = 0; y < SIDE; y++) {
                GameObject object = gameField[y][x];
                if (!object.isMine) {
                    List<GameObject> gameObjectList = getNeighbors(object); // получили список соседей
                    for (GameObject item : gameObjectList) {
                        if (item.isMine) object.countMineNeighbors++;
                    }
                }
            }
        }
    }

    private void openTile(int x, int y) {
        GameObject gameObject = gameField[y][x];
        if (!isGameStopped && !gameObject.isOpen) {
            if (!gameObject.isFlag) {
                gameObject.isOpen = true;
                setCellColor(x, y, Color.GREEN);

                if (gameObject.isMine) {
                    setCellValueEx(x, y, Color.RED, MINE);
                    gameOver();
                } else {
                    setCellNumber(x, y, gameObject.countMineNeighbors);
                    countClosedTiles--;
                    if (countClosedTiles == countMinesOnField) {
                        win();
                    } else if (gameObject.countMineNeighbors == 0) {
                        List<GameObject> neighborsList = getNeighbors(gameObject);
                        setCellValue(gameObject.x, gameObject.y, "");
                        for (GameObject item : neighborsList) {
                            if (!item.isOpen) {
                                item.isOpen = true;
                                openTile(item.x, item.y);
                            }
                        }
                    } else {
                        setCellNumber(x, y, gameObject.countMineNeighbors);
                    }
                }
            }
        }
    }

    private void markTile(int x, int y) {
        GameObject gameObject = gameField[y][x];

        //если ячейка еще не открыта, то ее можно пометить флагом
        //если в ячейке уже не стоит флаг
        //если количество доступных флагов больше 0
        if (!isGameStopped) {
            if (!gameObject.isOpen && !gameObject.isFlag && countFlags != 0) {
                gameObject.isFlag = true;
                countFlags--;
                setCellValue(x, y, FLAG);
                setCellColor(x, y, Color.YELLOW);
            } else if (gameObject.isFlag) {
                gameObject.isFlag = false;
                countFlags++;
                setCellValue(x, y, "");
                setCellColor(x, y, Color.ORANGE);
            }
        }
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.RED, "Вы проиграли!", Color.WHITE, 18);
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(Color.GREEN, "Вы выиграли!", Color.GREEN, 18);
    }
}