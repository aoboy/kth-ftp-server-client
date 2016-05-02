/*--------------------------------------------------
 * MASTER PROGRAM IN COMPUTER NETWORKS
 * NETWORK PROGRAMMING with JAVA
 * LAB ASSIGNMENT 02- FTP Client/Server(MULTITHREADED)
 ----------------------------------------------------
 * Author: Antonio Oliveira Gonga
 * 831223-A635
 *date created:  STOCKHOLM; Feb. 05, 2007
 ---------------------------------------------------*/

package sftp;
import java.net.*;
import java.util.Date;
import java.io.*;
/**
 *
 * @author antónio gonga
 */
public class Sftp {
     private static final int SERV_PORT = 4000; //local port/listen
     private static final int QLEN = 7;
    /** Creates a new instance of Sftp */
    public Sftp() {
    }
    //no input arguments for the server
    public static void main(String[] args) {
        // TODO code application logic here
        ServerSocket serv = null; //reference of the server socket
        try{
            serv = new ServerSocket(SERV_PORT); //creating and binding a port to the server socket
            InetAddress in = InetAddress.getLocalHost();
            System.out.println("\n\n\t...............................................");
            System.out.println("\tServer hostname: <"+in.getHostName()+">, IP <"+in.getHostAddress()+">");
            System.out.println("\tSTOCKHOLM; "+new Date().toString());
            System.out.println("\t...............................................");
            while(true){ //infinite loop accept incoming connections from clients and open a new Thread for each connection                
                Socket connection = serv.accept();
                System.out.println("\tServer accepted connection from: "+connection.getInetAddress().getHostName()+"\n\t"+new Date().toString());
                 System.out.println("\t...............................................");
                processNewClient cli = new processNewClient(connection);
                cli.start();
                
                /*try{
                    cli.join(); //wait this thread to die
                }catch(InterruptedException ie){System.out.println(ie.getMessage());}
                finally{
                    try{ if(connection != null) connection.close();}catch(IOException e){}
                } */
                
            } //end of loop while          
        }catch(IOException e){
            System.out.println("\terror generated >> "+e.getMessage());
            System.exit(1);
        }finally{
            try{ //try to close the server socket
                if(serv != null) serv.close();
            }catch(Exception e2){
                System.err.println("error: "+e2.getMessage());
            }
        }
    }   
}
