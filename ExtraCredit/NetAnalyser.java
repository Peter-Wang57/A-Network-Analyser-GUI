/**
 * NetAnalyser.java
 * Completed the function of Extra Credit
 *
 * @version v3.0
 * @author Wang Kunpeng
 * @since 2020-5-15
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;


public class NetAnalyser {

    private static final String FILE_IN = "D:/";
    private SimpleDateFormat FILE_Time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private JFrame myFrame;
    private JPanel myPanel;
    private JTextArea myTextArea;
    private JLabel[] histogramLabelArr;
    private JTextField TextField;
    private JComboBox<Integer> jComboBox;
    private static Integer maxProbes = 0;

    /**
     * Initialization, including JFrame(1), JPanel(1), JTextArea(1), JTextField(1)
     * JComboBox(1), JLabel(7), JButton(1)
     */

    public void Init(){
        myFrame = new JFrame();
        myFrame.setTitle("NetAnalyser V2.0");
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setSize(1500, 600);

        myPanel = new JPanel();
        myPanel.setLayout(null);

        myFrame.setContentPane(myPanel);

        /**
         *  three Labels and one TextArea on the left part
         */
        myTextArea = new JTextArea();
        myTextArea.setBounds(360,20,400,20);
        myTextArea.setText("Your output will appear here...");
        myPanel.add(myTextArea);

        JLabel jLabel1 = new JLabel();
        jLabel1.setBounds(20,20,400,20);
        jLabel1.setText("Enter Test URL & no. probes and click on Process");
        myPanel.add(jLabel1);

        JLabel jLabel2 = new JLabel();
        jLabel2.setBounds(20,80,80,20);
        jLabel2.setText("Test URL");
        myPanel.add(jLabel2);

        JLabel jLabel3 = new JLabel();
        jLabel3.setBounds(80,140,100, 20);
        jLabel3.setText("No. of probes");
        myPanel.add(jLabel3);

        /**
         *  four Labels on right part
         */
        JLabel jLabel4 = new JLabel();
        jLabel4.setBounds(800, 80, 200, 20);
        jLabel4.setText("Histogram");
        myPanel.add(jLabel4);

        histogramLabelArr = new JLabel[3];
        JLabel hL1 = new JLabel();
        hL1.setBounds(800, 120,800,20);
        myPanel.add(hL1);
        histogramLabelArr[0] = hL1;

        JLabel hL2 = new JLabel();
        hL2.setBounds(800, 160,800,20);
        myPanel.add(hL2);
        histogramLabelArr[1] = hL2;

        JLabel hL3 = new JLabel();
        hL3.setBounds(800, 200,800,20);
        myPanel.add(hL3);
        histogramLabelArr[2] = hL3;

        /**
         * one TextField in the middle of frame
         */
        TextField = new JTextField();
        TextField.setBounds(100, 80, 200, 20);
        myPanel.add(TextField);

        /**
         *  For Task2 probes from 1 to maxProbes, which is given by users
         */
        jComboBox = new JComboBox<>();
        jComboBox.setBounds(180, 140,50, 20);
        for(int i=1; i<=maxProbes; i++){
            jComboBox.addItem(i);
        }
        myPanel.add(jComboBox);

        JButton jButton = new JButton("Process");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//action of Button
                ClickButton();
            }
        });

        jButton.setBounds(120, 220, 90, 40);
        myPanel.add(jButton);
    }

    /**
     * when the button is clicked, the following will happen
     */
    public void ClickButton(){
        String s = TextField.getText();//get the URL
        s=s.trim();
        if((!s.contains(".")) || s.isEmpty()){
            JOptionPane.showMessageDialog(myFrame, "The URL is invalid, please try again.");
            myTextArea.setText("");
        } else {
            int num = (int) jComboBox.getSelectedItem();//get the probes
            String oneLine="";
            ArrayList<String> arrMessage = new ArrayList<>();//the messages need to be shown on TextArea
            try {
                //the given recommended means of calling the ping command
                Process p = Runtime.getRuntime().exec("cmd /c ping " + "-n " + num + " " + s);
                p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                myTextArea.setBounds(myTextArea.getX(), 0, myTextArea.getWidth(), myFrame.getHeight());

                //split the reader line by line
                while((oneLine = reader.readLine()) != null){
                    arrMessage.add(oneLine);
                }

                myTextArea.setText("");//clear the TextArea
                for(int i=0; i<arrMessage.size(); i++){
                    myTextArea.append(arrMessage.get(i) + "\n");//show the message on TextArea
                }

                /**
                 * to build the histogram
                 */
                Build_Histogram(arrMessage, num, s);

            } catch (Exception e2){
                myTextArea.setText("Server cannot be reached.");
                System.out.println(e2.getMessage());
            }
        }
    }

    /**
     * to build the histogram
     * @param arrMessage
     * @param num
     */
    public void Build_Histogram(ArrayList<String> arrMessage, int num, String s){
        ArrayList<Integer> arrRTT = new ArrayList<>();//record the RTT in arrRTT
        for(int i=2; i<num+2; i++){//from arrMessage[2] is the detail message including each RTT
            int flag = 0;
            String tmp = arrMessage.get(i);
            for(int j=0; j<tmp.length(); j++){
                if(tmp.charAt(j) == '='){
                    if(flag == 0) flag++;//the first '=', continue
                    else {// the second '=', means the followed is RTT
                        int tRTT = 0;
                        int k = j+1;
                        while(Character.isDigit(tmp.charAt(k))){//Record consecutive numbers
                            tRTT *= 10;
                            tRTT += tmp.charAt(k) - '0';
                            k++;
                        }
                        arrRTT.add(tRTT);
                        break;
                    }
                }
            }
        }

        /**
         * There is no reply
         */
        if(arrRTT.size() <= 0){
            JOptionPane.showMessageDialog(myFrame, "The URL is invalid, please try again.");
            myTextArea.setText("");
        }

        /**
         * find the maxTime and minTime
         */
        int maxTime = arrRTT.get(0), minTime = arrRTT.get(0);
        for(int i=0; i<arrRTT.size(); i++){
            if(maxTime < arrRTT.get(i)){
                maxTime = arrRTT.get(i);
            }
            if(minTime > arrRTT.get(i)){
                minTime = arrRTT.get(i);
            }
        }

        //determine the three intervals
        double binSize = 1.0 * (maxTime - minTime) / 3.0;

        double b1 = (double) minTime;
        double b2 = b1 + binSize;
        double b3 = b2 + binSize;
        double b4 = (double) maxTime;

        int cnt1 = 0, cnt2 = 0, cnt3 = 0;//record the frequency of each intervals

        for(int i=0; i<arrRTT.size(); i++){
            double tmp = 1.0 * arrRTT.get(i);
            if(tmp >= b1 && tmp < b2) cnt1++;
            else if(tmp >= b2 && tmp < b3) cnt2++;
            else if(tmp >= b3 && tmp <= b4) cnt3++;
        }

        //show the histogram on the right of frame
        String sLine1 = "";
        sLine1 = String.format("%.2f", b1) + "<=RTT<" + String.format("%.2f", b2) + "  ";
        //make the b1~b4 as two decimal places
        for(int i=1; i<=cnt1; i++){//draw the histogram
            sLine1 = sLine1 + "  X  ";
        }
        histogramLabelArr[0].setText(sLine1);


        String sLine2 = "";
        sLine2 = String.format("%.2f", b2) + "<=RTT<" + String.format("%.2f", b3) + "  ";
        for(int i=1; i<=cnt2; i++){
            sLine2 = sLine2 + "  X  ";
        }
        histogramLabelArr[1].setText(sLine2);

        String sLine3 = "";
        sLine3 = String.format("%.2f", b3) + "<=RTT<=" + String.format("%.2f", b4);
        for(int i=1; i<=cnt3; i++){
            sLine3 = sLine3 + "  X  ";
        }
        histogramLabelArr[2].setText(sLine3);

        /**
         * Write the histogram data to a text file
         */
        String FILE_Name = s.replace('.','-');
        FILE_Name = FILE_Name + "-" + FILE_Time.format(new Date()) + ".txt";

        try{
            PrintStream PrSt = new PrintStream(new File(FILE_IN, FILE_Name));
            PrSt.println(FILE_Name);
            PrSt.println();
            PrSt.println("RTT(ms) histogram");
            PrSt.println(String.format("%.2f", b1) + '-' + String.format("%.2f", b2) + ':' + cnt1);
            PrSt.println(String.format("%.2f", b2) + '-' + String.format("%.2f", b3) + ':' + cnt2);
            PrSt.println(String.format("%.2f", b3) + '-' + String.format("%.2f", b4) + ':' + cnt3);
            PrSt.close();
        } catch(Exception e2){
            System.out.println(e2.getMessage());
        }

    }

    public static void main(String[] args){
        //judge the input probes valid or not
        if(args.length == 1){
            String Arg = args[0];
            try{
                maxProbes = Integer.valueOf(Arg);
            } catch (Exception e){
                System.out.println("You should give a number between 10 and 20 inclusive.");
                System.out.println("Please run the program again.");
                System.exit(1);
            }

            if(maxProbes < 10 || maxProbes > 20){
                System.out.println("You should give a number between 10 and 20 inclusive.");
                System.out.println("Please run the program again.");
                System.exit(1);
            }

        } else {
            System.out.println("You should give a number between 10 and 20 inclusive.");
            System.out.println("Please run the program again.");
            System.exit(1);
        }

        NetAnalyser myNetAnalyser = new NetAnalyser();
        EventQueue.invokeLater(new Runnable() {
            public void run(){
                myNetAnalyser.Init();
            }
        });
    }
}
