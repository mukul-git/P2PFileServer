package edu.ufl.p2pFileserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Orchestrator {

    public static CyclicBarrier globalSynch;
    private static FileOwner fileOwner;
    private static List<IPeer> peers = new ArrayList<>();
    private static Integer numPeers = 5;

    private static Runnable barrierAction = new Runnable() {
        @Override
        public void run() {
            if (globalSynch.isBroken()) {
                //peers.forEach(iPeer -> iPeer.retrieveInitialSetOfChunksFromOwner());
            }
        }
    };


    private static Runnable peerReadyAction = new Runnable() {
        @Override
        public void run() {
            try {
                globalSynch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    };

    private static Runnable fileOwnerReadyAction = peerReadyAction;

    static {
        globalSynch = new CyclicBarrier(numPeers + 1, barrierAction);// 1 FileOwner + 5 peers
        /**
         * FileOwner awaits on barrier once it has finished creating chunks of original file,
         * and peers await on barrier once their connection with neighbors is established
         */
    }
//
//    public static void main(String[] args) {
//        // Dynamic port range : 49152–65535
//        System.out.println("Enter file owner port number in dynamic port range: (49152–65535) ");
//        Scanner sc = new Scanner(System.in);
//        int fileOwnerPortNum = sc.nextInt();
//        sc.nextLine();
//
//        List<Integer> assignedPorts = new ArrayList<>();
//        assignedPorts.add(fileOwnerPortNum);
//        fileOwner = FileOwner.startInstance(fileOwnerPortNum, fileOwnerReadyAction);
//        int peerPortNum, prevPeerPort = 0;
//
//        for (int i = 0; i < numPeers; ++i) {
//            do {
//                peerPortNum = (int) (Math.random() * ((65535 - 49152) + 1) + 49152);
//            } while (!assignedPorts.contains(peerPortNum));
//            //Putting all peers in a ring
//            IPeer peer = new Peer(fileOwnerPortNum, peerPortNum, prevPeerPort, peerReadyAction);
//            prevPeerPort = peerPortNum;
//            peers.add(peer);
//        }
//
//        // Communicate prevPeerPort to first peer
//        peers.get(0).setDownloadNeighborPort(prevPeerPort);
//
//        // Open gate for peers to start sync process (Use cyclicBarrier/CountdownLatch fileOwner+5 peers)
//        //
//        // fileOwner.
//
//    }

}
