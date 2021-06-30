import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JABEMonitorInterface extends Remote{
    public void alertOnHigherBid(String ItemID)throws RemoteException;
}
