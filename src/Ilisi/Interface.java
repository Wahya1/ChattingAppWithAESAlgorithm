package Ilisi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Interface extends Remote {
	void sendMessage(String sender, String recipient, String encryptedMessage) throws RemoteException;
    String receiveMessages(String username) throws RemoteException;
    String getAesKey() throws RemoteException;
    boolean doesUserExist(String username) throws Exception;
}
