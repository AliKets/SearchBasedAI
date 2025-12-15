/**
 * Représente un mouvement dans un jeu de dame.
 * Il stocke les indices de ligne et de colonne du mouvement.
 */
class Move
{
    public int points;
    private int oldrow;
    private int oldcol;
    private int row;
    private int col;
    private Piece currentPiece;
    private Piece oldPiece;
    public int orderScore; //tri

    public Move(){
        row = -1;
        col = -1;
    }

    public Move(int old_r,int old_c,int r, int c,Piece oldP,Piece current){
        row = r;
        col = c;
        oldcol=old_c;
        oldrow=old_r;
        oldPiece=oldP;
        currentPiece=current;
    }
    public Piece getOldPiece(){
        return oldPiece;
    }
    public int getOldRow(){
        return oldrow;
    }
    public int getOldCol(){
        return oldcol;
    }
    public int getRow(){
        return row;
    }
    public Piece getCurrentPiece(){
        return currentPiece;
    }
    public int getCol(){
        return col;
    }

    public void setRow(int r){
        row = r;
    }

    public void setCol(int c){
        col = c;
    }
    public int fromR(){ return getOldRow(); }
    public int fromC(){ return getOldCol(); }
    public int toR(){ return getRow(); }
    public int toC(){ return getCol(); }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Move move = (Move) obj;
        return this.row == move.row && this.col == move.col;
    }
}
