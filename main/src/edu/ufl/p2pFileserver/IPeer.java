package edu.ufl.p2pFileserver;

import java.util.Scanner;
import java.util.logging.Logger;

public interface IPeer {


    Logger LOGGER = Logger.getLogger(FileOwner.class.getName());


    static int getPort(String[] args, String portTypeName) {
        boolean correctPortFound = false;
        int port = 0;
        do {
            try {
                if (args.length < 1) {
                    throw new NumberFormatException();
                }
                port = Integer.parseInt(args[0]);
                if (port < 1024 && port > 65535) {
                    throw new NumberFormatException();
                } else {
                    correctPortFound = true;
                }
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid entry provided for " + portTypeName + ". Please enter a valid one in range 1024 - 65535");
                Scanner sc = new Scanner(System.in);
                args = new String[]{sc.nextLine()};
            }
        } while (!correctPortFound);
        return port;
    }


    void retrieveChunkListFromOwner();

    void setUploadNeighborPort(final int port);

    void setDownloadNeighborPort(final int port);

}
