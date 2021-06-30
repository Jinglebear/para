import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface JABEInterface extends Remote{
    public void login(String username, String password) throws RemoteException, JABEException;
    public void offer(String username,String itemName,int minPrice, int maxDuration) throws RemoteException, JABEException;
    public List<JABEItem> listAuctinsOfUser(String username,boolean all) throws RemoteException, JABEException;
    public void bid(String username,String itemID, int bid) throws RemoteException, JABEException;
    public void observe(JABEMonitorInterface monitorInterface) throws RemoteException;
    public void removeObserver(JABEMonitorInterface monitorInterface) throws RemoteException;
    public void logout(String username) throws RemoteException, JABEException;
}