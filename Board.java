import java.util.ArrayList;

// Classe représentant un plateau de jeu 3x3 pour Tic-Tac-Toe.
class Board {
    private static final int INF = 1_000_000_000;
    private Piece[][] board;

    // Basically les directions 8 totales (séparation entre DIRS et ADJ au cas ou on
    // veut changer ADJ pour gagner seulement si connection a
    // gauche,bas,haut,droit)....bref on
    // on pourrait avoir le meme pour les deux
    private static final int[][] DIRS = {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, // orthogonales
            { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } // diagonales
    };
    // Pour voir les adjacences(win)
    private static final int[][] ADJ = {
            { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }
    };

    public Board() {
        board = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = Piece.EMPTY;
            }
        }
        for (int c = 0; c < 8; c++) {
            board[0][c] = Piece.BLACK;
            board[7][c] = Piece.BLACK;
        }
        for (int r = 0; r < 8; r++) {
            board[r][0] = Piece.WHITE;
            board[r][7] = Piece.WHITE;
        }
    }

    // Pour trouve la piece à un endroit spécifique
    public Piece at(int r, int c) {
        return getCell(r, c);
    }

    // Pour savoir nbre grp connected, combien isolé et le largest cluster
    public static final class ConnInfo {
        public int components;
        public int largest;
        public int isolated;
    }

    public boolean isLegalMove(char team, Move m) {
        for (Move legal : getAvailableMoves(team)) {
            if (legal.equals(m))
                return true;
        }
        return false;
    }

    public ConnInfo connectivity(char team) {
        boolean[][] vis = new boolean[8][8];
        ConnInfo ci = new ConnInfo();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (!vis[r][c] && at(r, c).team() == team) {
                    int sz = 0;
                    java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
                    q.add(new int[] { r, c });
                    vis[r][c] = true;
                    while (!q.isEmpty()) {
                        int[] u = q.poll();
                        sz++;
                        for (int[] d : ADJ) {
                            int rr = u[0] + d[0];
                            int cc = u[1] + d[1];
                            if (inBoard(rr, cc) && !vis[rr][cc] && at(rr, cc).team() == team) {
                                vis[rr][cc] = true;
                                q.add(new int[] { rr, cc });
                            }
                        }
                    }
                    ci.components++;
                    if (sz == 1)
                        ci.isolated++;
                    if (sz > ci.largest)
                        ci.largest = sz;
                }
            }
        }
        return ci;
    }

    // Vrai si toutes les pieces de la team sont connected
    public boolean isConnected(char team) {
        ConnInfo ci = connectivity(team);
        return ci.components <= 1 && ci.largest > 0;
    }

    // Constructeur de copie
    public Board(Board other) {
        this.board = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                this.board[i][j] = other.board[i][j];
            }
        }
    }

    // Place la pièce 'mark' sur le plateau, à la position spécifiée dans Move
    public void play(Move m) {
        board[m.getRow()][m.getCol()] = board[m.getOldRow()][m.getOldCol()];
        board[m.getOldRow()][m.getOldCol()] = Piece.EMPTY;
        // int score=evaluate(this,board[m.getOldRow()][m.getOldCol()].team());
    }

    public Piece getCell(int row, int col) {
        return board[row][col];
    }

    // Retourne 100 pour une victoire, -100 pour une défaite, 0 pour un match nul
    public int evaluate(Board b, char me) {
        char opp = (me == 'N' ? 'R' : 'N');

        if (b.isConnected(me))
            return 1_000_000;
        if (b.isConnected(opp))
            return -1_000_000;

        Board.ConnInfo my = b.connectivity(me);
        Board.ConnInfo his = b.connectivity(opp);

        int score = 0;
        // Composantes: poids fort
        score += 120 * (his.components - my.components);
        // Plus grand cluster
        score += 6 * (my.largest - his.largest);
        // Isolés
        score += 8 * (his.isolated - my.isolated);

        int myCaps = countCaptures(me);
        int hisCaps = countCaptures(opp);
        score += (myCaps - hisCaps);

        return score;
    }

    // Compte le nbre de captures possibles pour une team spécifié
    public int countCaptures(char team) {
        int caps = 0;
        for (Move m : getAvailableMoves(team)) {
            if (isCapture(m))
                caps++;
        }
        return caps;
    }

    // regarde si le coup "mange" une piece
    public boolean isCapture(Move m) {
        Piece dest = getCell(m.getRow(), m.getCol());
        Piece src = getCell(m.getOldRow(), m.getOldCol());
        return dest != Piece.EMPTY && dest.team() != src.team();
    }

    // Remove un move(chronobreak lol)
    public void remove(Move m) {
        board[m.getRow()][m.getCol()] = m.getOldPiece();
        board[m.getOldRow()][m.getOldCol()] = m.getCurrentPiece();
    }

    // Obtient une liste de tous les mouvements disponibles sur le plateau.
    private int countInLine(int r, int c, int dr, int dc) {
        // La piece live
        int cnt = 1;
        int rr = r + dr, cc = c + dc;
        while (inBoard(rr, cc)) {
            if (board[rr][cc] != Piece.EMPTY) {
                cnt++;
            }
            rr += dr;
            cc += dc;
        }
        rr = r - dr;
        cc = c - dc;
        while (inBoard(rr, cc)) {
            if (board[rr][cc] != Piece.EMPTY) {
                cnt++;
            }
            rr -= dr;
            cc -= dc;
        }
        return cnt;
    }

    // Check pour des ennemies dans le path
    private boolean pathBlockedByEnemy(int r, int c, int dr, int dc, int steps, char ourTeam) {
        for (int k = 1; k < steps; k++) {
            int rr = r + dr * k;
            int cc = c + dc * k;
            Piece p = board[rr][cc];
            if (p != Piece.EMPTY && p.team() != ourTeam)
                return true; // adversaire bloque
        }
        return false;
    }

    // Dans le board?
    private boolean inBoard(int r, int c) {
        return 0 <= r && r < 8 && 0 <= c && c < 8;
    }

    // get les moves qui sont possible
    public ArrayList<Move> getAvailableMoves(char team) {
        ArrayList<Move> moves = new ArrayList<>();
        // row
        for (int r = 0; r < 8; r++) {
            // col
            for (int c = 0; c < 8; c++) {
                // piece current du loop
                Piece me = board[r][c];
                // Si c'est le cas, skip cette itération du loop
                if (me == Piece.EMPTY || me.team() != team)
                    continue;

                for (int[] d : DIRS) {
                    int steps = countInLine(r, c, d[0], d[1]);
                    int rr = r + d[0] * steps, cc = c + d[1] * steps;
                    // Skip si pas dans le board
                    if (!inBoard(rr, cc))
                        continue;
                    // skip si blocked
                    if (pathBlockedByEnemy(r, c, d[0], d[1], steps, team))
                        continue;

                    Piece dest = board[rr][cc];
                    if (dest == Piece.EMPTY || dest.team() != team) {
                        moves.add(new Move(r, c, rr, cc, dest, me)); // garde ton constructeur Move existant
                    }
                }
            }
        }
        return moves;
    }

    // Pour débuter la partie
    public void loadFromArray(int[][] data) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int val = data[r][c];
                Piece piece = Piece.EMPTY;
                for (Piece p : Piece.values()) {
                    if (p.index() == val) {
                        piece = p;
                        break;
                    }
                }
                board[r][c] = piece;
            }
        }
    }

    // Affiche l'état actuel du plateau.
    public void printBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == Piece.WHITE) {
                    System.out.print(" X ");
                } else if (board[i][j] == Piece.BLACK) {
                    System.out.print(" O ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
