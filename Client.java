import java.io.*;
import java.net.*;

class Client {
    public static void main(String[] args) {

        // --- Choix de l’hôte et du port ---
        String host = "localhost";
        int port = 8888;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation de 8888.");
            }
        }

        System.out.println("Connexion à " + host + ":" + port + "...");

        Socket MyClient;
        BufferedInputStream input;
        BufferedOutputStream output;
        int[][] board = new int[8][8];
        CPUPlayer player = new CPUPlayer('N');
        Board globalBoard = new Board();
        Move lastSent = null; // pour rollback si coup invalide

        try {
            MyClient = new Socket(host, port);

            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());

            while (true) {
                char cmd = (char) input.read();
                System.out.println(cmd);

                // --- CMD 1 : joueur BLANC ---
                if (cmd == '1') {
                    byte[] aBuffer = new byte[2048];
                    int size = input.available();
                    if (size > aBuffer.length)
                        size = aBuffer.length;
                    if (size > 0)
                        input.read(aBuffer, 0, size);

                    String s = new String(aBuffer, 0, Math.max(0, size)).trim();
                    System.out.println(s);

                    String[] boardValues = s.split(" ");

                    int col = 0, row = 0;
                    for (int i = 0; i < boardValues.length && row < 8; i++) {
                        board[row][col] = Integer.parseInt(boardValues[i]);
                        col++;
                        if (col == 8) {
                            col = 0;
                            row++;
                        }
                    }

                    globalBoard.loadFromArray(board);
                    player = new CPUPlayer('R');

                    System.out.println("Nouvelle partie! Vous jouez blanc.");
                    Move mv = player.think(globalBoard, 'R');

                    globalBoard.play(mv);
                    lastSent = mv;

                    String moveStr = MoveConverter.getStringMove(mv);
                    System.out.println("→ " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }

                // --- CMD 2 : joueur NOIR ---
                if (cmd == '2') {
                    System.out.println("Nouvelle partie! Vous jouez noir.");

                    byte[] aBuffer = new byte[2048];
                    int size = input.available();
                    if (size > aBuffer.length)
                        size = aBuffer.length;
                    if (size > 0)
                        input.read(aBuffer, 0, size);

                    String s = new String(aBuffer, 0, Math.max(0, size)).trim();
                    System.out.println(s);

                    String[] boardValues = s.split(" ");

                    int col = 0, row = 0;
                    for (int i = 0; i < boardValues.length && row < 8; i++) {
                        board[row][col] = Integer.parseInt(boardValues[i]);
                        col++;
                        if (col == 8) {
                            col = 0;
                            row++;
                        }
                    }

                    globalBoard.loadFromArray(board);
                    // CPUPlayer reste 'N'
                }

                // --- CMD 3 : serveur demande un coup ---
                if (cmd == '3') {
                    byte[] aBuffer = new byte[64];
                    int size = input.available();
                    if (size > aBuffer.length)
                        size = aBuffer.length;
                    if (size > 0)
                        input.read(aBuffer, 0, size);

                    String s = new String(aBuffer, 0, Math.max(0, size)).trim();
                    System.out.println("Dernier coup: " + s);

                    // coup adverse
                    if (!s.isEmpty()) {
                        try {
                            Move last = MoveConverter.ToGlobalMove(s, globalBoard);
                            globalBoard.play(last);
                        } catch (Exception e) {
                            System.out.println("WARN: coup adverse illisible: " + s);
                        }
                    }

                    // IA
                    Move mv = player.think(globalBoard, player.getTeam());

                    if (mv == null) {
                        String noop = "A1-A1";
                        output.write(noop.getBytes(), 0, noop.length());
                        output.flush();
                        continue;
                    }

                    globalBoard.play(mv);
                    lastSent = mv;

                    String moveStr = MoveConverter.getStringMove(mv);
                    System.out.println(moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }

                // --- CMD 4 : coup invalide ---
                if (cmd == '4') {
                    System.out.println("Coup invalide reçu, renvoi d’un coup neutre.");
                    String move = "A1-A1";
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // --- CMD 5 : fin de partie ---
                if (cmd == '5') {
                    byte[] aBuffer = new byte[64];
                    int size = input.available();
                    if (size > aBuffer.length)
                        size = aBuffer.length;
                    if (size > 0)
                        input.read(aBuffer, 0, size);

                    String s = new String(aBuffer, 0, Math.max(0, size)).trim();
                    System.out.println("Partie terminée. Dernier coup: " + s);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
