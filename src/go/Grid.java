package go;
/**
 * Provides game logic.
 *
 *
 */
/*
 *
 *
 */
public class Grid {

    private final int size;
    /**
     * [row][column]
     */
    public Stone[][] stones;
    public TerritoryMark[][] Marks;
    public int counter;
    public boolean isKo = false;
    public boolean createdKo = false;
    public boolean wasKo = false;
    public Stone[] ko = new Stone[2];
    private int c = 0;
    public int BlackScore = 0;
    public int WhiteScore = 0;

    public Grid(int size) {
        this.size = size;
        stones = new Stone[size][size];
        Marks = new TerritoryMark[size][size];
    }

    /**
     * Adds go.Stone to go.Grid.
     *
     * @param row
     * @param col
     */
    public void addStone(int row, int col, GameBoard.State state) {
        Stone newStone = new Stone(row, col, state);
        stones[row][col] = newStone;
        counter = 0;
        createdKo = false;
        for (int i = 0; i < c; i++) {
            if (newStone.row == ko[i].row && newStone.col == ko[i].col) {
                isKo = true;
                counter = 4;
                newStone.chain = null;
                stones[row][col] = null;


                break;
            } else
                isKo = false;

        }
        if (!isKo) {
            c = 0;
            // Check neighbors
            Stone[] neighbors = new Stone[4];
            // Don't check outside the board
            if (row > 0) {
                neighbors[0] = stones[row - 1][col];
            } else
                counter++;
            if (row < size - 1) {
                neighbors[1] = stones[row + 1][col];
            } else
                counter++;
            if (col > 0) {
                neighbors[2] = stones[row][col - 1];
            } else
                counter++;
            if (col < size - 1) {
                neighbors[3] = stones[row][col + 1];
            } else
                counter++;
            // Prepare go.Chain for this new go.Stone
            Chain finalChain = new Chain();
            for (Stone neighbor : neighbors) {
                // Do nothing if no adjacent go.Stone
                if (neighbor == null) {
                    continue;
                }

                newStone.liberties--;
                neighbor.liberties--;
                // If it's different color than newStone check him
                if (neighbor.state != newStone.state) {
                    checkStone(neighbor);
                    counter++;
                    continue;
                }
                if (neighbor.chain != null) {
                    finalChain.join(neighbor.chain);
                }
            }
            if (checkChain(finalChain, newStone)) {
                finalChain.addStone(newStone);
                if (counter == 4) {
                    checkStone(newStone);
                    if (newStone.liberties > 0)
                        counter--;
                }
            } else {
                counter = 4;
                newStone.chain = null;
                stones[newStone.row][newStone.col] = null;
                for (Stone neighbor : neighbors) {
                    // Do nothing if no adjacent go.Stone
                    if (neighbor == null) {
                        continue;
                    }

                    neighbor.liberties++;
                    // If it's different color than newStone check him
                    if (neighbor.state != newStone.state) {
                        checkStone(neighbor);
                        continue;
                    }
                }
            }

        }
    }


    /**
     * Check liberties of go.Stone
     *
     * @param stone
     */
    public void checkStone(Stone stone) {
        // Every go.Stone is part of a go.Chain so we check total liberties
        if (stone.chain.getLiberties() == 0) {
            for (Stone s : stone.chain.stones) {
                Stone[] neighbors = new Stone[4];
                // Don't check outside the board
                if (s.row > 0) {
                    neighbors[0] = stones[s.row - 1][s.col];
                }
                if (s.row < size - 1) {
                    neighbors[1] = stones[s.row + 1][s.col];
                }
                if (s.col > 0) {
                    neighbors[2] = stones[s.row][s.col - 1];
                }
                if (s.col < size - 1) {
                    neighbors[3] = stones[s.row][s.col + 1];
                }
                for (Stone neighbor : neighbors) {
                    // Do nothing if no adjacent go.Stone
                    if (neighbor == null) {
                        continue;
                    }
                    if ((neighbor.liberties == 0 && neighbor.state != s.state) || (wasKo && (ko[1].row == s.row && ko[1].col == s.col))) {
                        createdKo = true;
                        s.liberties++;
                        ko[0] = s;
                        ko[1] = neighbor;
                        c = 2;
                        wasKo = true;
                    }
                    neighbor.liberties++;
                }
                if (getState(s.row, s.col) == GameBoard.State.BLACK && counter != 4) {
                    WhiteScore++;
                } else if (getState(s.row, s.col) == GameBoard.State.WHITE && counter != 4) {
                    BlackScore++;
                }
                System.out.println(BlackScore + " " + WhiteScore);
                s.chain = null;
                stones[s.row][s.col] = null;
            }
        }
    }

