public class App {
    public static void main(String[] args) throws Exception {

        MyRunnable runnable = new MyRunnable();

        Thread t1 = new Thread(runnable);

        System.out.println(t1.getState());
        MyThread myThread = new MyThread();

        myThread.start();
        
        t1.start();


        System.out.println(t1.getState());

        System.out.println(t1.getState());

        System.out.println(t1.getState());


        System.out.println(t1.getState());

        System.out.println(t1.getState());

        Thread.sleep(1000);

        System.out.println(t1.getState());
    }
}


// Defining the Thread
class MyThread extends Thread{
    @Override 
    public void run(){
        System.out.println("Hello from MyThread" + " " + Thread.currentThread().getName());
    }
}

//Defining the Task
class MyRunnable implements Runnable{

    @Override
    public void run() {
        System.out.println("Hello from MyRunnable" + " " + Thread.currentThread().getName());
        
    }
    
}

/*
Thread lifecycle:
     start()             run()           run() finishes
New --------> Runnable --------> Running ---------------> Terminated



How threads are created
Thread Lifecycle
Thread Methods
Problem with Multithreading -> Race condition, Visibility and Ordering.

Synchronized Keyword, Static Sync & Custom Locks
*/