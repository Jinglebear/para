import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JABEMonitorInterface extends Remote{
    public void alertOnHigherBid(String itemID)throws RemoteException;
    public void alertOnEndingAuction(String itemID)throws RemoteException;
}
