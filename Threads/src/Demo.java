
public class Demo {

    public static void main(String[] args) throws Exception{
        // Box box  = new Box();

        // Thread producer = new Thread(() -> { 
        //     for(int i=1; i<21 ; i++)
        //         box.producer(i);
        // });

        // Thread consumer = new Thread(() -> { 
        //     for(int i =1; i <21 ; i++){
        //         box.consumer();
        //     }
        // });

        // consumer.start();
        // producer.start();

        Number num = new Number();

        Thread even = new Thread(() -> {
            for (int i = 1; i < 21; i++) {
                num.even(i);
            }
        });

        Thread odd = new Thread(() -> {
            for (int i = 1; i < 21; i++) {
                num.odd(i);
            }
        });

        even.start();
        odd.start();

    }
    
}



class Number{

    synchronized public void even(int value){
        if(value%2!=0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Even value " + value);
            notify();
        }
        
        
    }

    synchronized public void odd(int value){
        if(value%2==0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Odd value " + value);
            notify();
        }
        
    }
}

// class Box{
//     private int value;
//     private boolean flag;

//     public Box() {
//         this.flag = false;
//     }

//     public int getValue() {
//         return value;
//     }

//     public void setValue(int value) {
//         this.value = value;
//     }

//     public boolean isFlag() {
//         return flag;
//     }

//     public void setFlag(boolean flag) {
//         this.flag = flag;
//     }

//     synchronized public void producer(int value){
//         if(flag == true){
//             try {
//                 wait();
//             } catch (InterruptedException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
//         }
//         System.out.println("Produced the value " + value);
//         this.value = value;
//         this.flag = true;
//         notify();
//     }

//     synchronized public void consumer(){
//         if(flag == false){
//             try {
//                 wait();
//             } catch (InterruptedException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
//         }
//         System.out.println("Consumed the value " + this.value);
//         this.flag = false;
//         notify();
//     }

    
// }
