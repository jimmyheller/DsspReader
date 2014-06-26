package org.mentosa.dsspreader.main;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Jimmy on 6/6/2014.
 */
public class Divider extends Thread {

    protected static String hTempFileAddress;// = "E:\\Jimmy\\hTempMid";
    protected static String cTempFileAddress;// = "E:\\Jimmy\\cTempMid";
    protected static String eTempFileAddress;// = "E:\\Jimmy\\eTempMid";
    protected static String hcResultFolder;// = "";
    protected static String ecResultFolder;// = "";
    protected static String heResultFolder;// = "";

    public static Logger LOGGER = Logger.getLogger(Runner.class.getName());

    private static ArrayList<String> heResult = new ArrayList<>();
    private static ArrayList<String> hcResult = new ArrayList<>();
    private static ArrayList<String> ecResult = new ArrayList<>();


    public void makeProbe(String tempFileAddress, File file) {
        makeDir(tempFileAddress);
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            br.readLine();

            int lentgh = 4;
            int startIndex = 0;
            int endIndex = 4;
            int start;

            String currentLine = "";
            String sequence = "";
            LOGGER.log(Level.INFO, "Started To Divide " + file.getName());
            while ((currentLine = br.readLine()) != null) {
                String[] temp = currentLine.split("   ");
                sequence = temp[0].trim();
                // String pdbId = temp[2].trim();
                start = Integer.parseInt(temp[2].trim());
                //end = Integer.parseInt(temp[8].trim());


                //while (lentgh <= sequence.length()) {
                while (lentgh <= 10) {
                    String fileName = tempFileAddress + file.getName().substring(0, 1) + lentgh + ".txt";
                    while (endIndex <= sequence.length()) {
                   /* String[] dataTokeep = new String[4];
                    dataTokeep[0] =
                    dataTokeep[1] = temp[2];
                    dataTokeep[2] = String.valueOf(start + startIndex);
                    dataTokeep[3] = String.valueOf(start + endIndex - 1);*/
                        String data = sequence.substring(startIndex, endIndex).trim() + "   " + temp[1].trim() + "   " + String.valueOf(start + startIndex).trim() + "   " + String.valueOf(start + endIndex - 1).trim();
                        writeData(fileName, true, data);
                        startIndex++;
                        endIndex++;
                    }
                    lentgh++;
                    startIndex = 0;
                    endIndex = lentgh;
                }

                endIndex = lentgh = 4;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in file: " + file.getName() + " Problem is -> " + e.getMessage());
        }
        LOGGER.log(Level.INFO, "Ending Dividing file " + file.getName());


    }

    public ArrayList<String> comparator(String baseFilesAddres, String destFileAddress, String parameter1, String parameter2) {
        int baseTempBig = 0;
        int destTempBig = 0;
        int counter;
        ArrayList<File> baseFiles = getDir(baseFilesAddres, "txt");
        ArrayList<File> destFiles = getDir(destFileAddress, "txt");
        ArrayList<String> result = new ArrayList<>();
        for (File file : baseFiles) {
            int tempBig = Integer.parseInt(file.getName().substring(1, file.getName().indexOf(".")).trim());
            if (baseTempBig < tempBig) {
                baseTempBig = tempBig;
            }

        }
        for (File file : destFiles) {
            int tempBig = Integer.parseInt(file.getName().substring(1, file.getName().indexOf(".")).trim());
            if (destTempBig < tempBig) {
                destTempBig = tempBig;
            }

        }
        if (baseTempBig >= destTempBig) {
            counter = destTempBig;
        } else {
            counter = baseTempBig;
        }
        int tempCounter = 4;
        //-----------------------------------------
        BufferedReader baseReader = null;
        BufferedReader destReader = null;
        String baseParent = baseFilesAddres + parameter1;
        String destParent = destFileAddress + parameter2;
        String baseCurrentLine = "";
        String destCurrentLine = "";
        //-----------------------------------------
        try {
            while (tempCounter < counter) {
                baseReader = new BufferedReader(new FileReader(new File(baseParent + tempCounter + ".txt")));
                LOGGER.log(Level.INFO, "Starting to read file " + baseParent + tempCounter + ".txt");
                while ((baseCurrentLine = baseReader.readLine()) != null) {
                    destReader = new BufferedReader(new FileReader(new File(destParent + tempCounter + ".txt")));
                    // LOGGER.log(Level.INFO,"Starting to read file "+ destParent + tempCounter + ".txt" );
                    while ((destCurrentLine = destReader.readLine()) != null) {
                        String[] baseArray = baseCurrentLine.split("   ");
                        String[] destArray = destCurrentLine.split("   ");
                        if (baseArray[0].equals(destArray[0]) && baseArray[1].equals(destArray[1])) {
                            LOGGER.log(Level.INFO, "found data....");
                            result.add(baseArray[0] + "   " + baseArray[1] + "   " + baseArray[2] + "   " + baseArray[3] +
                                    destArray[0] + "   " + destArray[1] + "   " + destArray[2] + "   " + destArray[3]);
                        }
                    }
                }
                tempCounter++;
            }
            LOGGER.log(Level.INFO, "End of comparing for two files.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Problem In Finding Result Where baseFile is :" + baseParent + tempCounter + ".txt" +
                    " And Dest File is : " + destParent + tempCounter + ".txt And The Problem Is -> " + e.getMessage());
        }
        return result;


    }

