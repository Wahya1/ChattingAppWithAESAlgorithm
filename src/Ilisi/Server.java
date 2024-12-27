package Ilisi;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends UnicastRemoteObject implements Interface {
    private final Map<String, List<String>> userMessages = new HashMap<>();
    private final List<String> connectedUsers = new ArrayList<>();
    private final SecretKey aesKey;

    public Server() throws RemoteException {
        super();
        try {
            this.aesKey = AESUtil.generateKey(); // Generate the AES key
        } catch (Exception e) {
            throw new RemoteException("Error generating AES key", e);
        }
    }

    @Override
    public void sendMessage(String sender, String recipient, String encryptedMessage) throws RemoteException {
        try {
            String decryptedMessage = AESUtil.decrypt(encryptedMessage, this.aesKey);
            String formattedMessage = sender + ": " + decryptedMessage;

            // Store the message for the recipient
            userMessages.putIfAbsent(recipient, new ArrayList<>());
            userMessages.get(recipient).add(formattedMessage);

            // Log the sent message
            System.out.println("Message sent from " + sender + " to " + recipient + ":");
            System.out.println("- Encrypted message: " + encryptedMessage);
          
        } catch (Exception e) {
            throw new RemoteException("Error decrypting the message", e);
        }
    }

    @Override
    public boolean doesUserExist(String username) throws RemoteException {
        return connectedUsers.contains(username);
    }

    @Override
    public String receiveMessages(String username) throws RemoteException {
        // Register new user if not already registered
        if (!connectedUsers.contains(username)) {
            connectedUsers.add(username);
            System.out.println("New user registered: " + username);
        }

        List<String> messages = userMessages.getOrDefault(username, new ArrayList<>());
        // Clear messages after retrieval
        userMessages.put(username, new ArrayList<>()); 
        return String.join("\n", messages);
    }

    @Override
    public String getAesKey() throws RemoteException {
        return Base64.getEncoder().encodeToString(aesKey.getEncoded());
    }

    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(2020);
            Server server = new Server();
            java.rmi.Naming.rebind("rmi://localhost:2020/Server", server);
            System.out.println("Chat server ready.");
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
