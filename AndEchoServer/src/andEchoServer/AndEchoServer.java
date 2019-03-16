package andEchoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AndEchoServer {
	public static void main(String[] args) throws IOException {  // �����Լ�, IOException
		ServerSocket serverSocket = null; //소켓서버
		Socket clientSocket = null; //클라이언트 서버
		PrintWriter out = null;  // String 
		BufferedReader in = null;//stream 
		
		serverSocket = new ServerSocket(9999); //포트번호
		
		try {//클라이언트 연결될 시
			clientSocket = serverSocket.accept(); //클라이언트 서버와 연결
			System.out.println("클라이언트 연결");
			
			out = new PrintWriter(clientSocket.getOutputStream(), true); //String 변환되어서 나갈 문자열
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //들어온 문자열
			
			while (true) {
				String inputLine = null; 
				inputLine = in.readLine(); 
				System.out.println("클라이언트로 부터 받은 문자열:" + inputLine); 
				out.println(inputLine); 
				
				if (inputLine.equals("quit")) //quit 입력하면 서버종료
					break;
			}
			
			out.close();
			in.close();
			clientSocket.close();
			serverSocket.close();  //서버소켓 종료
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}