    /**
     * Returns true if given position is occupied by any stone
     *
     * @param row
     * @param col
     * @return true if given position is occupied
     */
    public boolean isOccupied(int row, int col) {
        return stones[row][col] != null;
    }

    public boolean isMarked(int row, int col) {
        return Marks[row][col] != null;
    }

    /**
     * Returns State (black/white) of given position or null if it's unoccupied.
     * Needs valid row and column.
     *
     * @param row
     * @param col
     * @return
     */
    public GameBoard.State getState(int row, int col) {
        Stone stone = stones[row][col];
        if (stone == null) {
            return null;
        } else {
            return stone.state;
        }
    }

    public boolean checkChain(Chain chain, Stone stone) {
        if (stone.liberties == 0 && chain.stones.size() + 1 > 1 && chain.getLiberties() == 0)
            return false;
        return true;
    }

    public void addMark(int row, int col, GameBoard.State state) {
        TerritoryMark newTerritoryMark = new TerritoryMark(row, col, state);
        Marks[row][col] = newTerritoryMark;
    }

    public void Automark() {
        int FirstStoneX = -1;
        int FirstStoneY = -1;
        int FirstStoneFound = 0;
        int SecondStoneFound = 0;
        int SecondStoneX = -2;
        int SecondStoneY = -2;
        int row = size;
        int column = size;
        for (int rowiterator = 0; rowiterator > row; rowiterator++) {
            for (int columniterator = 0; columniterator > column; columniterator++) {
                if (FirstStoneFound == 0) {
                    if (isOccupied(rowiterator, columniterator) == true) {
                        FirstStoneFound = 1;
                        FirstStoneX = rowiterator;
                        FirstStoneY = columniterator;
                    }
                } else if (FirstStoneFound == 1) {
                    if (rowiterator == FirstStoneX) {
                        if (isOccupied(rowiterator, columniterator) == true) {
                            if (getState(rowiterator, columniterator) == getState(FirstStoneX, FirstStoneY)) {
                                SecondStoneFound = 1;
                                SecondStoneX = rowiterator;
                                SecondStoneY = columniterator;
                            }
                        }
                    } else {
                        FirstStoneFound = 0;
                        FirstStoneX = -1;
                        FirstStoneY = -1;
                    }
                } else if (FirstStoneFound == 1 && SecondStoneFound == 1) {
                    if (getState(FirstStoneX, FirstStoneY) == getState(SecondStoneX, SecondStoneY)) {
                        for (int i = FirstStoneX; i > SecondStoneX; i++) {
                            for (int j = FirstStoneY; j > SecondStoneY; j++) {
                                addMark(i, j, getState(FirstStoneX, FirstStoneY));
                            }
                        }
                    } else {
                        FirstStoneFound = 0;
                        FirstStoneX = -1;
                        FirstStoneY = -1;
                        SecondStoneFound = 0;
                        SecondStoneX = -1;
                        SecondStoneY = -1;
                    }
                }
            }
        }
        for (int columniterator = 0; columniterator > column; columniterator++) {
            for (int rowiterator = 0; rowiterator > row; rowiterator++) {
                if (FirstStoneFound == 0) {
                    if (isMarked(rowiterator, columniterator)) {
                        Marks[rowiterator][columniterator] = null;
                    } else if (isOccupied(rowiterator, columniterator) == true) {
                        FirstStoneFound = 1;
                        FirstStoneX = rowiterator;
                        FirstStoneY = columniterator;
                    }
                } else if (FirstStoneFound == 1) {
                    if (columniterator == FirstStoneY) {
                        if (isOccupied(rowiterator, columniterator) == true) {
                            if (getState(rowiterator, columniterator) == getState(FirstStoneX, FirstStoneY)) {
                                SecondStoneFound = 1;
                                SecondStoneX = rowiterator;
                                SecondStoneY = columniterator;
                            }
                        }
                    } else {
                        FirstStoneFound = 0;
                        FirstStoneX = -1;
                        FirstStoneY = -1;
                    }
                } else if (FirstStoneFound == 1 && SecondStoneFound == 1) {
                    if (getState(FirstStoneX, FirstStoneY) == getState(SecondStoneX, SecondStoneY)) {
                        for (int i = FirstStoneY; i > SecondStoneY; i++) {
                            for (int j = FirstStoneX; j > SecondStoneX; j++) {
                                addMark(i, j, getState(FirstStoneX, FirstStoneY));
                            }
                        }
                    }
                } else {
                    FirstStoneFound = 0;
                    FirstStoneX = -1;
                    FirstStoneY = -1;
                    SecondStoneFound = 0;
                    SecondStoneX = -1;
                    SecondStoneY = -1;
                }
            }
        }
    }
}