    private void writeData(String fileName, boolean keepFile, String data) {
        FileWriter writer;
        File file = new File(fileName);
        try {

            if (keepFile) {
                writer = new FileWriter(file, keepFile);
            } else {
                writer = new FileWriter(file);
            }
            BufferedWriter bw = new BufferedWriter(writer);

            bw.write(data);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Problem in writing data to file: " + file.getName() + " -> " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            DsspGui.txtAreaLog.append("Problem in writing data to file: " + file.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            new Runner().writeData(Runner.logFile, true, "Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Exception in file: " + file.getName() + " Problem is -> " + e.getMessage());


        }

    }

    private ArrayList<String> findGreatesMer(ArrayList<String> data) {
        int baseCounter = 0;
        int destCounter = 1;
        while (baseCounter < data.size()) {
            String base = data.get(baseCounter).split("   ")[0];
            while (destCounter < data.size()) {
                if (data.get(destCounter).split("   ")[0].contains(base)) {
                    data.remove(baseCounter);
                    destCounter = baseCounter;
                }
                destCounter++;
            }
            destCounter = baseCounter + 2;
            baseCounter++;

        }

        return data;
    }

    private String makeDir(String address) {
        File tempFolder = new File(address);

        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }//edn of if
        else {
            deleteDir(tempFolder);
            tempFolder.mkdir();
        }
        return tempFolder.getAbsolutePath();

    }

    public void deleteDir(File file) {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();

            } else {
                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    //recursive delete
                    deleteDir(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();

                }
            }

        } else {
            //if file, then delete it
            file.delete();

        }
    }

    private ArrayList<File> getDir(String location, String suffix) {
        File f = new File(location);
        ArrayList<File> files = new ArrayList<>(Arrays.asList(f.listFiles()));
        ArrayList<File> dirFiles = new ArrayList<>();
        for (File file : files) {
            int pointer = file.getName().lastIndexOf(".");
            if (pointer > 0 && file.getName().substring(pointer).equals("." + suffix)) {
                dirFiles.add(file);
            }//end of if
        }//end of for
        return dirFiles;
    }


    public static void runner(final File hFile, final File eFile, final File cFile) {
        Thread.currentThread().setName("Main Thread");
        Runnable rnr1 = new Runnable() {
            @Override
            public void run() {
                try {
                    new Divider().makeProbe(hTempFileAddress, hFile);
                } catch (Exception e) {
                    DsspGui.txtAreaLog.append("Problem In Making Probe : " + hFile.getName() + " -> " + e.getMessage() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    new Runner().writeData(Runner.logFile, true, "Problem In Making Probe : " + hFile.getName() + " -> " + e.getMessage());
                }
            }
        };
        Runnable rnr2 = new Runnable() {
            @Override
            public void run() {
                try {
                    new Divider().makeProbe(eTempFileAddress, eFile);
                } catch (Exception e) {
                    DsspGui.txtAreaLog.append("Problem In Making Probe : " + eFile.getName() + " -> " + e.getMessage() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    new Runner().writeData(Runner.logFile, true, "Problem In Making Probe : " + eFile.getName() + " -> " + e.getMessage());
                }
            }
        };
        Runnable rnr3 = new Runnable() {
            @Override
            public void run() {
                try {
                    new Divider().makeProbe(cTempFileAddress, cFile);
                } catch (Exception e) {
                    DsspGui.txtAreaLog.append("Problem In Making Probe : " + cFile.getName() + " -> " + e.getMessage() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    new Runner().writeData(Runner.logFile, true, "Problem In Making Probe : " + cFile.getName() + " -> " + e.getMessage());
                }
            }
        };
        Thread t1 = new Thread(rnr1);
        t1.start();
        t1.setName("Thread For Probe H");
        Thread t2 = new Thread(rnr2);
        t2.start();
        t2.setName("Thread For Probe E");
        Thread t3 = new Thread(rnr3);
        t3.start();
        t3.setName("Thread For Probe C");

        while (true) {
            LOGGER.log(Level.INFO, "Checking for stopped threads");
            if (!t1.isAlive() && !t2.isAlive() && !t3.isAlive()) {
                LOGGER.log(Level.INFO, "All files creation is stopped....");
                break;
            } else {
                try {
                    Thread.sleep(5000);
                    LOGGER.log(Level.INFO, "Sleeping for 30 secs :" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    DsspGui.txtAreaLog.append("Exception Thread Management : " + Thread.currentThread().getName() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    new Runner().writeData(Runner.logFile, true, "Exception Thread Management : " + Thread.currentThread().getName());
                }
            }

        }
        LOGGER.log(Level.INFO, "starting to read divided files.....");

        Runnable runner1 = new Runnable() {
            @Override
            public void run() {

                Divider divider = new Divider();
                heResult = divider.comparator(hTempFileAddress, eTempFileAddress, "H", "E");
                heResult = divider.findGreatesMer(heResult);

                divider.makeDir(heResultFolder);
                divider.writeData(heResultFolder + "HE.txt", false, "");
                for (String line : hcResult) {
                    divider.writeData(heResultFolder + "HE.txt", false, line);
                    divider.writeData(heResultFolder + "HE" + line.split("   ")[0].length() + ".txt", true, line);
                }
            }
        };
        Runnable runner2 = new Runnable() {
            @Override
            public void run() {

                Divider divider = new Divider();
                hcResult = divider.comparator(hTempFileAddress, cTempFileAddress, "H", "C");
                hcResult = divider.findGreatesMer(hcResult);

                divider.makeDir(hcResultFolder);
                divider.writeData(hcResultFolder + "HC.txt", false, "");
                for (String line : hcResult) {
                    divider.writeData(hcResultFolder + "HC.txt", false, line);
                    divider.writeData(hcResultFolder + "HC" + line.split("   ")[0].length() + ".txt", true, line);
                }
            }
        };

        Runnable runner3 = new Runnable() {
            @Override
            public void run() {
                Divider divider = new Divider();
                ecResult = divider.comparator(eTempFileAddress, cTempFileAddress, "E", "C");
                ecResult = divider.findGreatesMer(ecResult);
                divider.makeDir(ecResultFolder);
                divider.writeData(ecResultFolder + "EC.txt", false, "");
                for (String line : ecResult) {
                    divider.writeData(ecResultFolder + "EC.txt", false, "");
                    divider.writeData(ecResultFolder + "EC" + line.split("   ")[0].length() + ".txt", true, line);
                }
            }
        };
        Thread comp1 = new Thread(runner1);
        comp1.start();
        comp1.setName("comp1");
        Thread comp2 = new Thread(runner2);
        comp2.start();
        comp2.setName("comp2");
        Thread comp3 = new Thread(runner3);
        comp3.start();
        comp3.setName("comp3");

        while (true) {
            LOGGER.log(Level.INFO, "Checking for stopped threads");
            if (!comp1.isAlive() && !comp2.isAlive() && !comp3.isAlive()) {
                LOGGER.log(Level.INFO, "Ended All");
                break;
            } else {
                try {
                    Thread.sleep(5000);
                    LOGGER.log(Level.INFO, "Sleeping for 30 secs :" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    DsspGui.txtAreaLog.append("Exception Thread Management : " + Thread.currentThread().getName() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    new Runner().writeData(Runner.logFile, true, "Exception Thread Management : " + Thread.currentThread().getName());
                }
            }
        }


    }

    /*public static void main(String[] args) {
        hTempFileAddress = "E:\\Jimmy\\htemp\\";
        eTempFileAddress = "E:\\Jimmy\\etemp\\";
        cTempFileAddress = "E:\\Jimmy\\ctemp\\";
        hcResultFolder = "E:\\Jimmy\\hcresult\\";
        ecResultFolder = "E:\\Jimmy\\ecresult\\";
        heResultFolder = "E:\\Jimmy\\heresult\\";

        final File hfile = new File("E:\\Jimmy\\H.txt");
        final File cfile = new File("E:\\Jimmy\\C.txt");
        final File efile = new File("E:\\Jimmy\\E.txt");
        Divider diver = new Divider();
        diver.makeProbe(hTempFileAddress, hfile);
        diver.makeProbe(eTempFileAddress, efile);
        ArrayList<String> j = diver.comparator("E:\\Jimmy\\htemp\\", "E:\\Jimmy\\etemp\\", "H", "E");
        diver.findGreatesMer(j);
        diver.makeDir(ecResultFolder);
        diver.writeData(ecResultFolder + "HC.txt", false, "");
        for (String sample : j) {

            diver.writeData(ecResultFolder + "HC.txt", false, sample);
            diver.writeData(ecResultFolder + "HC" + sample.split("   ")[0].length() + ".txt", false, sample);
        }

*/
  //  }

}
