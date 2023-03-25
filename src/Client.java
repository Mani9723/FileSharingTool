import java.io.*;
import java.net.Socket;
import java.util.Arrays;


/**
 * This class handles the client side of
 * the programming assignment 1
 *
 * @author Mani Shah, mshah22, G00974705
 */
public class Client
{
	private final static String UPLOAD_CMD = "java -cp pa1.jar client upload <path_on_client> </path/filename/on/server>";
	private final static String DOWNLD_CMD = "java -cp pa1.jar client download </path/existing_filename/on/server> <path_on_client>";
	private final static String DIR_CMD = "java -cp pa1.jar client dir </path/existing_directory/on/server>";
	private final static String MKDIR_CMD = "java -cp pa1.jar client mkdir </path/new_directory/on/server>";
	private final static String RMDIR_CMD = "java -cp pa1.jar client rmdir </path/existing_directory/on/server>";
	private final static String RM_CMD = "java -cp pa1.jar client rm </path/existing_filename/on/server>";
	private final static String SHUTDOWN_CMD = "java -cp pa1.jar client shutdown";
	private final static int BUFFER_SIZE = 4*1024;

	private static Socket socket;
	private static DataOutputStream dataOutputStream;
	private static DataInputStream dataInputStream;


	public Client(String[] args)
	{
		try {
			socket = new Socket("localhost", 8765);
			processArgs(args);
		} catch (IOException e) {
			System.out.println("Can't connect to the server");
			e.printStackTrace();
		}finally {
			try {
				flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Exiting");

	}

	/**
	 * Converts the array to a string
	 * @param args Arguments
	 * @return String representation
	 */
	private String conformCmd(String[] args)
	{
		return Arrays.toString(args).trim()
				.replace("[","")
				.replace("]","")
				.replace(",","");
	}

	/**
	 * Processes the command line arguments from the client
	 * and sends it to the server
	 * @param args String[] arguments
	 */
	private void processArgs(String[] args) throws IOException
	{
		String action = args[0];
		int len = args.length;
		switch(action){
			case "shutdown":
				sendShutdownCmd(args, len);
				break;
			case "upload":
				sendUploadCmd(args,len);
				break;
			case "download":
				sendDownloadCmd(args,len);
				break;
			case "dir":
				sendDirCmd(args,len);
				break;
			case "mkdir":
				sendMkdirCmd(args,len);
				break;
			case "rmdir":
				sendRmdirCmd(args,len);
				break;
			case "rm":
				sendRmCmd(args,len);
				break;
			default:
				System.out.println("Valid actions: upload, download," +
						"dir, mkdir, rmdir, rm, shutdown");
				break;
		}
	}

	/**
	 * Sends rm command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendRmCmd(String[] args, int len) throws IOException
	{
		if(len == 2) {
			// remove a file from the server
			sendToServer(conformCmd(args));
			boolean response = Boolean.parseBoolean(dataInputStream.readUTF());
			if(response){
				System.out.println("Removed File: "+ args[1]);
			}else{
				System.err.println("Error removing file: " + args[1]);
			}
			flush();
		}else{
			System.out.println(RM_CMD);
		}
	}

	/**
	 * Sends rmdir command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendRmdirCmd(String[] args, int len) throws IOException
	{
		if(len == 2) {
			// remove a directory from the server
			sendToServer(conformCmd(args));
			boolean response = Boolean.parseBoolean(dataInputStream.readUTF());
			if(response){
				System.out.println("Removed Directory: " + args[1]);
			}else{
				System.err.println("Error removing directory: " + args[1]);
			}
			flush();
		}else{
			System.out.println(RMDIR_CMD);
		}
	}

	/**
	 * Sends mkdir command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendMkdirCmd(String[] args, int len) throws IOException
	{
		if(len == 2) {
			// create a new directory in the server
			sendToServer(conformCmd(args));
			boolean response = Boolean.parseBoolean(dataInputStream.readUTF());
			if(response){
				System.out.println("Created directory: " + args[1]);
			}else{
				System.err.println("Error creating directory at: " + args[1]);
			}
			flush();
		}else{
			System.out.println(MKDIR_CMD);
		}
	}

	/**
	 * Sends dir command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendDirCmd(String[] args, int len) throws IOException
	{
		if(len == 2){
			// print contents of server directory
			sendToServer(conformCmd(args));
			System.out.println("after sending command");
			String response = dataInputStream.readUTF();
			System.out.println(response);
			if(!response.isEmpty()) {
				System.out.println(response);
			}else{
				System.out.println("Empty Directory");
			}
			flush();
		}else{
			System.out.println(DIR_CMD);
		}
	}

	/**
	 * Sends download command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendDownloadCmd(String[] args, int len) throws IOException
	{
		if(len == 3) {
			// download file from the server
			File pathOnClient = new File(args[2]);
			if(pathOnClient.isDirectory() && !pathOnClient.isFile()) {
				sendToServer(conformCmd(args));
				receiveFileFromServer(pathOnClient);
			}else{
				System.err.println("Destination a not directory: "+args[2]);
			}

			//TODO finish download
			flush();
		}else{
			System.out.println(DOWNLD_CMD);
		}
	}

	private void receiveFileFromServer(File dest) throws IOException
	{
		int bytes = 0;
		long size = dataInputStream.readLong();
		long resumeUploadFrom = 0, uploadedSoFar = 0;
		long fixedSize = size;
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean resumingUpload = false, reachedUploadPoint = false, readNextBuffer = false;;

		FileOutputStream fileOutputStream;
		boolean result = dest.createNewFile();
		if(!result && dest.length() < size) {
			resumingUpload = true;
			resumeUploadFrom = dest.length();
		}
		fileOutputStream = new FileOutputStream(dest, resumingUpload);
		System.out.print("Downloading File...");
		while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size)))
				!= -1) {
			while(resumingUpload && !reachedUploadPoint){
				uploadedSoFar += bytes;
				if(uploadedSoFar > resumeUploadFrom){
					reachedUploadPoint = true;
					readNextBuffer = false;
				}else {
					size -= bytes;
					readNextBuffer = true;
					break;
				}
			}
			if(readNextBuffer) continue;
			if(reachedUploadPoint){
				reachedUploadPoint = false;
				resumingUpload = false;
				int startIndexBuffer = getStartIndexBuffer(resumeUploadFrom, uploadedSoFar);
				buffer = Arrays.copyOfRange(buffer,startIndexBuffer,BUFFER_SIZE);
				bytes = size <= BUFFER_SIZE ? ((int)size-startIndexBuffer) :buffer.length;
			}
			fileOutputStream.write(buffer, 0, bytes);
			size -= 4096;
			int progress = 100 - (int)((double)(size)/fixedSize * 100);
			if (progress < 100) {
				System.out.print(progress + "%" + (progress < 10 ? "\b\b" : "\b\b\b"));
			}
			dataOutputStream.flush();

			if(buffer.length != 4096) buffer = new byte[4*1024];
		}
		System.out.print("\b\b\b" + "...100% \n");
		System.out.println("File Downloaded");
		dataOutputStream.writeUTF("Done");
		fileOutputStream.close();
	}

	/**
	 * Calculates the normalized buffer for the data
	 * @param resumeUploadFrom - Current file size of the interrupted upload
	 * @param uploadedSoFar - Skipping these bytes as they have been uploaded
	 * @return - Start index of the buffer
	 */
	private int getStartIndexBuffer(long resumeUploadFrom, long uploadedSoFar)
	{
		int startIndexBuffer = 0;
		if(resumeUploadFrom < 4096){
			startIndexBuffer = (int) resumeUploadFrom;
		}else{
			startIndexBuffer = (int)(resumeUploadFrom - 4096);
			if(startIndexBuffer > 4096){
				startIndexBuffer = 4096 - (int)(uploadedSoFar - resumeUploadFrom);
			}
		}
		return startIndexBuffer;
	}

	/**
	 * Sends upload command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendUploadCmd(String[] args, int len) throws IOException
	{
		if(len == 3){
			//upload file to the server
			sendFileToServer(args);
			System.out.println("After sending");
			flush();
		}else{
			System.out.println(UPLOAD_CMD);
		}
	}

	/**
	 * Sends shutdown command to the server
	 * @param args String[] Arguments
	 * @param len Length of arguments
	 */
	private void sendShutdownCmd(String[] args, int len) throws IOException
	{
		if(len != 1){
			System.out.println(SHUTDOWN_CMD);
		}else{
			// shutdown the server
			sendToServer(conformCmd(args));
			socket.close();
			flush();
			System.exit(0);
		}
	}

	private void sendFileToServer(String[] cmd) throws IOException
	{
		int bytes = 0;
		File file = new File(cmd[1]);
		FileInputStream fileInputStream = null;
		if(file.isFile()){
			fileInputStream = new FileInputStream(file);
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream.writeUTF(conformCmd(cmd));
			dataOutputStream.writeLong(file.length());

			byte[] buffer = new byte[BUFFER_SIZE];
			System.out.print("Uploading File...");
			while ((bytes = fileInputStream.read(buffer)) != -1){
				dataOutputStream.write(buffer,0,bytes);
				dataOutputStream.flush();
				int progress = dataInputStream.readInt();
				if (progress < 100) {
					System.out.print(progress + "%" + (progress < 10 ? "\b\b" : "\b\b\b"));
				}
			}
			String msg = null;
			while(msg == null){
				msg = dataInputStream.readUTF();
			}
			System.out.print("\b\b\b" + "...100% \n");
			System.out.println("File Uploaded");
			fileInputStream.close();
		}else{
			System.err.println(cmd[1] + " is not a file");
		}

	}

	/**
	 * Sends the command to the server
	 */
	private void sendToServer(String cmd) throws IOException
	{
		System.out.println("Creating a socket at port: " + socket.getLocalPort());
		dataInputStream = new DataInputStream(socket.getInputStream());
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.writeUTF(cmd);

	}

	private void flush() throws IOException
	{
		if(dataOutputStream != null && dataInputStream != null) {
			dataOutputStream.flush();
			dataOutputStream.close();
			dataInputStream.close();
		}

	}

	//TODO: SETUP ENVIRONMENT
	public static void main(String[] args)
	{
		if(args.length < 1){
			System.out.println("java Client <action> <path> <path>");
			System.exit(-1);
		}else{
			new Client(args);
		}
	}
}
