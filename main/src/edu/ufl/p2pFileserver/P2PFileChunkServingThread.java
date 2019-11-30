package edu.ufl.p2pFileserver;

import java.net.Socket;

public class P2PFileChunkServingThread extends Thread {
    private Socket socket;

    public P2PFileChunkServingThread(Socket socket) {
        super("P2PFileChunkServingThread");
        this.socket = socket;
    }

    public void run() {

//        try (
//                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(
//                                socket.getInputStream()));
//        ) {
//            String inputLine, outputLine;
//            KnockKnockProtocol kkp = new KnockKnockProtocol();
//            outputLine = kkp.processInput(null);
//            out.println(outputLine);
//
//            while ((inputLine = in.readLine()) != null) {
//                outputLine = kkp.processInput(inputLine);
//                out.println(outputLine);
//                if (outputLine.equals("Bye"))
//                    break;
//            }
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}