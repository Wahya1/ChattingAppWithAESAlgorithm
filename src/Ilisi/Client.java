package Ilisi;

import javax.crypto.SecretKey;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;
import Ilisi.AESUtil;
import Ilisi.Interface;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Client {
    private static SecretKey aesKey;

    public static void main(String[] args) {
        try {
            // Connect to the server
            Interface server = (Interface) Naming.lookup("rmi://localhost:2020/Server");

            // Retrieve the AES key from the server
            String keyString = server.getAesKey();
            aesKey = new SecretKeySpec(Base64.getDecoder().decode(keyString), "AES");

            // Initialize scanner for user input
            Scanner scanner = new Scanner(System.in);

            // Get the username
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            // Start a thread to continuously receive messages
            new Thread(() -> {
                while (true) {
                    try {
                       
                        String messages = server.receiveMessages(username);
                        if (!messages.isEmpty()) {
                            System.out.println("Received messages:\n" + messages);
                        }
                        Thread.sleep(1000); 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

           
            while (true) {
                String recipient;

                
                while (true) {
                    System.out.print("Enter recipient: ");
                    recipient = scanner.nextLine();

                    if (server.doesUserExist(recipient)) {
                        break; 
                    } else {
                        System.out.println("User not found. Please try again.");
                    }
                }

                // Enter and send the message
                System.out.print("Enter your message: ");
                String message = scanner.nextLine();

                // Encrypt the message before sending
                String encryptedMessage = AESUtil.encrypt(message, aesKey);
                server.sendMessage(username, recipient, encryptedMessage);
                System.out.println("Message sent to " + recipient + ".");
            }

        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
}
