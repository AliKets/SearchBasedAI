public enum Piece {
    BLACK (2,'N'),
    WHITE (4,'R'),
    EMPTY (0,'-');

    private final int index;
    private final char team;
    Piece(int index, char team){ this.index=index; this.team=team; }
    public int index(){ return index; }
    public char team(){ return team; }
}
