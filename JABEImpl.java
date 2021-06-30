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

	//user is logged out if he quit the Application (JABEClient); removes user from list of logged in users
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
		//sets the offer for an item, starts the timer; auction automatically ends after time expires
        boolean success = false;
        if (loggedInUsers.contains(username)) {
            JABEItem item = new JABEItem(itemName);
            item.setPrice(minPrice);
            this.offers.get(username).add(item);
            success = true;
            MyTask myTask = new MyTask(maxDuration, this, item.getID(), username);		//uses class MyTask
            Timer timer = new Timer("MyTimer");
            timer.schedule(myTask, 0);
        } else {
            System.out.println(username + "tried to offer an item (but was not logged in)");
        }
        return success;
    }

	//Updates item list when the Item with ID itemID from Seller seller has expired
    public synchronized void updateItemList(String itemID, String seller) {
		
        boolean search = true;
        for (int i = 0; i < this.offers.get(seller).size() && search; ++i) {
            if (this.offers.get(seller).get(i).getID().equals(itemID)) {
                String highestBidder = this.currentHighestBidder.get(itemID);
                this.currentHighestBidder.remove(itemID);
				
				//prints out message on the Server:
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
    public synchronized List<JABEItem> listAuctionsOfUser(String username) throws RemoteException {
		
        for (Map.Entry<String, ArrayList<JABEItem>> entry : this.offers.entrySet()) {
            if (entry.getKey().equals(username)) {
                return entry.getValue();
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
                        if (bid > itemInList.getPrice()) {	//If item was found and new bid is higher, set new highest bidder (and return true)
                            itemInList.setPrice(bid);
                            this.currentHighestBidder.put(itemInList.getID(), username);
                            success = true;
                        }
                    }
                }
            }
        }
        return success;
    }

    @Override
    public void observe(String ItemID, String username) throws RemoteException {
        // TODO Auto-generated method stub

    }

}
