package com.marginallyclever.communications;

import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;
import jssc.*;

import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public final class SerialConnection implements MarginallyCleverSerialPortEventListener {
    private static final int BAUD_RATE = 57600;
    private SerialPort serialPort;
    private String[] portsDetected;
    private boolean portOpened=false;
    private boolean portConfirmed=false;
    static private final String cue = "> ";
    static private final String hello = "HELLO WORLD! I AM DRAWBOT #";
    static private final String nochecksum = "NOCHECKSUM";
    static private final String badchecksum = "BADCHECKSUM ";
    static private final String badlinenum = "BADLINENUM ";
    // parsing input from Makelangelo
    private String serial_recv_buffer="";
    // prevent repeating pings from appearing in console
    boolean lastLineWasCue=false;
    private String recentPort;
    private final Preferences prefs;
    private final MainGUI mainGUI;
    private final MultilingualSupport translator;
    private final MachineConfiguration machine;

    public SerialConnection(Preferences prefs, MainGUI mainGUI, MultilingualSupport translator, MachineConfiguration machine) {
        this.prefs = prefs;
        this.mainGUI = mainGUI;
        this.translator = translator;
        this.machine = machine;
        loadRecentPortFromPreferences(); //FIXME smelly
    }

    @Override
    public void ClosePort() {
        if(portOpened) {
            if (serialPort != null) {
                try {
                    serialPort.removeEventListener();
                    serialPort.closePort();
                } catch (SerialPortException e) {}
            }
            portOpened=false;
            portConfirmed=false;
        }
    }

    // open a serial connection to a device.  We won't know it's the robot until
    @Override
    public int OpenPort(String portName) {
        if(portOpened && portName.equals(recentPort)) return 0;

        ClosePort();

        mainGUI.Log("<font color='green'>" + translator.get("ConnectingTo") + portName + "...</font>\n");

        // open the port
        serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();// Open serial port
            serialPort.setParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            mainGUI.Log("<span style='color:red'>" + translator.get("PortNotConfigured") + e.getMessage() + "</span>\n");
            return 3;
        }

        mainGUI.Log("<span style='color:green'>" + translator.get("PortOpened") + "</span>\n");
        SetRecentPort(portName);
        portOpened=true;
        lastLineWasCue=false;
        mainGUI.updateMenuBar();
        mainGUI.PlayConnectSound();

        return 0;
    }


    /**
     * Check if the robot reports an error and if so what line number.
     * @return -1 if there was no error, otherwise the line number containing the error.
     */
    protected int ErrorReported() {
        if(portConfirmed==false) return -1;

        if( serial_recv_buffer.lastIndexOf(nochecksum) != -1 ) {
            String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(nochecksum) + nochecksum.length());
            String x=GetNumberPortion(after_error);
            return Integer.decode(x);
        }
        if( serial_recv_buffer.lastIndexOf(badchecksum) != -1 ) {
            String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(badchecksum) + badchecksum.length());
            String x=GetNumberPortion(after_error);
            return Integer.decode(x);
        }
        if( serial_recv_buffer.lastIndexOf(badlinenum) != -1 ) {
            String after_error = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(badlinenum) + badlinenum.length());
            String x=GetNumberPortion(after_error);
            return Integer.decode(x);
        }

        return -1;
    }


    /**
     * Complete the handshake, load robot-specific configuration, update the menu, repaint the preview with the limits.
     * @return true if handshake succeeds.
     */
    public boolean ConfirmPort() {
        if(portConfirmed==true) return true;
        if(serial_recv_buffer.lastIndexOf(hello) < 0) return false;

        portConfirmed=true;

        String after_hello = serial_recv_buffer.substring(serial_recv_buffer.lastIndexOf(hello) + hello.length());
        machine.ParseRobotUID(after_hello);

        mainGUI.getMainframe().setTitle(translator.get("TitlePrefix")
                + Long.toString(machine.GetUID())
                + translator.get("TitlePostfix"));

        mainGUI.SendConfig();
        mainGUI.getPreviewPane().updateMachineConfig();

        mainGUI.updateMenuBar();
        mainGUI.getPreviewPane().setConnected(true);

        // rebuild the drive pane so that the feed rates are correct.
        mainGUI.setDrivePane(mainGUI.DriveManually());

        return true;
    }

    // find all available serial ports for the settings->ports menu.
    @Override
    public String[] ListSerialPorts() {
        String OS = System.getProperty("os.name").toLowerCase();

        if(OS.indexOf("mac") >= 0){
            portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("OS X");
        } else if(OS.indexOf("win") >= 0) {
            portsDetected = SerialPortList.getPortNames("COM");
            //System.out.println("Windows");
        } else if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0){
            portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("Linux/Unix");
        } else {
            System.out.println("OS ERROR");
            System.out.println("OS NAME="+System.getProperty("os.name"));
        }
        return portsDetected;
    }

    // Deal with something robot has sent.
    @Override
    public void serialEvent(SerialPortEvent events) {
        if(events.isRXCHAR()) {
            try {
                int len = events.getEventValue();
                byte[] buffer = serialPort.readBytes(len);
                String line2 = new String(buffer,0,len);

                serial_recv_buffer+=line2;
                // wait for the cue ("> ") to send another command
                if(serial_recv_buffer.lastIndexOf(cue)!=-1) {
                    String line2_mod = serial_recv_buffer;
                    //line2_mod = line2.mod.replace("\n", "");
                    //line2_mod = line2_mod.replace(">", "");
                    line2_mod = line2_mod.trim();
                    if(line2_mod.length()>0) {
                        if(line2_mod.equals(cue.trim())) {
                            if(lastLineWasCue==true) {
                                // don't repeat the ping
                                //Log("<span style='color:#FF00A5'>"+line2_mod+"</span>");
                            } else {
                                mainGUI.Log("<span style='color:#FFA500'>" + line2_mod + "</span>");
                            }
                            lastLineWasCue=true;
                        } else {
                            lastLineWasCue=false;
                            mainGUI.Log("<span style='color:#FFA500'>" + line2_mod + "</span>");
                        }
                    }

                    int error_line = ErrorReported();
                    if(error_line != -1) {
                        mainGUI.getGcodeFile().linesProcessed = error_line;
                        serial_recv_buffer="";
                        mainGUI.SendFileCommand();
                    } else if(ConfirmPort()) {
                        serial_recv_buffer="";
                        mainGUI.SendFileCommand();
                    }
                }
            } catch (SerialPortException e) {}
        }
    }

    // connect to the last port
    @Override
    public void reconnectToLastPort() {
        ListSerialPorts();
        if(Arrays.asList(portsDetected).contains(recentPort)) {
            OpenPort(recentPort);
        }
    }

    /**
     *
     * @param src
     * @return
     */
    private String GetNumberPortion(String src) {
        int length = src.length();
        String result = "";
        for (int i = 0; i < length; i++) {
            Character character = src.charAt(i);
            if (Character.isDigit(character)) {
                result += character;
            }
        }
        return result;
    }


    // pull the last connected port from prefs
    private void loadRecentPortFromPreferences() {
        recentPort = prefs.get("recent-port", "");
    }

    // update the prefs with the last port connected and refreshes the menus.
    // TODO: only update when the port is confirmed?
    public void SetRecentPort(String portName) {
        prefs.put("recent-port", portName);
        recentPort=portName;
        //UpdateMenuBar(); FIXME
    }

    /**
     *
     * @return <code>true</code> if the serial port has been confirmed; <code>false</code> otherwise
     */
    @Override
    public boolean isPortConfirmed() {
        return portConfirmed;
    }

    /**
     *
     * @return the serial port of this serial connection.
     */
    @Override
    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     *
     * @return the serial ports detected for this serial connection.
     */
    @Override
    public String[] getPortsDetected() {
        return portsDetected;
    }

    /**
     *
     * @return the port open for this serial connection.
     */
    @Override
    public boolean isPortOpened() {
        return portOpened;
    }

    /**
     *
     * @return the most recent port used by this serial connection.
     */
    @Override
    public String getRecentPort() {
        return recentPort;
    }
}