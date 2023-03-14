package PA1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPClient
{

	public static void main(String[] args) throws Exception
	{
		String userInput, modifiedServerOutput;
		// Create a socket at port 5555
		Socket clientSocket = new Socket("localhost",5555);

		System.out.println("Creating a socket at port: " + clientSocket.getLocalPort());

		// Create an input stream that will read data from console
		BufferedReader inFromUser = new BufferedReader(
				new InputStreamReader(System.in));

		// create an output stream so that the client socket can send the data to the server socket
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());

		// create reader that will recieve data from the server
		BufferedReader inFromServer = new BufferedReader (
				new InputStreamReader(clientSocket.getInputStream()));

		// read user input
		System.out.print("Message: ");
		userInput = inFromUser.readLine();


		// send the user input to the server
		outToServer.writeBytes(userInput + '\n');

		// read the modified response from the server
		modifiedServerOutput = inFromServer.readLine();

		System.out.println(modifiedServerOutput);

		clientSocket.close();
//		System.out.println(clientSocket.isClosed());






	}
}
