package test;


import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Jimmy on 4/22/2014.
 */
public class Runner {

    protected static String tempFolderAddress;
    protected static String midFilesAddress;
    protected static String resultFiles;
    protected static String partialResult;
    protected static String logFileAddress;
    protected static File tempListAddress;
    protected static File logFile;
    protected final static Logger LOGGER = Logger.getLogger(Runner.class.getName());


    protected void run(String location, boolean flag) {

        //GETTING ORGINIAL FILES
        makeDir(logFileAddress);
        logFile = makeFile(logFileAddress, "log.txt");
        ArrayList<File> dsspFiles = getDir(location, "dssp");
        makeDir(tempFolderAddress);
        //if(dsspFiles.size()  != 0) {
        for (File file : dsspFiles) {
            //CREATING TEMP FILES
            LOGGER.log(Level.INFO, "Reading File ->  " + file.getName());
            DsspGui.txtAreaLog.append("Reading File ->  " + file.getName() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            readDsspFile(file);
            //pdbId = file.getName();
        }//end of for
        //}//end of if
//        else{
        //return;
        //      }

        //CREATING MERS FILES
        makeDir(midFilesAddress);
        File hFile = makeFile(midFilesAddress, "H.txt");
        File eFile = makeFile(midFilesAddress, "E.txt");
        File cFile = makeFile(midFilesAddress, "C.txt");

        writeData(hFile, false, "SEQUENCE                     PDBID         START         END");
        writeData(eFile, false, "SEQUENCE                     PDBID         START         END");
        writeData(cFile, false, "SEQUENCE                     PDBID         START         END");

        //Creating sequences to be written in midFiles
        ArrayList<File> midFiles;
        if (flag) {
            midFiles = readListFile(tempListAddress);
        } else {
            midFiles = getDir(tempFolderAddress, "dssp");
        }
        for (File file : midFiles) {
            if (file.exists()) {
                ArrayList<String[]> memoryForCutters = readTmpFile(file);
                LOGGER.log(Level.INFO, "Writting Sequences for file ->  " + file.getName());
                DsspGui.txtAreaLog.append("Writting Sequences for file ->  " + file.getName() + "\n");
                DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                writeSequences(memoryForCutters, file.getName());
            } else {
                DsspGui.txtAreaLog.append(file.getName() + " does not exist in the directory " + "\n");
                DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            }
        }
        //making files to be write final result to them.


        makeDir(resultFiles);
        File heFile = makeFile(resultFiles, "HE.txt");
        File ceFile = makeFile(resultFiles, "CE.txt");
        File hcFile = makeFile(resultFiles, "HC.txt");
        makeDir(partialResult);
        //making headers
        writeData(ceFile, false, "SEQUENCE       PDBID  START   END   SEQUENCE    PDBID   START    END   ");
        writeData(heFile, false, "SEQUENCE       PDBID  START   END   SEQUENCE    PDBID   START    END   ");
        writeData(hcFile, false, "SEQUENCE       PDBID  START   END   SEQUENCE    PDBID   START    END   ");


        hFile = new File("H:\\Jimmy\\H.txt");
        cFile = new File("H:\\Jimmy\\C.txt");
        eFile = new File("H:\\Jimmy\\E.txt");
        finalResult(hFile, eFile, heFile);
        finalResult(cFile, eFile, ceFile);
        finalResult(hFile, cFile, hcFile);


    }

    private ArrayList<File> readListFile(File file) {
        BufferedReader reader;
        String currentLine;
        ArrayList<File> tempFilesList = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));

