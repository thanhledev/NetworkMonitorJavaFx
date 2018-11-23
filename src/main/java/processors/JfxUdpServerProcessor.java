package processors;

import de.uniba.ktr.Entities.Message;
import de.uniba.ktr.Processors.UdpServerProcessor;
import de.uniba.ktr.Libs.Helper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;

public class JfxUdpServerProcessor extends UdpServerProcessor {
    // properties
    private Queue shareQ;
    private int maxSize;

    // constructors
    public JfxUdpServerProcessor(DatagramSocket socket, Queue shareQ, int maxSize) {
        super(socket);
        this.shareQ = shareQ;
        this.maxSize = maxSize;
    }

    // overridden method
    @Override
    public void run() {
        this.owner = Thread.currentThread();
        System.out.println("Beginning listening for udp packets");
        try
        {
            while (true)
            {
                // simulate incoming datagram packet
                byte[] buffer = new byte[super.BUFFER_SIZE];
                DatagramPacket receivingPacket = new DatagramPacket(buffer, buffer.length);

                // receive the packet
                this.lSocket.receive(receivingPacket);

                // convert datagram packet to message
                Message message = Helper.streamToMessage(receivingPacket.getData());

                // check if queue is still having enough space
                synchronized (shareQ) {
                    while (shareQ.size() == maxSize) {
                        shareQ.wait();
                    }
                }

                shareQ.add(message);

                synchronized (shareQ) {
                    shareQ.notify();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(this.owner.getName() + " is interrupted!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void terminate() {
        this.owner.interrupt();
    }
}
