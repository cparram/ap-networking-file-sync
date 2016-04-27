package filesync;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by cesar on 14-04-16.
 */
public class SyncFileThread  extends Thread {
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
            sendToServer(direction, outStream);
            sendToServer(syncFile.getFilename(), outStream);
            if (getServerMsg(inStream).matches("BLOCKSIZE_OK$")) {
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
     * @param msg String message that will be sent.
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

