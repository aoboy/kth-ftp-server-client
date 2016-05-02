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
import java.util.Scanner;

public class clientClass {
    DataInputStream input = null;
    DataOutputStream output = null;
    BufferedInputStream bis = null;
    BufferedOutputStream fout = null;
    Socket sock = null;
    private String straux;
    private String lineIn, lineOut;
    private int timeOutCount;
    
    /** Creates a new instance of clientClass */
    public clientClass(String hostname, int port) {
        try{
        InetAddress serveraddr = InetAddress.getByName(hostname);
        //this.sock = new Socket();
        this.sock = new Socket(serveraddr, port);
        this.timeOutCount = 0;
        }catch(IOException e){
            System.out.println("\tUnable to connect to Server: "+hostname +"\n\terror generated: "+e.getMessage());
            System.exit(1);            
        }
    }
    /*--------------------------------------------------------------
     *decodes the input commands and performs the requested task
     *according to the received command      
     --------------------------------------------------------------*/
    public void process(){
        try{
            InetAddress in = InetAddress.getLocalHost();
            System.out.println("\n\n\t...............................................");
            System.out.println("\tClient hostname: <"+in.getHostName()+">, IP <"+in.getHostAddress()+">");
            System.out.println("\tSTOCKHOLM; "+new Date().toString());
            System.out.println("\t...............................................");
            //get input stream from socket(in<---out)
            input = new DataInputStream(this.sock.getInputStream());
            //get output stream from socket(in-->out)
            output = new DataOutputStream(this.sock.getOutputStream());
            //scan input data from keyboard
            Scanner scanner = new Scanner(System.in);
            
            /*------------------ loop while------------------*/
            //run infinitely and decodes the input commands
            while(true){  
                //try{ 
                        this.sock.setSoTimeout(3000);  //time-Out
                        System.out.println("\t...............................................");
                        System.out.print("\tEnter a command>");
                        lineIn = scanner.nextLine().trim();
                        //quit command decoder
                        if(lineIn.startsWith("quit", 0) || this.lineIn.startsWith("exit")){
                            this.output.writeUTF(lineIn);
                            lineOut = this.input.readUTF();                    
                            System.out.println("\t"+lineOut+" BYE BYE");
                            break;
                        }
                        
                    //-------------get command decoder------------------                        
                        if(lineIn.startsWith("get ")){
                            straux = lineIn.substring(4, lineIn.length());
                            this.output.writeUTF(lineIn);
                            if(this.straux == null){
                                System.out.println("\tNo remote file name entered");
                                continue;
                            }
                            this.getFile(); continue;
                        }
                        
                //---------put command decoder-----------
                        if(lineIn.startsWith("put ")){  //put
                            this.straux = lineIn.substring(4, lineIn.length());                     
                            if(this.straux == null){
                                System.out.println("\tNo input file name entered");
                                continue;
                            }
                            this.output.writeUTF(lineIn);
                            this.putFile(); continue;
                        }
                 //-------------delete a file--------------
                        if(this.lineIn.startsWith("rm ", 0) || this.lineIn.startsWith("del ", 0)){                                                           
                          this.output.writeUTF(this.lineIn)  ;
                          System.out.println("\t"+this.input.readUTF());
                          continue;
                        }
                        
                //-------- change dir command decoder---------
                        if(lineIn.startsWith("cd", 0) && !this.lineIn.contains("../") && !this.lineIn.contains("c:")){
                            if(this.lineIn.length() <=3){
                                this.output.writeUTF(this.lineIn);
                                System.out.println("\t"+this.input.readUTF());
                                continue;
                            }
                            this.straux = lineIn.substring(3, lineIn.length());
                            if(this.straux == null){
                                System.out.println("\tRemote dir error/empty");
                                continue;
                            }                         
                            this.output.writeUTF(lineIn);
                            System.out.println("\t"+this.input.readUTF());
                            continue;
                        }
                //-----------list files in The Server /path diretory--------
                        if(lineIn.startsWith("list", 0) || this.lineIn.startsWith("ls -l ", 0)){
                            this.output.writeUTF(lineIn);
                            this.List(); continue;
                        }
                        if(this.lineIn.startsWith("pwd")){
                            this.output.writeUTF(this.lineIn);
                            System.out.println("\t"+this.input.readUTF()); continue;
                        }else{
                            this.output.writeUTF(this.lineIn);
                            System.out.println("\t"+this.input.readUTF());
                        } 
                   /***************************************************************
                    *fast retransmit here....................
                        if(this.sock.getSoTimeout()==3000){
                             this.fastRetransmition();  
                            if(++this.timeOutCount > 3){
                                System.err.println("\tServer did not Reply("+this.timeOutCount+")");
                                this.timeOutCount = 0;
                                break;
                            }
                        }
                   ****************************************************************/
            } 
        ///*------------------------end of the loop while----------------------------*/
            
        }catch(IOException e){
            System.out.println("\tError:"+e.getMessage());                       
            System.exit(1);
        }finally{
            try{ 
                //close input, output streams and the socket
                if(this.input != null) this.input.close();
                if(this.output != null) this.output.close();
                if(this.sock != null) this.sock.close();
            }catch(IOException er){System.out.println(er.getMessage());}
        }
    }    
    /*---------------------------------------------------------
     *retrieve a file from a remote server
     *input: none;
     *output: file from server     
     ---------------------------------------------------------*/
    public void getFile(){ 
         try{
             String line = this.input.readUTF();
              System.out.println("\t"+line);
              if(line.startsWith("error")){
                return;
              }
              int b = this.input.readInt(); //number of Bytes to be read
              int bsize = this.sock.getReceiveBufferSize(); // receiver bufferSize
              fout = new BufferedOutputStream(new FileOutputStream(straux)); //create a new file at receiver site
              System.out.println("\tFile Size to Receive: "+b+" Bytes");
              this.retrieveFile(this.input, this.fout, b, bsize);  //transfer a file
              System.out.println("\tFile transfer complete/received successful");
        }catch(IOException io){ System.err.println(io.getMessage()); System.exit(1); }
        finally{
            try{  //check fout before closing
                if(this.fout != null) this.fout.close();
            } catch(Exception ex){}
        }
    }
    //performs the Uploading 
    //Uploads a file into the server
    public void putFile(){  
        try{
            int noOfBytes, bsize;  
            File file = new File(this.straux);
            bis = new BufferedInputStream(new FileInputStream(file));
            //get File Size of the file to be Uploaded
            noOfBytes = (int)file.length();       
            //get remote host buffer Size
            bsize = this.sock.getSendBufferSize();
            System.out.println("\tFile Size: "+ noOfBytes);
            //send File Size First before transfering it
            System.out.println("\t"+this.input.readUTF());
            this.output.writeInt(noOfBytes);
            this.transfer(this.bis, this.output, noOfBytes, bsize);
            System.out.println("\tFile Transfer Done!!");
        }catch(IOException err){ 
            System.out.println(err.getMessage()); 
            System.exit(1);
        }finally{
            try{ if( this.bis != null) this.bis.close();}catch(IOException e){ }
          }
    }
    //List files in the remote directory
    private void List() throws IOException{
        System.out.println("\t.............Listing Server Dir................");
        String line = this.input.readUTF();
        System.out.println(line);
    }
    
