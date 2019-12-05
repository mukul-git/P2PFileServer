package edu.ufl.p2pFileserver;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class P2PConnection extends StreamSocket implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(P2PConnection.class.getName());

    private boolean session;
    private String message;
    private final IPeer peer;
    private final boolean isFileOwnerInstance;

    public P2PConnection(final Socket socket, final IPeer peer) throws IOException {
        super(socket);
        this.peer = peer;
        if (peer instanceof FileOwner)
            isFileOwnerInstance = true;
        else
            isFileOwnerInstance = false;
    }

    @Override
    public void run() {
        session = true;
        try {
            while (session) {
                message = receiveMessage();
                if (message == null || message.isEmpty()) {
                    continue;
                }
                String command = message.split(" ")[0];
                String arg;
                String[] fileEntries;
                switch (command) {

                    case "connect":


                        break;
                    case "dirRequest":
                        System.out.println("["+peer.getPeerId()+"]"+"Received dirRequest for peer");
                        Map<Long, Long> localFileChunkListWithSize = peer.getFileChunkListWithSize();
                        StringBuilder dirResponseMsg = new StringBuilder();
                        if (peer instanceof FileOwner) {
                            dirResponseMsg.append("dirResponseFOwner ");
                        } else {
                            dirResponseMsg.append("dirResponse ");
                        }
                        localFileChunkListWithSize.forEach((fileId, fileSize) -> {
                            dirResponseMsg.append(fileId + "," + fileSize + ";");
                        });

                        message = dirResponseMsg.toString();
                        sendMessage(message);
                        break;
                    case "upload":
                        System.out.println("["+peer.getPeerId()+"]"+"Received upload request for peer");
                        arg = message.split(" ", 2)[1];
                        String name = arg.split(" ")[0];
                        int sizeInBytes = Integer.parseInt(arg.split(" ")[1]);
                        fileReceive(name, sizeInBytes);
                        break;
                    case "receive":
                        System.out.println("["+peer.getPeerId()+"]"+"Received download for peer");
                        arg = message.split(" ", 2)[1];
                        fileTransmit(arg);
                        break;
                    default:
                        System.out.println("["+peer.getPeerId()+"]"+"Received wrong command: " + command + " from client.");
                        sendMessage("Received invalid request: " + message);
                        break;
                }
            }
        } catch (SocketException se) {
            System.out.println("[" + peer.getPeerId() + "] " + "Connection Reset on this line.");
        } finally {
            close();
        }
    }


    public void fileTransmit(final String file) {

        boolean fileExists = false;
        for (long fi : peer.getFileChunkList()) {
            if (String.valueOf(fi).concat(IPeer.partFileSuffix).equals(file)) {
                fileExists = true;
            }
        }
        if (fileExists) {
            File parentDir = new File(peer.getStagingDir().toString());
            File fileToBeSent = new File(parentDir, file.trim());
            if (fileToBeSent.exists()) {
                sendMessage(String.valueOf(fileToBeSent.length()));
                sendFile(fileToBeSent);
            } else {
                sendMessage("Error: Requested file not found");
            }
        } else {
            sendMessage("Error: Requested file not found");
        }
    }

    public void fileReceive(final String fileName, final int fileSizeInBytes) {

        System.out.println("[" + peer.getPeerId() + "] " + "Expected file's size: " + fileSizeInBytes);
        File f = new File(peer.getStagingDir().toString());

        File fcombined = new File(f, fileName);

        boolean success = getFile(fcombined, fileSizeInBytes);

        if (success) {
            //sendMessage("Success: Received file");
            peer.addToFileChunkList(Long.valueOf(fileName.split(IPeer.partFileSuffix)[0]));
        } else {
            System.out.println("[" + peer.getPeerId() + "] " + "Error: Couldn't receive file: " + fileName + " sent to " + peer.getPeerId());
        }
    }

}