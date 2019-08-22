import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;

public interface IntServer extends Remote {

    LinkedHashMap<Integer, String> getLeaderbord() throws RemoteException;

}