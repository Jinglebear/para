import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface JABEInterface extends Remote{
    public boolean login(String username, String password) throws RemoteException;
    public boolean offer(String username,String itemName,int minPrice, int maxDuration) throws RemoteException;
    public List<JABEItem> listAuctinsOfUser(String username) throws RemoteException;
    public boolean bid(String username,String itemID, int bid) throws RemoteException;
    public void observe(String itemID,String username) throws RemoteException;
    public boolean logout(String username) throws RemoteException;
}