import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class JABEClient {
    private String username;

    public String getUsername() {
        return username;
    }

    public JABEClient(String username) {
        this.username = username;
    }

    private void login(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        System.out.println("Enter password:");
        String password = scanner.nextLine();
        if (jabeInterface.login(getUsername(), password)) {
            System.out.println("Logged in successfully");
        } else {
            System.out.println("Login failed!");
        }
    }

    private void printActions() {
        System.out.println("Available Actions: (Enter the appropriate number)");
        System.out.println("1: Offer (offer an item and determine the min price and max duration (seconds)");
        System.out.println("2: List (all items of a certain user can be listed)");
        System.out.println("3: Bid (bid for an item (identifyed by item ID))");
        System.out.println("4: Observe (Switch the client into observer-mode)");
        System.out.println("5: Quit Observer Mode");
        System.out.println("0: Exit the Client \n");

    }

    private void listItems(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        System.out.println("Enter 0 for all items or 1 for entering specific username: <0> / <1>");
        String in = scanner.nextLine();
        int choice = Integer.parseInt(in);
        if (choice == 1) {
            System.out.println("Enter username:");
            String username = scanner.nextLine();
            List<JABEItem> items = jabeInterface.listAuctinsOfUser(username, false);
            if (items != null) {
                if (!items.isEmpty()) {
                    for (JABEItem item : items) {
                        System.out.println(item.toString());
                    }
                } else {
                    System.out.println("This user does not have Items listed yet");
                }
            } else {
                System.out.println("No items could be found");
            }
        } else if (choice == 0) {
            List<JABEItem> items = jabeInterface.listAuctinsOfUser(null, true);
            for (JABEItem item : items) {
                System.out.println(item.toString());
            }
        }
    }

    private void offerItem(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        System.out.println("Enter Item : <name> <minPrice> <maxDuration>");
        String itemInput = scanner.nextLine();
        String[] input = itemInput.split("\\s+");
        try {
            if (input.length != 3) {
                throw new IllegalArgumentException();
            }
            String itemName = input[0];
            int minPrice = Integer.parseInt(input[1]);
            int maxDuration = Integer.parseInt(input[2]);
            if (jabeInterface.offer(getUsername(), itemName, minPrice, maxDuration)) {
                System.out.println("Successfully placed Item: " + itemName + " " + minPrice + " " + maxDuration);
            } else {
                System.out.println("Could not place the item successfully");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Wrong Input..");
        }
    }

    private void bidOnItem(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        System.out.println("Enter bid: <ItemID> <bid(int)>");
        String bidInput = scanner.nextLine();
        String[] input = bidInput.split("\\s+");
        try {
            if (input.length != 2) {
                throw new IllegalArgumentException();
            }
            String id = input[0];
            int bid = Integer.parseInt(input[1]);
            if (jabeInterface.bid(getUsername(), id, bid)) {
                System.out.println("Successfully placed a bid on the item");
            } else {
                System.out.println("Could not place a bid on the item");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Wrong Input..");
        }
    }
    JABEMonitorImpl monitor;
    private void observe(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        System.out.println("Entering observer mode");

        monitor = new JABEMonitorImpl();
        jabeInterface.observe(monitor);

    }
    private void quitObserve(JABEInterface jabeInterface,Scanner scanner)throws RemoteException{
        System.out.println("Quitting observer mode");
        jabeInterface.removeObserver(monitor);
    }
    private void actionLoop(JABEInterface jabeInterface, Scanner scanner) throws RemoteException {
        boolean action = true;
        printActions();
        while (action) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                switch (input) {
                    case 1:
                        offerItem(jabeInterface, scanner);
                        printActions();
                        break;
                    case 2:
                        listItems(jabeInterface, scanner);
                        printActions();
                        break;
                    case 3:
                        bidOnItem(jabeInterface, scanner);
                        printActions();
                        break;
                    case 4:
                        observe(jabeInterface, scanner);
                        printActions();
                        break;
                    case 5:
                        quitObserve(jabeInterface, scanner);
                        printActions();
                        break;
                    case 0:
                        System.out.println("Quitting...");
                        action = false;
                        jabeInterface.logout(getUsername());
                        break;
                    default:
                        throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong Input..");
                jabeInterface.logout(getUsername());
            }
        }
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter Username:");
            String username = scanner.nextLine();
            JABEClient client = new JABEClient(username);
            JABEInterface jabeInterface = (JABEInterface) Naming.lookup("//localhost:1099/JABEImpl");
            client.login(jabeInterface, scanner);
            client.actionLoop(jabeInterface, scanner);
            jabeInterface.logout(client.getUsername());
            scanner.close();

        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
