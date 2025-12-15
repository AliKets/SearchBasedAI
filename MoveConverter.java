class MoveConverter {
    public static Move ToGlobalMove(String str, Board b) {
        str = str.trim().toUpperCase().replace(" ", "");
        String[] parts = str.split("-");
        if (parts.length != 2) throw new IllegalArgumentException("Format: " + str);

        int colFrom = parts[0].charAt(0) - 'A';
        int rowFromSrv = Character.getNumericValue(parts[0].charAt(1));
        int colTo   = parts[1].charAt(0) - 'A';
        int rowToSrv   = Character.getNumericValue(parts[1].charAt(1));

        int rowFrom = 8 - rowFromSrv;
        int rowTo   = 8 - rowToSrv;

        Piece src  = b.getCell(rowFrom, colFrom);
        Piece dest = b.getCell(rowTo,   colTo);

        return new Move(rowFrom, colFrom, rowTo, colTo, dest, src);
    }

    public static String getStringMove(Move m) {
        char colFrom   = (char) ('A' + m.getOldCol());
        int  rowFromSv = 8 - m.getOldRow();
        char colTo     = (char) ('A' + m.getCol());
        int  rowToSv   = 8 - m.getRow();
        return "" + colFrom + rowFromSv + "-" + colTo + rowToSv;
    }
}
