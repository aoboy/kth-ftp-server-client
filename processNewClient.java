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
public class processNewClient extends Thread{
    Socket sock = null;
    DataOutputStream output = null;
    DataInputStream input = null;
    BufferedOutputStream fout = null;
    BufferedInputStream bis = null;
    private String fIn;
    private String lineIn, lineOut;
    private String path = "./";
    private boolean listFlag;
    /** Creates a new instance of processNewClient */
    public processNewClient(Socket sock) {
        this.sock = sock;
    }
    //rededinition of superClass method
    public void run(){
        try{
            input = new DataInputStream(this.sock.getInputStream());
            output = new DataOutputStream(this.sock.getOutputStream());
            while(true){
                lineIn=input.readUTF().trim();
                System.out.println("\tfrom Client: "+lineIn);
                if(lineIn.startsWith("fast")){
                    lineIn = lineIn.substring(5,lineIn.length());
                }
                if(lineIn.startsWith("quit", 0) || this.lineIn.startsWith("exit")){
                    this.processQuit();
                    break;
                }
                if(lineIn.startsWith("get ", 0)){
                    this.fIn = lineIn.substring(4, lineIn.length());
                    System.out.println("\tEntered File: "+this.fIn);
                    this.processGetFile(); continue;
                }
                if(lineIn.startsWith("put ", 0)){
                    this.fIn = lineIn.substring(4, lineIn.length());
                    this.processPutFile();
                    continue;
                }
                if(this.lineIn.startsWith("rm ", 0) || this.lineIn.startsWith("del ")){
                    if(this.lineIn.length() < 4){
                        System.out.println("\tFile to remove missing");  
                        this.output.writeUTF("ERROR!....!\n\tFile to remove/delete is missing");
                        continue;
                    }
                    if(this.lineIn.startsWith("rm ", 0))  this.fIn = lineIn.substring(3, lineIn.length());
                    if(this.lineIn.startsWith("del ", 0)) this.fIn = lineIn.substring(4, lineIn.length());
                     
                     this.processRemove();
                     continue;
                }
                if(lineIn.startsWith("list", 0) || this.lineIn.startsWith("ls -l")){
                    if(this.lineIn.contains("ls -l"))
                        this.listFlag = true;
                    this.processList(this.path); 
                    this.listFlag = false;
                    continue;
                }
                if(lineIn.startsWith("cd", 0)){
                    
                    if(this.lineIn.length() <=3){
                        System.out.println("\tChanging remote dir to =>./");  
                        this.path = "./";
                        this.output.writeUTF("OK....!\n\tChanging remote directory to =>"+this.path);
                        continue;
                    }
                    this.fIn = lineIn.substring(3, lineIn.length());
                    if(this.fIn.contains("../") || this.fIn.contains("c:")){
                        this.output.writeUTF("COMMAND ERROR >>Permission denied...!"); continue;
                    }
                    this.processChangeRemoteDir(this.fIn); continue;
                }
                if(this.lineIn.startsWith("pwd", 0)){
                    this.output.writeUTF("OK....!\n\t"+this.path);
                }else{
                    this.output.writeUTF("COMMAND ERROR ...!");
                }
              
           } //end of the loop while            
        }catch(IOException err){
            System.err.println(err.getMessage());
        }finally{ //will always be executed 
            try{
                if(this.input != null) input.close();
                if(this.output !=null) this.output.close();
                if(this.sock != null) this.sock.close();
            }catch(Exception e){System.out.println(e.getMessage());}
        }
    }  
    private void processList(String path)throws IOException{
        File file = new File(this.path);
        String out = "\tOK..!\n\t---------------------------------------------\n\t[d]-dir, [-]-file, [r]-read, [w]-write, [x]-exec\n\t";
        if ( !file.exists() || !file.canRead(  ) ) {
            System.out.println( "Can't read " + file );
            this.output.writeUTF("OK...!\n\tDirectory/FileNotFound");
            return;
        }
        if ( file.isDirectory(  ) ) {
            String [] files = file.list(  );
            
            try{
                for(int i=0; i< files.length; i++){
                    if(this.listFlag){
                        if(files[i].contains("."))
                            out+= "-rwx........... "+files[i]+"\n\t";
                        else{
                            out+= "drwx........... "+files[i]+"\n\t";
                        }
                    }
                    else{
                        out += files[i]+"\n\t";
                    }
                }
                 this.output.writeUTF(out);
                 this.output.flush();
            }
            catch(IOException io){ 
               System.out.println(io.getMessage()); System.exit(1);
            }
        }
        else
            try {
                FileReader fr = new FileReader ( file );
                BufferedReader in = new BufferedReader( fr );
                String line;
               while ((line = in.readLine( )) != null){
                    out+= line+"\n\t";
               }
                this.output.writeUTF(out);
                this.output.flush();
            }
            catch ( FileNotFoundException e ) {
                System.out.println( "File Disappeared" );
            }    
    }
  
