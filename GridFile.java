import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Grid {
    // Grid class implementation (core requirements)
    private boolean[][] bombGrid;
    private int[][] countGrid;
    private int numRows;
    private int numColumns;
    private int numBombs;
    private Random random;

    // Default constructor (10x10 with 25 bombs)
    public Grid() {
        this(10, 10, 25);
    }

    // Constructor with custom rows and columns (25 bombs)
    public Grid(int rows, int columns) {
        this(rows, columns, 25);
    }

    // Constructor with custom rows, columns and bomb count
    public Grid(int rows, int columns, int numBombs) {
        this.numRows = rows;
        this.numColumns = columns;
        this.numBombs = numBombs;
        this.random = new Random();

        createBombGrid();
        createCountGrid();
    }

    private void createBombGrid() {
        bombGrid = new boolean[numRows][numColumns];
        int bombsPlaced = 0;

        while (bombsPlaced < numBombs) {
            int row = random.nextInt(numRows);
            int col = random.nextInt(numColumns);

            if (!bombGrid[row][col]) {
                bombGrid[row][col] = true;
                bombsPlaced++;
            }
        }
    }

    private void createCountGrid() {
        countGrid = new int[numRows][numColumns];

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numColumns; c++) {
                countGrid[r][c] = countAdjacentBombs(r, c);
            }
        }
    }

    private int countAdjacentBombs(int row, int col) {
        int count = 0;

        for (int r = Math.max(0, row - 1); r <= Math.min(numRows - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(numColumns - 1, col + 1); c++) {
                if (bombGrid[r][c]) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public int getNumBombs() {
        return numBombs;
    }

    public boolean[][] getBombGrid() {
        boolean[][] copy = new boolean[numRows][numColumns];
        for (int r = 0; r < numRows; r++) {
            System.arraycopy(bombGrid[r], 0, copy[r], 0, numColumns);
        }
        return copy;
    }

    public int[][] getCountGrid() {
        int[][] copy = new int[numRows][numColumns];
        for (int r = 0; r < numRows; r++) {
            System.arraycopy(countGrid[r], 0, copy[r], 0, numColumns);
        }
        return copy;
    }

    public boolean isBombAtLocation(int row, int column) {
        if (row < 0 || row >= numRows || column < 0 || column >= numColumns) {
            return false;
        }
        return bombGrid[row][column];
    }

    public int getCountAtLocation(int row, int column) {
        if (row < 0 || row >= numRows || column < 0 || column >= numColumns) {
            return 0;
        }
        return countGrid[row][column];
    }

    // GUI Implementation
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MinesweeperGUI game = new MinesweeperGUI();
            game.setVisible(true);
        });
    }
}

class MinesweeperGUI extends JFrame {
    private Grid gameGrid;
    private JButton[][] buttons;
    private int revealedCount;
    private int rows;
    private int cols;
    private int bombs;

    public MinesweeperGUI() {
        this(10, 10, 25); // Default to 10x10 with 25 bombs
    }

    public MinesweeperGUI(int rows, int cols, int bombs) {
        this.rows = rows;
        this.cols = cols;
        this.bombs = bombs;
        initializeGame();
        setupUI();
    }

    private void initializeGame() {
        gameGrid = new Grid(rows, cols, bombs);
        revealedCount = 0;
    }

    private void setupUI() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        buttons = new JButton[rows][cols];

        // Create buttons for each cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));
                button.setFont(new Font("Arial", Font.BOLD, 14));

                final int row = r;
                final int col = c;
                button.addActionListener(e -> handleCellClick(row, col));

                buttons[r][c] = button;
                gridPanel.add(button);
            }
        }

        add(gridPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void handleCellClick(int row, int col) {
        if (!buttons[row][col].isEnabled()) {
            return; // Already revealed
        }

        if (gameGrid.isBombAtLocation(row, col)) {
            revealAllBombs();
            gameOver(false);
            return;
        }

        revealCell(row, col);

        // Check for win condition
        if (revealedCount == (rows * cols - bombs)) {
            gameOver(true);
        }
    }

    private void revealCell(int row, int col) {
        if (!buttons[row][col].isEnabled()) {
            return;
        }

        int count = gameGrid.getCountAtLocation(row, col);
        buttons[row][col].setText(count > 0 ? String.valueOf(count) : "");
        buttons[row][col].setEnabled(false);
        revealedCount++;

        // Extra credit: Auto-reveal for 0-count cells
        if (count == 0) {
            revealAdjacentCells(row, col);
        }
    }

    private void revealAdjacentCells(int row, int col) {
        for (int r = Math.max(0, row - 1); r <= Math.min(rows - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(cols - 1, col + 1); c++) {
                if (!gameGrid.isBombAtLocation(r, c)) {
                    revealCell(r, c);
                }
            }
        }
    }

    private void revealAllBombs() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (gameGrid.isBombAtLocation(r, c)) {
                    buttons[r][c].setText("💣");
                    buttons[r][c].setBackground(Color.RED);
                } else {
                    int count = gameGrid.getCountAtLocation(r, c);
                    buttons[r][c].setText(String.valueOf(count));
                }
                buttons[r][c].setEnabled(false);
            }
        }
    }

    private void gameOver(boolean won) {
        String message = won ? "Congratulations! You won!" : "Game Over! You hit a bomb!";
        int option = JOptionPane.showConfirmDialog(
                this,
                message + "\nWould you like to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        getContentPane().removeAll();
        initializeGame();
        setupUI();
        revalidate();
        repaint();
    }
}
