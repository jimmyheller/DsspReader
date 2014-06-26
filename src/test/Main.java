package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Home on 6/4/2014.
 */
public class Main {
    public static void main(String args[]) {
        new ThreadTest().start();
        new ThreadTest().start();
        new ThreadTest().start();
    }
}

class ThreadTest extends Thread {

    public ThreadTest(){
        super();
    }
    public void run() {
        File hFile = new File("H:\\Jimmy\\H.txt");
        File cFile = new File("H:\\Jimmy\\C.txt");
        File eFile = new File("H:\\Jimmy\\E.txt");

        File heFile = new File("H:\\Jimmy\\resultFiles\\HE.txt");
        File hcFile = new File("H:\\Jimmy\\resultFiles\\HC.txt");
        File ceFile = new File("H:\\Jimmy\\resultFiles\\CE.txt");
        Runner runner = new Runner();
        runner.finalResult(hFile,eFile,heFile);
        runner.finalResult(hFile,cFile,hcFile);
        runner.finalResult(cFile,eFile,ceFile);
    }


}
