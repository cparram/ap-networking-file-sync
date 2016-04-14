package filesync;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

/**
 * Created by cesar on 14-04-16.
 */
public class Client {

    @Option(name = "--port", usage="listening port", required = true)
    private int port;
    @Option(name = "--filename", usage = "server file name", required = true)
    private String fileName;
    @Option(name = "--block-size", usage = "Block size", required = true)
    private int blockSize;
    @Option(name = "--hostname", usage = "Hostname", required = true)
    private String hostName;


    /**
     * @param args
     */
    public static void main(String[] args) {
        Client client = new Client();

        if (client.config(args)) {
            client.start();
        }
    }

    private void start() {
        SynchronisedFile syncFile = null;
        try {
            syncFile = new SynchronisedFile(fileName, blockSize);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        SyncFileThread syncFileThread = new SyncFileThread(syncFile, blockSize, hostName, port);
        syncFileThread.start();

        while(true) {

            try {
                syncFile.CheckFileState();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }
    }

    private boolean config(String[] args) {
        boolean configured = false;
        CmdLineParser parser = new CmdLineParser(this);
        try {
            // parse the arguments.
            parser.parseArgument(args);
            configured = true;
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
        }
        return configured;
    }
}
