package edu.ufl.p2pFileserver;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Peer implements IPeer {
    private final static Logger LOGGER = Logger.getLogger(Peer.class.getName());

    private final int fileOwnerPort;
    private final int selfPort;
    // Neighbor from where this node downloads chunk
    private final int downloadNeighborPort;
    // Neighbor to which this node uploads chunk
    private int uploadNeighborPort;
    //private IPeer uploadNeighbor;
    //private IPeer downloadNeighbor;
    private final List<Long> completeFileChunkList;

    //TODO: Add a monitor to it
    private final Map<Long, Path> chunkIdToChunkRepo;
    private final Map<Long, Long> fileIdToSizeMap;

    private final Map<Long, Long> downloadNeighborFileChunkToSizeMap;

    private StreamSocket fOwnerStreamSocket;
    private StreamSocket uploadStreamSocket;
    private StreamSocket downloadStreamSocket;
    private final File stagingDir;
    private ServerSocket selfListenerServerSocket;


    public Peer(final int fileOwnerPort, final int selfPort, final int downloadNeighborPort) {

        this.fileOwnerPort = fileOwnerPort;
        this.selfPort = selfPort;
        this.downloadNeighborPort = downloadNeighborPort;
        //this.peerReadyAction = peerReadyAction;
        chunkIdToChunkRepo = new HashMap<>();
        fileIdToSizeMap = new HashMap<>();
        downloadNeighborFileChunkToSizeMap = new HashMap<>();
        completeFileChunkList = new ArrayList<>();
        System.out.println("Starting Peer at port num " + selfPort + " and use peer at port " + downloadNeighborPort + " as download neighbor");

        //Create staging directory
        stagingDir = new File(getPeerId());
        CommonUtils.deleteDirectory(stagingDir);
        stagingDir.mkdirs();

        //Start listening for connection from peers
        try {
            selfListenerServerSocket = new ServerSocket(selfPort);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public List<Long> getFileChunkList() {
        return new ArrayList<>(chunkIdToChunkRepo.keySet());
    }

    @Override
    public void setFileChunkListWithSize(Map<Long, Long> fileOwnerFileChunkListWithSize) {
        fileIdToSizeMap.putAll(fileOwnerFileChunkListWithSize);
        completeFileChunkList.addAll(fileIdToSizeMap.keySet());
    }

    @Override
    public Map<Long, Long> getFileChunkListWithSize() {

        chunkIdToChunkRepo.forEach((id, path) -> {

            Path filePath = path;
            long size = 0;
            try {
                size = Files.size(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            fileIdToSizeMap.putIfAbsent(id, size);
        });

        return fileIdToSizeMap;
    }

    @Override
    public List<Long> getRemainingFileChunkList() {
        List<Long> remainingFileChunkList = new ArrayList<>(completeFileChunkList);
        remainingFileChunkList.removeAll(chunkIdToChunkRepo.keySet());
        return remainingFileChunkList;
    }

    @Override
    public Path getStagingDir() {
        return stagingDir.toPath();
    }

    @Override
    public boolean addToFileChunkList(final Long filePartId) {
        Path fileName = Paths.get(stagingDir.getPath() + File.separator + filePartId + partFileSuffix);
        chunkIdToChunkRepo.put(filePartId, fileName);
        return true;//TODO: Remove
    }

    @Override
    public void setUploadNeighborPort(int port) {

    }

    @Override
    public void setDownloadNeighborPort(int port) {

    }

    @Override
    public String getPeerId() {
        return String.valueOf(selfPort);
    }

    @Override
    public void updateDownloadNeighborFileChunkListWithSize(Map<Long, Long> uploadNeighborFileChunkListWithSize) {
        uploadNeighborFileChunkListWithSize.forEach((fileId, fileSizeInBytes) -> {
            downloadNeighborFileChunkToSizeMap.putIfAbsent(fileId, fileSizeInBytes);
        });
    }

    public Map<Long, Long> getDownloadNeighborFileChunkToSizeMap() { //TODO: Check for concurrency issues
        return downloadNeighborFileChunkToSizeMap;
    }


    private List<Integer> sendAvailableFileChunkList() {
        return new ArrayList<>();
    }


    private void sendFileToUploadNeighbor(int uploadNeighborPort) {

    }

    private void receiveFileFromDownloadNeighbor(int downloadNeighborPort) {
    }

    public static void main(String[] args) throws UnknownHostException {
        int fOwnerPort, selfPort, downloadNeighborPort;
        if (args.length < 3) {
            LOGGER.severe("[Peer]" + "Incomplete arguments provided.");
            fOwnerPort = IPeer.getPort(new String[]{}, "File Owner Port");
            selfPort = IPeer.getPort(new String[]{}, "Peer Port");
            downloadNeighborPort = IPeer.getPort(new String[]{}, "Download Neighbor Port");
        } else {
            fOwnerPort = IPeer.getPort(new String[]{args[0]}, "File Owner Port");
            selfPort = IPeer.getPort(new String[]{args[1]}, "Peer Port");
            downloadNeighborPort = IPeer.getPort(new String[]{args[2]}, "Download Neighbor Port");
        }

        Peer self = new Peer(fOwnerPort, selfPort, downloadNeighborPort);

        LOGGER.info("[" + selfPort + "] " + "Starting Peer instance at port " + selfPort);
        final Thread newConnectionListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Thread thread = null;
                    try {
                        thread = new Thread(new P2PConnection(self.getServerSocket().accept(), self));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    thread.start();
                }
            }
        });
        newConnectionListenerThread.start();


        //TODO: Connect to fileOwner
        final InetAddress localHost = InetAddress.getByName("127.0.0.1");
        int i = 0;
        boolean isConnected = false;

        while (!isConnected) {//TODO: Change to state machine
            StreamSocket fOwnerStreamSocket = null;
            try {
                fOwnerStreamSocket = new StreamSocket(localHost, fOwnerPort);
                self.setfOwnerStreamSocket(fOwnerStreamSocket);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            if (fOwnerStreamSocket != null && fOwnerStreamSocket.getSocket().isConnected()) {
                isConnected = true; //TODO: Change state of peer to fOwner Connected
                LOGGER.info("[" + selfPort + "] " + "File Owner connection established.");
                //TODO: Send connect message
            } else {
                try {
                    System.out.println("[" + selfPort + "] " + "Waiting to connect to file owner at port:" + fOwnerPort);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //TODO: Connect to download neighbor
        isConnected = false;
        while (!isConnected) {//TODO: Change to state machine
            StreamSocket downloadNeighborStreamSocket = null;
            try {
                downloadNeighborStreamSocket = new StreamSocket(localHost, downloadNeighborPort);
                self.setDownloadStreamSocket(downloadNeighborStreamSocket);
            } catch (IOException e) {
                // Wait for download neighbor to spawn
                //e.printStackTrace();
            }
            if (downloadNeighborStreamSocket != null && downloadNeighborStreamSocket.getSocket().isConnected()) {
                isConnected = true; //TODO: Change state of peer to download neighbor connected
                LOGGER.info("[" + selfPort + "] " + "Download neighbor at port:" + downloadNeighborPort + " connection established.");
            } else {
                try {
                    System.out.println("[" + selfPort + "] " + "Waiting for download neighbor's connection at port:" + downloadNeighborPort);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        // Utility vars to be used
        Random random = new Random();
        long randomFileId = 0;
        try {
            //Initiate file chunk list exchange with owner
            System.out.println("[" + selfPort + "] " + "Requesting file chunk list from FileOwner");
            self.fOwnerStreamSocket.sendMessage("dirRequest ");
            String fileChunkListMessage = self.fOwnerStreamSocket.receiveMessage();
            self.processFileChunkListMessage(fileChunkListMessage);


            //Download 20% of initial files randomly from fileOwner
            List<Long> remainingFileChunkList = self.getRemainingFileChunkList();
            int initialChunkNums = (int) (remainingFileChunkList.size() * 0.2);
            for (int k = 0; k < initialChunkNums; ++k) {
                randomFileId = remainingFileChunkList.get(random.nextInt(remainingFileChunkList.size()));
                self.download(randomFileId + partFileSuffix, self.fOwnerStreamSocket);
            }

            //Request list of files from download neighbor
            syncWithDownloadNeighbor(self);


            while (remainingFileChunkList.size() > 0) {

                List<Long> downloadNeighborDiff = new ArrayList<>(self.getDownloadNeighborFileChunkToSizeMap().keySet());
                downloadNeighborDiff.removeAll(self.chunkIdToChunkRepo.keySet());
                if (downloadNeighborDiff.size() == 0) {
                    //In sync with download neighbor
                    //Download a chunk from FileOwner
                    randomFileId = remainingFileChunkList.get(random.nextInt(remainingFileChunkList.size()));
                    self.fOwnerStreamSocket.sendMessage("download " + randomFileId);
                    //Re-sync with download neighbor
                    syncWithDownloadNeighbor(self);
                } else {
                    randomFileId = downloadNeighborDiff.get(random.nextInt(remainingFileChunkList.size()));
                    self.downloadStreamSocket.sendMessage("download " + randomFileId);
                }
                remainingFileChunkList = self.getRemainingFileChunkList();

            }
        } catch (SocketException e) {
            e.printStackTrace();
            //TODO: Handle connection reset
        }

        // Rejoin files
        File rejoinedFilePartDestination = Paths.get(self.getStagingDir() + File.separator + "test.pdf").toFile();
        if (!rejoinedFilePartDestination.exists()) {
            try {
                rejoinedFilePartDestination.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        collateFiles(new ArrayList<>(self.chunkIdToChunkRepo.values()), rejoinedFilePartDestination);
    }

    private static void syncWithDownloadNeighbor(final Peer peer) throws SocketException {
        peer.downloadStreamSocket.sendMessage("dirRequest ");
        String downloadNeighborFileChunkListMessage = peer.downloadStreamSocket.receiveMessage();
        peer.processDownloadNeighborFileChunkListMessage(downloadNeighborFileChunkListMessage);
    }

    @Override
    public ServerSocket getServerSocket() {
        return selfListenerServerSocket;
    }

    public void setfOwnerStreamSocket(StreamSocket fOwnerStreamSocket) {
        this.fOwnerStreamSocket = fOwnerStreamSocket;
    }

    public void setUploadStreamSocket(StreamSocket uploadStreamSocket) {
        this.uploadStreamSocket = uploadStreamSocket;
    }

    public void setDownloadStreamSocket(StreamSocket downloadStreamSocket) {
        this.downloadStreamSocket = downloadStreamSocket;
    }

    public static void collateFiles(final List<Path> filePartPaths, final File rejoinedFile) {

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriterOutput = null;
        try {
            fileWriter = new FileWriter(rejoinedFile, true);
            bufferedWriterOutput = new BufferedWriter(fileWriter);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        filePartPaths.sort(Path::compareTo);
        for (Path filePartPath : filePartPaths) {
            File filePart = filePartPath.toFile();
            System.out.println("Merging file part: " + filePart.getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(filePart);
                BufferedReader in = new BufferedReader(new InputStreamReader(fis));

                String aLine;
                while ((aLine = in.readLine()) != null) {
                    bufferedWriterOutput.write(aLine);
                    bufferedWriterOutput.newLine();
                }

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bufferedWriterOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void processFileChunkListMessage(final String message) {
        Map<Long, Long> fileOwnerFileChunkListWithSize = new HashMap<>();
        String arg = message.split(" ")[1];
        String[] fileEntries = arg.split(";");
        for (String fileEntry : fileEntries) {
            if (fileEntry != null && !fileEntry.isEmpty()) {
                String[] fileEntrySplit = fileEntry.split(",");
                if (fileEntrySplit != null && fileEntrySplit.length == 2) {
                    long fileId = Long.valueOf(fileEntrySplit[0]);
                    long fileSizeInBytes = Long.valueOf(fileEntrySplit[1]);
                    fileOwnerFileChunkListWithSize.put(fileId, fileSizeInBytes);
                }
            }
        }
        setFileChunkListWithSize(fileOwnerFileChunkListWithSize);
    }

    public void processDownloadNeighborFileChunkListMessage(final String message) {
        Map<Long, Long> downloadNeighborFileChunkListWithSize = new HashMap<>();
        String arg = message.split(" ")[1];
        String[] fileEntries = arg.split(";");
        for (String fileEntry : fileEntries) {
            if (fileEntry != null && !fileEntry.isEmpty()) {
                String[] fileEntrySplit = fileEntry.split(",");
                if (fileEntrySplit != null && fileEntrySplit.length == 2) {
                    long fileId = Long.valueOf(fileEntrySplit[0]);
                    long fileSizeInBytes = Long.valueOf(fileEntrySplit[1]);
                    downloadNeighborFileChunkListWithSize.put(fileId, fileSizeInBytes);
                }
            }
        }
        updateDownloadNeighborFileChunkListWithSize(downloadNeighborFileChunkListWithSize);
    }

    public void download(final String filename, final StreamSocket streamSocket) throws SocketException {

        File file = new File(filename);
        int fileSize;

        streamSocket.sendMessage("receive " + file.getName());
        String headerMessage = streamSocket.receiveMessage();
        if (headerMessage.toLowerCase().contains("error")) {
            System.out.println("[" + selfPort + "] " + headerMessage);
        } else {
            fileSize = Integer.parseInt(headerMessage);

            File downloadCopy = new File(getStagingDir().toString(), file.getName());
            boolean isReceivedByClient = streamSocket.getFile(downloadCopy, fileSize);

            if (isReceivedByClient) {
                //sendMessage("Success: Received file");
                addToFileChunkList(Long.valueOf(filename.split(IPeer.partFileSuffix)[0]));
            } else {
                LOGGER.severe("[" + selfPort + "] " + "Error: Couldn't receive file: " + filename + " sent to " + getPeerId());
            }

            System.out.println("[" + selfPort + "] " + "File chunk received by peer: " + file.getName());
        }

    }

}
