package filesync;

import java.io.*;
import java.net.Socket;

/**
 * Created by cesar on 14-04-16.
 */
public class SyncFileThread extends Thread {
    private final int blockSize;
    private final String hostName;
    private final int port;
    private SynchronisedFile syncFile;
    private final String direction;

    public SyncFileThread(SynchronisedFile syncFile, int blockSize, String hostName, int port, String direction) {
        this.syncFile = syncFile;
        this.blockSize = blockSize;
        this.hostName = hostName;
        this.port = port;
        this.direction = direction;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostName, port);
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendToServer("BLOCKSIZE " + blockSize, outStream);
            if (getServerMsg(inStream).matches("BLOCKSIZE_OK$")) {
                sendToServer(direction, outStream);
                if (direction.equals("pull")) {
                    String message;
                    message = getServerMsg(inStream);
                    FileOutputStream outputStream;
                    try {
                         outputStream = new FileOutputStream(syncFile.getFilename());
                        while (!message.equals("end")){
                            outputStream.write(message.getBytes());
                            message = getServerMsg(inStream);
                        }
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(direction.equals("push")){
                    FileInputStream clientFile;
                    try {
                        clientFile = new FileInputStream(syncFile.getFilename());
                        byte[] buffer = new byte[1024];
                        while (clientFile.read(buffer) != -1) {
                            sendToServer(new String(buffer), outStream);
                        }
                        sendToServer("end", outStream);
                        clientFile.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else{
                    System.out.println("BAD DIRECTION. Use pull or push option");
                }
                Instruction inst;
                while ((inst = syncFile.NextInstruction()) != null) {
                    sendToServer("INST " + inst.ToJSON(), outStream);
                    String response = getServerMsg(inStream);
                    /* nothing to do when server have problem to process the instruction
                    if (response.matches("[INST_NOT_PROCESSED]")) {
                    } else if (response.matches("[INST_PROCESSED]")) {
                    }*/
                    if (response.matches("NEW_BLOCK_INST_WAS_REQUIRED$")) {
                        Instruction upgraded = new NewBlockInstruction((CopyBlockInstruction) inst);
                        sendToServer("INST " + upgraded.ToJSON(), outStream);
                        // to any response from server, the client will send the next instruction
                        getServerMsg(inStream);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Gets responses from server. It will waits until server sends a response
     *
     * @param inStream Input
     * @return String message from server
     */
    private String getServerMsg(BufferedReader inStream) {
        String response = "";
        try {
            response = inStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("SyncFileThread#getServerMsg: " + response);
        return response;
    }

    /**
     * Sends message to server.
     *
     * @param msg       String message that will be sent.
     * @param outStream Output stream
     */
    private void sendToServer(String msg, DataOutputStream outStream) {
        System.out.println("SyncFileThread#sendToServer: " + msg);
        try {
            outStream.writeBytes(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}