    private void processQuit(){
         System.out.println("\tClient requested server to terminate connection");
         try{
            output.writeUTF("OK");
            output.flush();
         }catch(IOException err){}
    }
    private void processGetFile(){
         try{
            int noOfBytes, bsize;                       
            File file = new File(this.path+this.fIn);             
            bis = new BufferedInputStream(new FileInputStream(file));
            noOfBytes = (int)file.length();            
            bsize = this.sock.getSendBufferSize();
            System.out.println("\tFile Size: "+ noOfBytes);
            this.output.writeUTF("OK....!");
            this.output.writeInt(noOfBytes);
            this.transfer(this.bis, this.output, noOfBytes, bsize);
            System.out.println("\tFile transfer complete");        
        }catch(IOException err){ 
            System.out.println("error gen-> "+err.getMessage()); 
            try{this.output.writeUTF("error...."+err.getMessage());} catch(IOException e){}
            //System.exit(1);
        }finally{
            try{ if( this.bis != null) this.bis.close();}catch(IOException e){ }
          }
    }    
    private void processRemove(){
        try{
            //System.out.println("\tPath:"+this.path+this.fIn);
            File file = new File(this.path+this.fIn);             
            
            if(file.delete()){
                System.out.println("\tFile "+this.fIn+" removed from "+this.path+this.fIn);
                this.output.writeUTF("OK....\n\tFile "+this.fIn+" deleted from "+this.path);
            }
            System.out.println("\tPath:"+this.path+this.fIn);
        }catch(IOException io ){
            try{
            this.output.writeUTF("OK....\n\tFile "+this.fIn+" not Found"); }catch(IOException er){}           
        }
    }
    //server receive a file regarding a request from client
    private void processPutFile(){
        System.out.println("\tClient request Server to Upload a File!");
        try{
              this.output.writeUTF("OK....!");
              int b = this.input.readInt(); //number of Bytes to be read
              System.out.println("\tFile Size to be stored: "+b);
              int bsize = this.sock.getReceiveBufferSize(); // receiver bufferSize
              fout = new BufferedOutputStream(new FileOutputStream(this.path+fIn)); //create a new file at receiver site
              this.transfer(this.input, this.fout, b, bsize);  //transfer a file
              System.out.println("\tFile transfer complete");
        }catch(IOException io){ System.err.println(io.getMessage()); System.exit(1); }
        finally{
            try{  //check fout before closing
                if(this.fout != null) this.fout.close();
            } catch(Exception ex){}
        }
    }
    private void processChangeRemoteDir(String str) throws IOException{
            this.path += str+"/";
            System.out.println("\tChanging remote directory =>"+this.path); 
            this.output.writeUTF("OK.....\n\tChanging remote directory =>"+this.path);
    }
    private static int transfer(InputStream input, OutputStream output, int noOfBytes, int bufferSize)
 				throws IOException { 		 
        byte[] buf = new byte[bufferSize];
        int len, total = 0;
        String line;        
        while (((len = input.read(buf) ) > 0) && (total+1 < noOfBytes)){
            output.write(buf, 0, len); 
            total += len;
            if(len >= noOfBytes) break;
        }
	output.flush(); // flush the buffer when done
	return total;
    }
}