    /*-------------------------------------------------------------------------------------------
     *function from Course dir--> used for sending file to the Server/upload
     --------------------------------------------------------------------------------------------*/
    private static void transfer(InputStream is, OutputStream output, int noOfBytes, int bufferSize)
                                throws IOException{
         // Transfer bytes from in to out
        byte[] buf = new byte[bufferSize]; 
        int len, total = 0;       
        while ((len = is.read(buf)) > 0 && total < noOfBytes) {
            output.write(buf, 0, len);
            total += len; 
            if(total >= noOfBytes) break;
        }
         System.out.println("\tTotal Sent:"+total+" bytes");
	output.flush();                                    
    }
    //receives a file from a remote Host
    /*-------------------------------------------------------------------------------------------
     *function from Course dir--> used for receiving file from the Server/download or retrieve
     --------------------------------------------------------------------------------------------*/
    private static int retrieveFile(InputStream input, OutputStream output, int noOfBytes, int bufferSize)
 				throws IOException { 		 
        byte[] buf = new byte[bufferSize];
        int len, total = 0;
        //continue reading while total bytes read less than file size
        while ((len = input.read(buf)) > 0 && total < noOfBytes) {
            output.write(buf, 0, len);            
            total += len;
            if(total >= noOfBytes) break; //break if noOfBytes has been reached
        }
        System.out.println("\tTotal received:"+total+" bytes");
	output.flush(); // flush the buffer when done        
	return total;
    }
     private void fastRetransmition(){
        try{
            this.output.writeUTF("fast "+this.lineIn);
            this.output.writeUTF("fast "+this.lineIn);
            this.output.writeUTF("fast "+this.lineIn);
        }catch(IOException io){
            System.out.println("Error while retransmitting request: "+io.getMessage());
        }
     }
    
}
