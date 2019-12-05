package edu.ufl.p2pFileserver;

import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.logging.Logger;


public class FileOwner implements IPeer {

    private final static Logger LOGGER = Logger.getLogger(FileOwner.class.getName());


    private static FileOwner fileOwnerInstance;

    private long fileSize = 100 * 1024 * 1024;
    private File overseenFile;
    private List<Peer> connetedPeers;
    private Map<Long, Path> partFilesMap;
    private final Map<Long, Long> fileIdToSizeMap;
    private int kBPerSplit = 100;
    private String currentDirPath;
    private final File stagingDir;

    @Override
    public ServerSocket getServerSocket() {
        return fOwnerServerSocket;
    }

    private ServerSocket fOwnerServerSocket;


    public FileOwner(int portNumber) {
        partFilesMap = new HashMap<>();
        fileIdToSizeMap = new HashMap<>();
        overseenFile = new File("test.pdf");
        currentDirPath = overseenFile.getParent();
        stagingDir = new File(currentDirPath, "fOwner");
        CommonUtils.deleteDirectory(stagingDir);
        stagingDir.mkdirs();

        if (!overseenFile.exists() || overseenFile.length() >= fileSize) {
            LOGGER.severe("[FileOwner] File not found or is not of required size. Please add test.pdf file of atleast 1 MB size.");
        } else {
            LOGGER.info("[FileOwner] Creating file partitions");
            try {
                Files.copy(Paths.get(overseenFile.getPath()), Paths.get(stagingDir.getPath() + File.separator + overseenFile.getName()));
                partitionFile();
            } catch (Exception e) {
                LOGGER.severe("[FileOwner] File split operation failed." + e.getMessage());
            }

            //Start listening for connection from peers
            try {
                fOwnerServerSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Split a file into multiples files.
     *
     * @throws IOException
     */
    public void partitionFile() throws IOException {

        List<Path> partFiles = new ArrayList<>();
        final long sourceSize = Files.size(Paths.get(overseenFile.getName()));
        final long bytesPerSplit = 1024L * kBPerSplit;
        final long numSplits = sourceSize / bytesPerSplit;
        final long remainingBytes = sourceSize % bytesPerSplit;
        int position = 0;

        try (RandomAccessFile sourceFile = new RandomAccessFile(overseenFile.getName(), "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            for (; position < numSplits; position++) {
                //write multipart files.
                writePartToFile(bytesPerSplit, position, position * bytesPerSplit, sourceChannel, partFiles);
            }

            if (remainingBytes > 0) {
                writePartToFile(remainingBytes, position, position * bytesPerSplit, sourceChannel, partFiles);
            }
        }
    }

    private void writePartToFile(long byteSize, long partID, long bytePosition, FileChannel sourceChannel, List<Path> partFiles) throws IOException {

        Path fileName = Paths.get(stagingDir.getPath() + File.separator + partID + partFileSuffix);
        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(bytePosition);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFilesMap.put(partID, fileName);
        fileIdToSizeMap.put(partID, Files.size(fileName));
    }

    private void sendFileChunk(int chunkId, int peerPort) {

    }


    private boolean registerPeer(IPeer peer) {
        boolean successfullyRegistered = false;


        return successfullyRegistered;
    }

    public static void main(String[] args) {
        int port = IPeer.getPort(args, "File owner port");
        LOGGER.info("[FileOwner] Starting File owner instance at port " + port);
        FileOwner fOwner = new FileOwner(port);
        LOGGER.info("[FileOwner] Listening for connections from peers at port:" + port);
        while (true) {
            Thread thread = null;
            try {
                thread = new Thread(new P2PConnection(fOwner.getServerSocket().accept(), fOwner));
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.start();
        }
    }

    @Override
    public List<Long> getFileChunkList() {
        return new ArrayList<>(partFilesMap.keySet());
    }

    @Override
    public void setFileChunkListWithSize(Map<Long, Long> fileOwnerFileChunkListWithSize) {
        //Do Nothing
    }


    @Override
    public Map<Long, Long> getFileChunkListWithSize() {
        return new HashMap<>(fileIdToSizeMap);
    }

    @Override
    public Path getStagingDir() {
        return stagingDir.toPath();
    }

    @Override
    public boolean addToFileChunkList(Long filePartId) {
        StringBuilder newFilePartPath = new StringBuilder();
        newFilePartPath.append(stagingDir.getPath())
                .append(File.separator)
                .append(filePartId)
                .append(IPeer.partFileSuffix);
        Object prevVal = partFilesMap.put(filePartId, Paths.get(newFilePartPath.toString()));
        return prevVal == null;
    }


    @Override
    public void setUploadNeighborPort(final int port) {

    }

    @Override
    public void setDownloadNeighborPort(final int port) {

    }

    @Override
    public List<Long> getRemainingFileChunkList() {
        return Collections.emptyList();
    }

    @Override
    public String getPeerId() {
        return "FileOwner";
    }

    @Override
    public void updateDownloadNeighborFileChunkListWithSize(Map<Long, Long> uploadNeighborFileChunkListWithSize) {
        //Do Nothing
    }

}
