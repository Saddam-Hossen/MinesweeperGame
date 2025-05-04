import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Grid {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MinesweeperGUI frame = new MinesweeperGUI();
            frame.setVisible(true);
        });
    }
}

// ========== GameFrame Class ==========
class MinesweeperGUI extends JFrame {
    private GridLogic gameGrid;
    private JButton[][] buttons;
    private int revealedCount;
    private final int rows = 10;
    private final int cols = 10;
    private final int bombs = 25;

    public MinesweeperGUI() {
        initializeGame();
        setupUI();
    }

    private void initializeGame() {
        gameGrid = new GridLogic(rows, cols, bombs);
        revealedCount = 0;
    }

    private void setupUI() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        buttons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));
                button.setFont(new Font("Arial", Font.BOLD, 14));
                button.setMargin(new Insets(0, 0, 0, 0));

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

        if (revealedCount == (rows * cols - bombs)) {
            gameOver(true);
        }
    }

    private void revealCell(int row, int col) {
        if (!buttons[row][col].isEnabled()) return;

        int count = gameGrid.getCountAtLocation(row, col);
        buttons[row][col].setText(count > 0 ? String.valueOf(count) : "");
        buttons[row][col].setEnabled(false);
        revealedCount++;

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
                    buttons[r][c].setText(count > 0 ? String.valueOf(count) : "");
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
            getContentPane().removeAll();
            initializeGame();
            setupUI();
            revalidate();
            repaint();
        } else {
            System.exit(0);
        }
    }
}

// ========== GridLogic Class (Previously Grid) ==========
class GridLogic {
    private boolean[][] bombGrid;
    private int[][] countGrid;
    private int numRows;
    private int numColumns;
    private int numBombs;
    private Random random;

    public GridLogic(int rows, int columns, int numBombs) {
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
}
