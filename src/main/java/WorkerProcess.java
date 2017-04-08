public class WorkerProcess
{
    public static void main(String[] args)
    {
        while(true) {
        	try { 
        	    Thread.sleep(5000);
            } catch(InterruptedException e) {}
            System.out.println("Worker process woke up in 5 seconds");
        }
    }    
}
