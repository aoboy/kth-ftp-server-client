/*--------------------------------------------------
 * MASTER PROGRAM IN COMPUTER NETWORKS
 * NETWORK PROGRAMMING with JAVA
 * LAB ASSIGNMENT 02- FTP Client/Server(MULTITHREADED)
 ----------------------------------------------------
 * Author: Antonio Oliveira Gonga
 * 831223-A635
 *date created:  STOCKHOLM; Feb. 05, 2007
 ---------------------------------------------------*/

package ftpclient;
import java.net.*;
import java.util.Date;
import java.io.*;

public class ftpClient {
    public static final int SERV_PORT = 4000;
    /** Creates a new instance of ftpClient */
    public ftpClient() {
    }
    
    public static void main(String[] args){
        // TODO code application logic here       
             if(args.length != 1){
                System.out.println("\t Error..! in the input args, <<enter the name of the Server as an input argment>>");
                System.out.println("\tjava -jar ftpclient.jar <<servername>>");
                System.exit(1);
            }
            String host = args[0]; //in.getHostName();
            clientClass cli = new clientClass(host, SERV_PORT);
            cli.process();
            System.out.println("\t-------------------------------------------------");
    }    
}
