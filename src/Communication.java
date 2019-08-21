import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Communication {
    //util. method from previous task...
    public byte[] readFully(int bytesToRead, SocketChannel client) {
        //implement this method
        int readBytes = 0;
        ByteBuffer inBB = ByteBuffer.allocate(bytesToRead);

        while (readBytes < bytesToRead) {
            try {
                //read available bytes...
                readBytes += client.read(inBB);
            } catch (ClosedChannelException e) {
                System.out.println("The socket has been closed; returning data read so far (" + readBytes + " bytes). Error was: " + e);
                return inBB.array();
            } catch (java.io.IOException e) {
                System.out.println("The socket has caused an I/O exception; returning data read so far (" + readBytes + " bytes). Error was: " + e);
                return inBB.array();
            }
        }

        return inBB.array();
    }

    public void sendBytes(int type,SocketChannel client) {
        byte[] bytes = new byte[2];
        sendBytes(type,client, bytes);
    }

    public void sendBytes(int type,SocketChannel client,byte[] bytes) {
        try {
            //first get payload as bytes, so we know its length in advance...
            byte[] payload = bytes;

            //construct message to send
            byte[] msg = new byte[3 + payload.length];
            msg[0] = 1;

            //set the message type
            msg[1] = (byte) type;

            //payload length
            msg[2] = (byte) payload.length;

            //copy the payload into the message
            System.arraycopy(payload, 0, msg, 3, payload.length);

            ByteBuffer bb = ByteBuffer.allocate(msg.length);
            bb.clear();
            bb.put(msg);
            bb.rewind();
            client.write(bb);
        } catch (Exception e) {
            System.out.println("Error in bytes: " + e);
        }
    }


    public ArrayList<String> bytesToArraylist(byte[] bytes) throws IOException {
        ArrayList<String> read = new ArrayList<String>();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);
        while (in.available() > 0) {
            String element = in.readUTF();
            read.add(element);
        }
        return read;
    }

    public byte[] arraylistToBytes(ArrayList<String> list) throws IOException {
        // write to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (String element : list) {
            out.writeUTF(element);
        }
        return baos.toByteArray();
    }

    public String getString(byte[] bytes){
        String str = null;
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}
