package PA1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer
{
	public static void main(String[] args) throws Exception
	{
		String clientMessage;
		String modifiedMessage;
		// create server socket that will listen to client sockets
		ServerSocket serverSocket = new ServerSocket(5555);

		System.out.println("Created socket at port: " + serverSocket.getLocalPort());

		while(true){

			System.out.println("Listening...");
			// create socket once a client socket wants to connect to the server socket
			// once that client socket is accepted, a new socket is created so that the server
			// can reply to the client
			Socket socketConnection = serverSocket.accept();
			System.out.println("Client socket has been accepted: " + socketConnection.isConnected());

			// this is a buffered reader that will get the input stream of data from the client socket
			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(socketConnection.getInputStream()));

			// this will create a data stream that will transmit data back to the client socket
			DataOutputStream outToClient = new DataOutputStream(socketConnection.getOutputStream());

			// this will save the input stream from the client socket to a string
			clientMessage = inFromClient.readLine();

			// modify the message
			modifiedMessage = clientMessage.toUpperCase()+'\n';

			// send the modified string back to the client
			outToClient.writeBytes(modifiedMessage);

			System.out.println(serverSocket.isClosed());
		}
	}
}
