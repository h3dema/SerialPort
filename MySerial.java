/**
 * Classe para comunicação com porta serial
 *
 *
 * precisa de javax.com que pode ser encontrada em
 * http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-misc-419423.html#java_comm_api-30u1
 *
 */

import javax.comm.SerialPort;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;
import javax.comm.SerialPortEventListener;
import javax.comm.SerialPortEvent;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.Arrays;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

public class MySerial {

  public static final String NOMEAPLICACAO = "MySerial";
  public static final int TIMEOUT = 2000; // in milliseconds

  private SerialPort serialPort = null;
  private OutputStream out = null;
  private InputStream in = null;
  private List<String> mensagens = new ArrayList<>();

  class SerialRead implements SerialPortEventListener {
    public void serialEvent(SerialPortEvent ev) {
      if ((in != null) && (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE)) {
        // ler os dados da entrada
        byte[] b = new byte[2048];
        try {
        if (in.read(b) > 0) {
          String msg = Arrays.toString(b);
          mensagens.add(msg);
        }
        } catch (IOException e) {}
      }
    }
  }

  public MySerial(String nomePorta){
    CommPortIdentifier portId;
    Enumeration portList = CommPortIdentifier.getPortIdentifiers(); // obtem todas as portas no computador

    while (portList.hasMoreElements()) {
      portId = (CommPortIdentifier) portList.nextElement();
      // verifica se achou uma porta serial com o nome igual ao parâmetro do construtor
      if ((portId.getPortType() == CommPortIdentifier.PORT_SERIAL) && (portId.getName().equals(nomePorta))) {
        try {
          serialPort = (SerialPort) portId.open(NOMEAPLICACAO, TIMEOUT);
        } catch (PortInUseException e) {}
        try {
          //obtem a stream para envio de dados usada por write()
          out = serialPort.getOutputStream();
          in = serialPort.getInputStream();
          // configura a porta serial para comunicação
          serialPort.setSerialPortParams(9600,
                                         SerialPort.DATABITS_8,
                                         SerialPort.STOPBITS_1,
                                         SerialPort.PARITY_NONE);
          // registra um tratador para as mensagens de entrada
          SerialPortEventListener lsnr = new SerialRead();
          serialPort.addEventListener(lsnr);
        } catch (IOException | UnsupportedCommOperationException | TooManyListenersException e) { serialPort = null; }
        break;
      }
    }
  }

  public void write(String msg) {
    if (out == null) return;
    try {
      out.write(msg.getBytes());
    } catch (IOException e) {}
  }


  public static void main(String[] args) {
    String messageString = "Mensagem que quero mandar via serial.\n";
    MySerial ms = new MySerial("COM1");
    ms.write(messageString);
  }
}