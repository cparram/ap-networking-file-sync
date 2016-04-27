package filesync;

import java.io.*;
import java.net.Socket;

/**
 * Created by cesar on 14-04-16.
 */
public class ServerSync extends Thread {
    private Socket socket;
    private String fileName;

    public ServerSync(Socket socket, String fileName) {
        this.socket = socket;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        DataOutputStream output = null;
        BufferedReader input = null;
        try {
            output = new DataOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get block size from client
        String blockSizeMsg = getClientMsg(input);
        if (blockSizeMsg.matches("BLOCKSIZE\\s\\d+$")) {
            int blockSize = Integer.valueOf(blockSizeMsg.split(" ")[1]);
            SynchronisedFile syncFile = null;
            try {
                syncFile = new SynchronisedFile(fileName, blockSize);
                sendToClient("BLOCKSIZE_OK", output);
            } catch (IOException e) {
                e.printStackTrace();
                sendToClient("BLOCKSIZE_FAILED", output);
            }
            String direction = getClientMsg(input);
            String clientFileName = getClientMsg(input);
            if (direction.equals("pull")) {
                FileInputStream serverFile;
                FileOutputStream newClientFile;
                try {
                    serverFile = new FileInputStream(fileName);
                    newClientFile = new FileOutputStream(clientFileName);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = serverFile.read(buffer)) > 0) {
                        newClientFile.write(buffer, 0, length);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (direction.equals("push")) {
                FileInputStream clientFile;
                FileOutputStream newServerFile;
                try {
                    clientFile = new FileInputStream(clientFileName);
                    newServerFile = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = clientFile.read(buffer)) > 0) {
                        newServerFile.write(buffer, 0, length);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String instMsg = getClientMsg(input);
            while (instMsg.matches("^(INST)\\s.+")) {
                InstructionFactory instFact = new InstructionFactory();
                Instruction receivedInst = instFact.FromJSON(instMsg.split("^(INST)\\s")[1]);

                if (receivedInst == null) { // if the instruction isn't valid
                    sendToClient("INST_FAILED", output);
                } else { // if the instruction is valid
                    try {
                        syncFile.ProcessInstruction(receivedInst);
                        sendToClient("INST_PROCESSED", output);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendToClient("INST_FAILED", output);
                    } catch (BlockUnavailableException e) {
                        e.printStackTrace();
                        sendToClient("NEW_BLOCK_INST_WAS_REQUIRED", output);
                        String instMsgUpdated = getClientMsg(input);
                        if (instMsgUpdated.matches("^(INST)\\s.+")) {
                            Instruction receivedInst2 = instFact.FromJSON(instMsgUpdated.split("^(INST)\\s")[1]);
                            if (receivedInst2 == null) { // if the instruction isn't valid
                                sendToClient("INST_FAILED", output);
                            } else { // if the instruction is valid
                                try {
                                    syncFile.ProcessInstruction(receivedInst2);
                                    sendToClient("INST_PROCESSED", output);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    sendToClient("INST_NOT_PROCESSED", output);
                                } catch (BlockUnavailableException e1) {
                                    e1.printStackTrace();
                                    sendToClient("INST_NOT_PROCESSED", output);
                                }
                            }
                        } else {
                            sendToClient("INST_WAS_REQUIRED2", output);
                        }
                    }
                }
                instMsg = getClientMsg(input);
            }
        } else { // if first msg isn't the block size
            sendToClient("BLOCKSIZE_WAS_REQUIRED", output);
        }
        // ToDO: close socket
    }

    /**
     * Sends messages to client.
     *
     * @param msg    Message that will be sent
     * @param output Data output stream
     */
    private void sendToClient(String msg, DataOutputStream output) {
        System.out.println("ServerSync#sentToClient: " + msg);
        try {
            output.writeBytes(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets client message. It will wait until client sends a message
     *
     * @param input Reader
     * @return String message from client
     */
    private String getClientMsg(BufferedReader input) {
        String msg = "";
        try {
            msg = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ServerSync#getClientMsg: " + msg);
        return msg;
    }
}