            while ((currentLine = reader.readLine()) != null) {
                File tmp = new File(Runner.tempFolderAddress + System.getProperty("file.separator") + currentLine.toLowerCase() + ".dssp");
                tempFilesList.add(tmp);


            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage());
        }
        return tempFilesList;


    }

    private ArrayList<String[]> findGreatesMer(ArrayList<String[]> data) {
        int baseCounter = 0;
        int destCounter = 1;
        while (baseCounter < data.size()) {
            while (destCounter < data.size()) {
                if (data.get(destCounter)[0].contains(data.get(baseCounter)[0])) {
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

    protected void finalResult(File baseFile, File destFile, File resultFile) {
        long lineNumber = countLineNumbers(baseFile);
        int counter = 1;

        ArrayList<String[]> data = new ArrayList<>();
        ArrayList<String[]> data2 = new ArrayList<>();
        LOGGER.log(Level.INFO, "Reaching comparing files of " + baseFile.getName() + " and  destFile : " + destFile.getName());
        DsspGui.txtAreaLog.append("Reaching comparing files of " + baseFile.getName() + " and  destFile : " + destFile.getName() + "\n");
        DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
        long destLineNumbers = countLineNumbers(destFile);


        while (counter <= lineNumber) {
            compareProbeToDestFile(makeProbe(baseFile, counter), destFile, data, data2, destLineNumbers);
            counter++;

        }
        ArrayList<String[]> destData;
        ArrayList<String[]> baseData;
        LOGGER.log(Level.INFO, "Reaching Finding Mers");
        DsspGui.txtAreaLog.append("Reaching Finding Mers" + "\n");
        DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
        destData = findGreatesMer(data);
        baseData = findGreatesMer(data2);
        LOGGER.log(Level.INFO, "Reaching Writing to file -> " + resultFile.getName());
        DsspGui.txtAreaLog.append("Reaching Writing to file -> " + resultFile.getName() + "\n");
        DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
        writeToFile(baseData, destData, resultFile);


    }


    private void writeToFile(ArrayList<String[]> baseData, ArrayList<String[]> destData, File resultFile) {
        int baseCounter = 0;
        int destCounter = 0;
        while (baseCounter < baseData.size() && destCounter < destData.size()) {
            String[] baseTemp = baseData.get(baseCounter);
            String[] destTemp = destData.get(destCounter);
            String line =
                    baseTemp[0] + "   " +           //SEQUENCE
                            baseTemp[1] + "   " +//PDBID
                            baseTemp[2] + "   " +//START
                            baseTemp[3] + "   " +//END
                            destTemp[0] + "   " +//SEQUENCE
                            destTemp[1] + "   " +//PDBID
                            destTemp[2] + "   " +//START
                            destTemp[3];//END
            synchronized (this) {
                writeData(resultFile, true, line);
                writePartialData(baseTemp[0].length(), resultFile.getName(), line);
            }
            baseCounter++;
            destCounter++;

        }


    }

    private void writePartialData(int length, String name, String line) {
        File file = new File(partialResult + name.substring(0, 2) + length + ".txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
                writeData(file, false, "SEQUENCE       PDBID  START   END   SEQUENCE    PDBID   START    END   ");
            }
            writeData(file, true, line);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error at Write Partial Data in file: " + file.getName());
            DsspGui.txtAreaLog.append("Error at Write Partial Data in file: " + file.getName() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error at Write Partial Data in file: " + file.getName());
        }
    }

    private void compareProbeToDestFile(ArrayList<String[]> baseMemory, File destFile, ArrayList<String[]> finalData, ArrayList<String[]> finalData2, long destLineNumbers) {

        int counter = 1;
        int destFileCounter = 0;
        int baseFilecounter = 0;
        //---------------------------------------------------------
//        int baseSize = baseMemory.size();

        while (counter <= destLineNumbers) {
            if ((destFileCounter - counter) % 1000 == 0)
                LOGGER.log(Level.INFO, (destLineNumbers - counter) + " : To Go in dest file - > " + destFile.getName());
            ArrayList<String[]> destMemory = makeProbe(destFile, counter);
            int destSize = destMemory.size();
            List<String> s = new ArrayList<>();
            LOGGER.log(Level.INFO, "Start Making List");

            for(String[] strTemp : destMemory){
                s.add(strTemp[0]);
            }
            LOGGER.log(Level.INFO, "End Making List");
            int baseSize = s.size();
            if (destSize > 0 && baseSize > 0)
                while (baseFilecounter < baseSize) {

                  /*  int desStringSize = destMemory.get(destFileCounter)[0].length();
                    int baseStringSize = baseMemory.get(baseFilecounter)[0].length();
                    String destString = destMemory.get(destFileCounter)[0];
                    String baseString = baseMemory.get(baseFilecounter)[0];
                    String basePdbid = baseMemory.get(baseFilecounter)[1].substring(0, 4);
                    String destPdbid = destMemory.get(destFileCounter)[1].substring(0, 4);
                    while ((destFileCounter < destSize) && (desStringSize == baseStringSize)) {
                        if (baseString.equals(destString) && !(basePdbid.equals(destPdbid))) {
                            LOGGER.log(Level.INFO, "Found sequence :" + destMemory.get(destFileCounter)[0]);
                            DsspGui.txtAreaLog.append("Found sequence :" + destMemory.get(destFileCounter)[0] + "\n");
                            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                            finalData.add(destMemory.get(destFileCounter));
                            finalData2.add(baseMemory.get(baseFilecounter));
                        }
                        destFileCounter++;
                    }
                    destFileCounter = 0; */
                    baseFilecounter++;
                }
                    counter++;
                    destFileCounter = 0;
                    baseFilecounter = 0;

        }

    }

    private long countLineNumbers(File file) {

        int linenumber = 0;
        try {


            if (file.exists()) {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);

                while (lnr.readLine() != null) {
                    linenumber++;
                }
                lnr.close();

            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in reading line numbers in file: " + file.getName() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in reading line numbers in file: " + file.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in reading line numbers in file: " + file.getName() + " -> " + e.getMessage());

        }
        return linenumber;
    }

    private ArrayList<String[]> makeProbe(File baseFile, long lineNumber) {

        BufferedReader reader;
        String currentLine;
        int lentgh = 4;
        int startIndex = 0;
        int endIndex = 4;
        ArrayList<String[]> memory = new ArrayList<>();

        String sequence;
        String[] temp;
        int tempCounter = 1;
        int start;
        int end;

        try {
            reader = new BufferedReader(new FileReader(baseFile));
            while (tempCounter <= lineNumber) {
                reader.readLine();
                tempCounter++;
            }
            //------------------------------------------------------
            currentLine = reader.readLine();
            if (currentLine != null) {
                temp = currentLine.split(" ");
                sequence = temp[0].trim();
                String pdbId = temp[2].trim();
                start = Integer.parseInt(temp[5].trim());
                //end = Integer.parseInt(temp[8].trim());

                // while (lentgh <= sequence.length()) {
                while (lentgh <= sequence.length()) {
                    while (endIndex <= sequence.length()) {

                        String[] dataTokeep = new String[4];
                        dataTokeep[0] = sequence.substring(startIndex, endIndex);
                        dataTokeep[1] = pdbId;
                        dataTokeep[2] = String.valueOf(start + startIndex);
                        dataTokeep[3] = String.valueOf(start + endIndex - 1);
                        memory.add(dataTokeep);
                        startIndex++;
                        endIndex++;
                    }
                    lentgh++;
                    startIndex = 0;
                    endIndex = lentgh;
                }
            }


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in Making Probe in file :" + baseFile.getName() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in Making Probe in file :" + baseFile.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in Making Probe in file :" + baseFile.getName() + " -> " + e.getMessage());

        }
        //------------------------------------------------------
        return memory;
    }

    private void writeSequences(ArrayList<String[]> temp, String fileName) {
        int counter = 0;
        int start;
        int end = 0;
        String base;
        String dest;
        //--------------------------------------------------------
        base = temp.get(counter)[0];
        dest = base;
        ArrayList<String> residue = new ArrayList<>();
        //---------------------------------------------------------
        while (counter < temp.size()) {
            try {
                start = Integer.parseInt(temp.get(counter)[2].trim());
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, "Error in reading start point from file : " + fileName + " -> " + e.getMessage());
                DsspGui.txtAreaLog.append("Error in reading start point from file : " + fileName + " -> " + e.getMessage() + "\n");
                DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                writeData(logFile, true, "Error in reading start point from file : " + fileName + " -> " + e.getMessage());
                start = -1;
            }
            while (base.equals(dest) && counter < temp.size()) {
                try {
                    end = Integer.parseInt(temp.get(counter)[2].trim());
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Error in reading end point from file : " + fileName + " -> " + e.getMessage());
                    DsspGui.txtAreaLog.append("Error in reading end point from file : " + fileName + " -> " + e.getMessage() + "\n");
                    DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                    writeData(logFile, true, "Error in reading end point from file : " + fileName + " -> " + e.getMessage());

                    end = -1;
                }
                residue.add(temp.get(counter)[1]);
                counter++;
                if (counter < temp.size()) {
                    dest = temp.get(counter)[0];
                }
            }
            if (residue.size() >= 4) {
                writeSequenceData(base, fileName, residue, start, end);
                residue.clear();
            } else {
                residue.clear();
            }
            if (counter < temp.size()) {
                base = temp.get(counter)[0];
            }

        }


    }

    private ArrayList<String[]> readTmpFile(File file) {
        BufferedReader reader;
        String currentLine;
        ArrayList<String[]> memoryForCutters = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));

            while ((currentLine = reader.readLine()) != null) {
                //Reading structure
                String[] cutters = new String[3];
                cutters[0] = currentLine.substring(16, 17);
                cutters[1] = currentLine.substring(13, 14);
                cutters[2] = currentLine.substring(7, 10);

                memoryForCutters.add(cutters);


            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in reading file: " + file.getAbsolutePath() + " -> " + e.getMessage());
        }
        return memoryForCutters;

    }


    private void writeSequenceData(String base, String fileName, ArrayList<String> residue, int start, int end) {
        File file = null;
        String sequence = "";
        for (String str : residue) {
            sequence += str;
        }
        switch (base) {
            case "H":
                file = new File(midFilesAddress + "H.txt");
                break;
            case "E":
                file = new File(midFilesAddress + "E.txt");
                break;
            case "C":
                file = new File(midFilesAddress + "C.txt");
                break;
        }
        FileWriter writer;
        try {


            writer = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(writer);
            if (!file.exists()) {
                file.createNewFile();
            }

            bw.write(sequence + "  " + fileName + "   " + start + "   " + end);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in writing sequence in file: " + file.getAbsoluteFile() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in writing sequence in file: " + file.getAbsoluteFile() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in writing sequence in file: " + file.getAbsoluteFile() + " -> " + e.getMessage());

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

    private File makeFile(String location, String name) {
        File file = new File(location + name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error in making file : " + file.getAbsoluteFile() + " -> " + e.getMessage());
                DsspGui.txtAreaLog.append("Error in making file : " + file.getAbsoluteFile() + " -> " + e.getMessage() + "\n");
                DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
                writeData(logFile, true, "Error in making file : " + file.getAbsoluteFile() + " -> " + e.getMessage());
            }
        }
        return file;

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

    private void makeDir(String address) {
        File tempFolder = new File(address);

        if (!tempFolder.exists()) {
            LOGGER.log(Level.INFO, "Making directory : " + tempFolder.getAbsoluteFile());
            DsspGui.txtAreaLog.append("Making directory : " + tempFolder.getAbsoluteFile() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            tempFolder.mkdir();
        }//edn of if
        else {
            deleteDir(tempFolder);
            tempFolder.mkdir();
        }

    }

    private void writeData(File file, boolean keepFile, String data) {
        FileWriter writer;
        try {
            if (keepFile) {
                writer = new FileWriter(file, keepFile);
            } else {
                writer = new FileWriter(file);
            }
            BufferedWriter bw = new BufferedWriter(writer);
            if (!file.exists()) {
                file.createNewFile();

            }
            bw.write(data);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Problem in writing data to file: " + file.getName() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Problem in writing data to file: " + file.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            JOptionPane.showMessageDialog(null, "Problem in writing data to file: " + file.getName() + " -> " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);


        }

    }

    private void writeChainData(File file, boolean keepFile, String data) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer;
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
            LOGGER.log(Level.SEVERE, "Error in writing chain data in file :" + file.getName() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Error in writing chain data in file :" + file.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Error in writing chain data in file :" + file.getName() + " -> " + e.getMessage());

        }
    }


    private void readDsspFile(File currentFile) {
        BufferedReader reader;
        String currentLine;
        try {
            reader = new BufferedReader(new FileReader(currentFile));
            currentLine = reader.readLine();

            while (!currentLine.contains("#")) {
                currentLine = reader.readLine();

            }//end of while


            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.substring(13, 15).equals("!*") || currentLine.substring(13, 14).equals("!")) {
                    continue;
                }

                //H,G,I ->H
                String temp = currentLine;
                String pointChar = currentLine.substring(16, 17);
                switch (pointChar) {
                    case "H":
                    case "G":
                    case "I":
                        temp = temp.substring(0, 16) + "H" + temp.substring(17);
                        writeChainData(new File(tempFolderAddress + currentFile.getName().replace(".dssp", "") + currentLine.substring(11, 12).toLowerCase() + ".dssp"), true, temp);
                        break;
                    case "B":
                    case "E":
                        temp = temp.substring(0, 16) + "E" + temp.substring(17);
                        writeChainData(new File(tempFolderAddress + currentFile.getName().replace(".dssp", "") + currentLine.substring(11, 12).toLowerCase() + ".dssp"), true, temp);
                        break;
                    case "C":
                    case "S":
                    case "T":
                    case " ":
                        temp = temp.substring(0, 16) + "C" + temp.substring(17);
                        writeChainData(new File(tempFolderAddress + currentFile.getName().replace(".dssp", "") + currentLine.substring(11, 12).toLowerCase() + ".dssp"), true, temp);
                        break;

                }


            }//end of while
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Problem reading DSSP File: " + currentFile.getName() + " -> " + e.getMessage());
            DsspGui.txtAreaLog.append("Problem reading DSSP File: " + currentFile.getName() + " -> " + e.getMessage() + "\n");
            DsspGui.txtAreaLog.update(DsspGui.txtAreaLog.getGraphics());
            writeData(logFile, true, "Problem reading DSSP File: " + currentFile.getName() + " -> " + e.getMessage());
        }


    }


