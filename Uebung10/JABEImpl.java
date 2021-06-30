import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class JABEImpl extends UnicastRemoteObject implements JABEInterface {
    List<String> loggedInUsers; // list of logged in clients
    Map<String, String> loginCredentials; // map of (username,password) login credentials
    Map<String, ArrayList<JABEItem>> offers; // map of clients and their offers
    Map<String, String> currentHighestBidder; // map of the itemsIDs and the belonging highest bidder

    protected JABEImpl(Map<String, String> loginCredentials) throws RemoteException {
        super();
        this.loginCredentials = loginCredentials;
        this.loggedInUsers = new ArrayList<>();
        this.offers = new HashMap<>();
        this.currentHighestBidder = new HashMap<>();
        this.observingClients = new ArrayList<>();
        this.listOfConcern = new HashMap<>();
    }

    /**
     * login Method
     * 
     * @return true (if login was successfull), false (if login was not successfull)
     */
    public boolean login(String username, String password) throws RemoteException {
        boolean success = false;
        for (Map.Entry<String, String> entry : this.loginCredentials.entrySet()) {
            if (entry.getKey().equals(username) && entry.getValue().equals(password)) {
                this.loggedInUsers.add(username);
                success = true;
            }
        }
        if (success) {
            ArrayList<JABEItem> itemArr = new ArrayList<>();
            offers.put(username, itemArr);
        }
        return success;
    }

    public boolean logout(String username) throws RemoteException {
        boolean success = false;
        if (this.loggedInUsers.contains(username)) {
            this.loggedInUsers.remove(username);
            success = true;
        }

        return success;
    }

    /**
     * offer Method make a new Item offer list item in offers map list item in
     * timeList map
     */
    public boolean offer(String username, String itemName, int minPrice, int maxDuration) throws RemoteException {
        boolean success = false;
        if (loggedInUsers.contains(username)) {
            JABEItem item = new JABEItem(itemName);
            item.setPrice(minPrice);
            this.offers.get(username).add(item);
            success = true;
            MyTask myTask = new MyTask(maxDuration, this, item.getID(), username);
            Timer timer = new Timer("MyTimer");
            timer.schedule(myTask, 0);
            if(this.listOfConcern.containsKey(username)){
                this.listOfConcern.get(username).add(item.getID());
            }
            else{
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(item.getID());
                this.listOfConcern.put(username, tmp);
            }
        } else {
            System.out.println(username + "tried to offer an item (but was not logged in)");
        }
        return success;
    }

    public synchronized void updateItemList(String itemID, String seller) {
        boolean search = true;
        for (int i = 0; i < this.offers.get(seller).size() && search; ++i) {
            if (this.offers.get(seller).get(i).getID().equals(itemID)) {
                String highestBidder = this.currentHighestBidder.get(itemID);
                this.currentHighestBidder.remove(itemID);
                System.out.println("Item offer " + this.offers.get(seller).get(i).getName() + " expired and "
                        + highestBidder + " was the highest Bidder!");
                this.offers.get(seller).remove(i);
                search = false;
            }
        }
    }

    /**
     * list method if username fits to one client in offers map the belonging offer
     * list is returned
     */
    public synchronized List<JABEItem> listAuctinsOfUser(String username,boolean all) throws RemoteException {
        if(all){
            ArrayList<JABEItem> result = new ArrayList<>();
            for(Map.Entry<String,ArrayList<JABEItem>> entry: this.offers.entrySet()){
                for(JABEItem item : entry.getValue()){
                    result.add(item);
                }
            }
            return result;
        }
        else{
            for (Map.Entry<String, ArrayList<JABEItem>> entry : this.offers.entrySet()) {
                if (entry.getKey().equals(username)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * bidding method if client is logged in and the itemID is valid if bid is
     * higher than current bid (price) bid is placed, bidder is listed in
     * currentHighestBidder Map
     * 
     */
    @Override
    public synchronized boolean bid(String username, String itemID, int bid) throws RemoteException {
        boolean success = false;
        if (this.loggedInUsers.contains(username)) {
            for (ArrayList<JABEItem> list : this.offers.values()) {
                for (JABEItem itemInList : list) {
                    if (itemInList.getID().equals(itemID)) {
                        if (bid > itemInList.getPrice()) {
                            itemInList.setPrice(bid);
                            this.currentHighestBidder.put(itemInList.getID(), username);
                            success = true;
                            if(this.listOfConcern.containsKey(username)){
                                this.listOfConcern.get(username).add(itemID);
                            }
                            else{
                                ArrayList<String> tmp = new ArrayList<>();
                                tmp.add(itemID);
                                this.listOfConcern.put(username, tmp);
                            }
                            for(JABEMonitorInterface jabeMonitorInterface : this.observingClients){
                                for(Map.Entry<String,ArrayList<String>> entry : this.listOfConcern.entrySet()){
                                    if(entry.getValue().contains(itemID)){
                                        jabeMonitorInterface.alertOnHigherBid(itemID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return success;
    }
    Map<String,ArrayList<String>> listOfConcern;
    @Override
    public synchronized void observe(JABEMonitorInterface monitorInterface) throws RemoteException {
        observingClients.add(monitorInterface);
    }
    @Override
    public synchronized void removeObserver(JABEMonitorInterface monitorInterface)throws RemoteException{
        observingClients.remove(monitorInterface);
    }
    List<JABEMonitorInterface> observingClients;

}
