import java.rmi.RemoteException;
import java.util.TimerTask;

public class MyTask extends TimerTask {
    private int maxDuration;
    private JABEImpl jabeImpl;
    private String itemID;
    private String seller;

    public MyTask(int maxDuration,JABEImpl jabeImpl,String itemID,String seller){
        super();
        this.maxDuration = maxDuration;
        this.jabeImpl = jabeImpl;
        this.itemID = itemID;
        this.seller = seller;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(maxDuration*1000);
            try {
                jabeImpl.updateItemList(itemID,seller);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            cancel();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }      
    }
    
}
