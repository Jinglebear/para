import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class JABEMonitorImpl extends UnicastRemoteObject implements JABEMonitorInterface{

    protected JABEMonitorImpl() throws RemoteException {
        super();
    }

    @Override
    public void alertOnHigherBid(String itemID) throws RemoteException {
        System.out.println("New Bid on item: "+itemID);
        
    }

    @Override
    public void alertOnEndingAuction(String itemID) throws RemoteException {
        System.out.println("Auction for item: "+itemID+" ended");
        
    }

    
    
}
