package edu.ufl.p2pFileserver;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.logging.Logger;


public class FileOwner implements IPeer {

    private final static Logger LOGGER = Logger.getLogger(FileOwner.class.getName());
    private final String suffix = ".part";

    private static FileOwner fileOwnerInstance;

    private long fileSize = 100 * 1024 * 1024;
    private File overseenFile;
    private List<Peer> connetedPeers;
    private Map<Long, Path> partFilesMap;
    private int kBPerSplit = 100;
    private String currentDirPath;
    private final File stagingDir;

    //private FileOwner(int portNumber, Runnable fileOwnerReadyNotifier) {
    public FileOwner(int portNumber) {
        partFilesMap = new HashMap<>();
        overseenFile = new File("test.pdf");
        currentDirPath = overseenFile.getAbsolutePath();
        stagingDir = new File(currentDirPath, "fOwner");
        CommonUtils.deleteDirectory(stagingDir);
        stagingDir.mkdirs();
        try {
            Files.copy(Paths.get(new URI(overseenFile.getAbsolutePath())), Paths.get(new URI(stagingDir.getAbsolutePath())));

            //this.fileOwnerReadyNotifier = fileOwnerReadyNotifier;

            //overseenFile.deleteOnExit(); //Register for file deletion on VM exit
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        if (!overseenFile.exists() || overseenFile.length() >= fileSize) {
            LOGGER.severe("File not found or is not of required size. Please add test.pdf file of atleast 1MB size.");
        } else {
            LOGGER.info("Starting File owner instance at port " + portNumber);
            LOGGER.info("Creating file partitions");
            try {
                partitionFile();
            } catch (Exception e) {
                LOGGER.severe("File split operation failed.");
            }

            //TODO: Start listeners for connection from peers


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
                writePartToFile(bytesPerSplit, position * bytesPerSplit, sourceChannel, partFiles);
            }

            if (remainingBytes > 0) {
                writePartToFile(remainingBytes, position * bytesPerSplit, sourceChannel, partFiles);
            }
        }
    }

    private void writePartToFile(long byteSize, long position, FileChannel sourceChannel, List<Path> partFiles) throws IOException {
        Path fileName = Paths.get(currentDirPath + position + suffix);
        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFilesMap.put(position, fileName);
    }

    private void sendFileChunk(int chunkId, int peerPort) {

    }


    private boolean registerPeer(IPeer peer) {
        boolean successfullyRegistered = false;


        return successfullyRegistered;
    }

    public static void main(String[] args) {
        int port = IPeer.getPort(args, "File owner port");
        FileOwner fOwner = new FileOwner(port);
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


//    public static FileOwner startInstance(int portNumber, Runnable fileOwnerReadyNotifier) {
//        if (fileOwnerInstance == null) {
//            synchronized (FileOwner.class) {
//                if (fileOwnerInstance == null) {
//                    fileOwnerInstance = new FileOwner(portNumber, fileOwnerReadyNotifier);
//                }
//            }
//        }
//        return fileOwnerInstance;
//    }


//    private Runnable fillFile = new Runnable() {
//        @Override
//        public void run() {
//            int i = 0;
//            PrintWriter pw = null;
//            try {
//                FileWriter fw = new FileWriter(overseenFile);
//                pw = new PrintWriter(fw);
//
//                while (overseenFile.length() < fileSize) {
//                    for (int cnt = 0; cnt < 100; ++i, ++cnt) {
//                        try {
//                            pw.printf("%d is the line number.$", i);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (pw != null)
//                    pw.close();
//            }
//        }
//    };

//From https://www.baeldung.com/java-write-to-file

    //    @Test
//    public void whenTryToLockFile_thenItShouldBeLocked()
//            throws IOException {
//        RandomAccessFile stream = new RandomAccessFile(fileName, "rw");
//        FileChannel channel = stream.getChannel();
//
//        FileLock lock = null;
//        try {
//            lock = channel.tryLock();
//        } catch (final OverlappingFileLockException e) {
//            stream.close();
//            channel.close();
//        }
//        stream.writeChars("test lock");
//        lock.release();
//
//        stream.close();
//        channel.close();
//    }


}
