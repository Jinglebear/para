import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JABEServer {
	
    public static void main(String[] args) {
		
        Scanner scanner = new Scanner(System.in);
        Map<String,String> credentials = new HashMap<>();
        System.out.println("Enter new client credentials please: (<username> <password>) confirm each key value pair with enter..");
        System.out.println("If you finished your Input, please type 'DONE'");
        boolean read = true;
		
        while(read){	//Enter a new user with password
            try {
                String input = scanner.nextLine();
                if(input.equals("DONE")){
                    read = false;
                    scanner.close();
                }
                else{
                    String[] inputSplitted = input.split("\\s+");
                    if(inputSplitted.length != 2 || inputSplitted[0].length() <4 || inputSplitted[1].length() <4){
                        throw new IllegalArgumentException();
                    }
                    credentials.put(inputSplitted[0], inputSplitted[1]);
                }

            }catch (IllegalArgumentException e) {
                System.out.println("Wrong Input");
                System.out.println("Usernames and passwords should be at least 4 characters long");
            }catch (Exception e) {
                //TODO: handle exception
            }
        }	//end of while
		
		//Print out all added users with their passwords
        System.out.println("Entered credentials: ");
        for(Map.Entry<String,String> entry: credentials.entrySet()){
            System.out.println("Username: "+entry.getKey() +" Password: "+  entry.getValue());
        }	//end of for
		
        System.out.println("JABE is starting....");
        //starting the Naming service
		//JABEImpl runs on this main-Thread, is started from here
        try {
            Registry reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            JABEImpl jabeImpl = new JABEImpl(credentials);
            Naming.rebind("JABEImpl", jabeImpl);
            
        }catch (RemoteException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
