import java.io.Serializable;

public class JABEItem implements Serializable{
    private static int idCounter = 0; // static counter
    private String name; // name of the item
    private final String ID; // unique ID
    private int price; // price of the item
    public JABEItem(String name){
        this.name = name;
        this.ID = createID();
    }
    /**
     * Create unique ID so servers can identify the Item
     * @return
     */
    public static synchronized String createID() {
        return String.valueOf(idCounter++);
    }
    @Override
    public String toString() {
        return "Name: "+getName()+" ID: "+getID()+ " Current price: "+ getPrice();
    }
    /**
     * getter for Name, used in notify
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * getter for ID
     * @return
     */
    public String getID() {
        return ID;
    }
    /**
     * getter for price of the item
     * @return
     */
    public synchronized int getPrice() {
        return price;
    }
    /**
     * setter for changing the price of the item
     * e.g. by a bid 
     * @param price
     */
    public synchronized void setPrice(int price) {
        this.price = price;
    }
}
