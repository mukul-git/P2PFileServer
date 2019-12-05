package edu.ufl.p2pFileserver;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class StreamSocket extends Socket {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private InputStream inStream;
    private OutputStream outStream;

    public StreamSocket(Socket socket) throws IOException {
        this.socket = socket;
        inStream = socket.getInputStream();
        input = new BufferedReader(new InputStreamReader(inStream));
        outStream = socket.getOutputStream();
        output = new PrintWriter(new OutputStreamWriter(outStream));
    }

    public StreamSocket(InetAddress acceptorHost, int acceptorPort) throws IOException {
        socket = new Socket(acceptorHost, acceptorPort);
        inStream = socket.getInputStream();
        input = new BufferedReader(new InputStreamReader(inStream));
        outStream = socket.getOutputStream();
        output = new PrintWriter(new OutputStreamWriter(outStream));
    }

    public void sendMessage(final String message) {
        output.print(message + "\n");
        output.flush();
    }

    public String receiveMessage() throws SocketException {
        String message = null;
        try {
            message = input.readLine();
        } catch (IOException e) {

            if (e instanceof SocketException)
                throw new SocketException(e.getMessage());
            else
                e.printStackTrace();
        }
        return message;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getFile(final File file, final int sizeInBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] fileBytes = new byte[1024];
            int recieveCount;
            int totalRecieved = 0;
            while (-1 != (recieveCount = inStream.read(fileBytes))) {
                fos.write(fileBytes, 0, recieveCount);
                //System.out.println("Server received " + recieveCount + " of " + file.getName());
                totalRecieved += recieveCount;
                if (totalRecieved == sizeInBytes)
                    break;
            }
            fos.flush();
            fos.close();
            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean sendFile(File file) {
        try {
            outStream.flush();
            //System.out.println("Sending file " + file.getAbsolutePath() + "...");
            byte[] fileBytes = new byte[1024];

            FileInputStream fis = new FileInputStream(file);
            int count;
            while (-1 != (count = fis.read(fileBytes, 0, fileBytes.length))) {
                outStream.write(fileBytes, 0, count);
                //System.out.println("Sent " + count);
                outStream.flush();
            }
            fis.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Socket getSocket() {
        return socket;
    }
}