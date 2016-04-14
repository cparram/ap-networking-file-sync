package filesync;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by cesar on 14-04-16.
 */
public class Server {

    @Option(name = "--port", usage="listening port", required = true)
    private int port;
    @Option(name = "--filename", usage = "server file name", required = true)
    private String fileName;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();

        if (server.config(args)) {
            server.listen();
        }
    }

    private void listen() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept();
                Thread serverSync = new ServerSync(socket, fileName);
                serverSync.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
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