/*
    public static void main(String[] args) {

        //making the UI
        JFrame jFrame = new JFrame("Dssp Reader");
        JPanel jPanel = new JPanel();
        final JButton jButton = new JButton("Start");
        final JTextField dsspAddress = new JTextField(20);
        final JTextField midFileListAddress = new JTextField(20);
        final JCheckBox isListExist = new JCheckBox();
        /////////////////////////////////////////////////
        jPanel.add(jButton);
        jPanel.add(dsspAddress);
        jPanel.add(midFileListAddress);
        jPanel.add(isListExist);
        dsspAddress.setText("Enter the address....");
        dsspAddress.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dsspAddress.setText("");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dsspAddress.setText("");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dsspAddress.setText("");
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Timer timer = new Timer();
                timer.start();
                String fileLocation = dsspAddress.getText().trim();
                LOGGER.info("You entered this addresse : " + fileLocation + " ");
                LOGGER.info("All files would be created in this address.");
                tempFolderAddress = fileLocation + System.getProperty("file.separator") + "tempfiles" + System.getProperty("file.separator");
                midFilesAddress = fileLocation + System.getProperty("file.separator") + "midFiles" + System.getProperty("file.separator");
                resultFiles = fileLocation + System.getProperty("file.separator") + "resultFiles" + System.getProperty("file.separator");
                partialResult = fileLocation + System.getProperty("file.separator") + "partialResult" + System.getProperty("file.separator");
                logFileAddress = fileLocation + System.getProperty("file.separator") + "log" + System.getProperty("file.separator");
                tempListAddress = new File(midFileListAddress.getText().trim());
                boolean flag = isListExist.isSelected();
                new Runner().run(fileLocation, flag);
                timer.end();
                dsspAddress.setText(timer.getTotalTime() + "");
            }
        });
        jFrame.add(jPanel);
        jFrame.setSize(300, 150);
        int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2);
        int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
        jFrame.setLocation(width, height);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }*/


}


