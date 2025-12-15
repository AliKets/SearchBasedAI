public class ZobristTable {
    private static final long[][][] R = new long[8][8][3]; // EMPTY, BLACK, WHITE
    private static final java.util.Random rnd = new java.util.Random(0xC0FFEE);
    static {
        for (int r=0;r<8;r++) for(int c=0;c<8;c++) for(int k=0;k<3;k++) R[r][c][k]=rnd.nextLong();
    }
    public static long pieceKey(int r,int c, Piece p){
        int k = (p==Piece.EMPTY?0:(p==Piece.BLACK?1:2));
        return R[r][c][k];
    }
}
