package processors;

import de.uniba.ktr.Processors.TcpServerProcessor;
import de.uniba.ktr.Entities.Message;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;

public class JfxTcpServerProcessor extends TcpServerProcessor {

    // properties
    private Queue shareQ;
    private int maxSize;

    // constructors
    public JfxTcpServerProcessor(Socket socket, Queue shareQ, int maxSize) {
        super(socket);
        this.shareQ = shareQ;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        this.owner = Thread.currentThread();
        System.out.println("Beginning listening for tcp packets");

        // each instance of this TcpServerProcessor will serve exactly one TCP connection
        try {
            ObjectOutputStream out = new ObjectOutputStream(lSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(lSocket.getInputStream()));

            // convert stream into message
            Message message = (Message) in.readObject();

            // check if the queue is still having enough space
            synchronized (shareQ) {
                while (shareQ.size() == maxSize) {
                    shareQ.wait();
                }
            }

            shareQ.add(message);

            synchronized (shareQ) {
                shareQ.notify();
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate() {
        this.owner.interrupt();
    }
}
