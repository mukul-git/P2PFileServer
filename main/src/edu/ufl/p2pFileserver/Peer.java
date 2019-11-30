package edu.ufl.p2pFileserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Peer implements IPeer {
    private final static Logger LOGGER = Logger.getLogger(Peer.class.getName());

    private final int fileOwnerPort;
    private final int selfPort;
    private int downloadNeighborPort;
    private int uploadNeighborPort;
    //private IPeer uploadNeighbor;
    //private IPeer downloadNeighbor;
    private Map<Integer, Object> chunkIdToChunkRepo;
    private StreamSocket uploadStreamSocket;
    private StreamSocket downloadStreamSocket;

    //private final Runnable peerReadyAction;

//    public Peer(final int fileOwnerPort, final int selfPort, final int downloadNeighborPort, final Runnable peerReadyAction) {
//
//        this.fileOwnerPort = fileOwnerPort;
//        this.selfPort = selfPort;
//        this.downloadNeighborPort = downloadNeighborPort;
//        this.peerReadyAction = peerReadyAction;
//        chunkIdToChunkRepo = new HashMap<>();
//        System.out.println("Starting Peer at port num " + selfPort + " and use peer at port " + downloadNeighborPort + " as download neighbor");
//        //Delegate Connect request to fileOwner
//        //fileOwner.registerPeer(this);
//
//        if (!isPeerReady()) {
//            // Start a new thread to check readiness and execute peerReadyAction when ready
//            new Thread(peerReadyCheckJob);
//        }
//    }

    public Peer(final int fileOwnerPort, final int selfPort, final int downloadNeighborPort) {

        this.fileOwnerPort = fileOwnerPort;
        this.selfPort = selfPort;
        this.downloadNeighborPort = downloadNeighborPort;
        //this.peerReadyAction = peerReadyAction;
        chunkIdToChunkRepo = new HashMap<>();
        System.out.println("Starting Peer at port num " + selfPort + " and use peer at port " + downloadNeighborPort + " as download neighbor");
        //Delegate Connect request to fileOwner
        //fileOwner.registerPeer(this);

//        if (!isPeerReady()) {
//            // Start a new thread to check readiness and execute peerReadyAction when ready
//            new Thread(peerReadyCheckJob);
//        }
    }


    @Override
    public void retrieveChunkListFromOwner() {

    }

    @Override
    public void setUploadNeighborPort(int port) {

    }

    @Override
    public void setDownloadNeighborPort(int port) {

    }

    private List<Integer> sendAvailableFileChunkList() {
        return new ArrayList<>();
    }


    private void sendFileToUploadNeighbor(int uploadNeighborPort) {

    }

    private void receiveFileFromDownloadNeighbor(int downloadNeighborPort) {
    }

    public static void main(String[] args) {
        int fOwnerPort, selfPort, downloadNeighborPort;
        if (args.length < 3) {
            LOGGER.severe("Incomplete arguments provided.");
            fOwnerPort = IPeer.getPort(new String[]{}, "File Owner Port");
            selfPort = IPeer.getPort(new String[]{}, "Peer Port");
            downloadNeighborPort = IPeer.getPort(new String[]{}, "Download Neighbor Port");
        } else {
            fOwnerPort = IPeer.getPort(new String[]{args[0]}, "File Owner Port");
            selfPort = IPeer.getPort(new String[]{args[1]}, "Peer Port");
            downloadNeighborPort = IPeer.getPort(new String[]{args[2]}, "Download Neighbor Port");
        }

        Peer self = new Peer(fOwnerPort, selfPort, downloadNeighborPort);
    }


//    private boolean isPeerReady() {
//        return fileOwnerPort != 0 && selfPort != 0 && downloadNeighborPort != 0 && uploadNeighborPort != 0;
//    }

//    private Runnable peerReadyCheckJob = new Runnable() {
//        @Override
//        public void run() {
//            while (!isPeerReady()) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            new Thread(peerReadyAction);
//        }
//    };
}
