package edu.gladstone.hts.server;

import edu.gladstone.hts.microscope.MicroscopeCommandHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandServer implements Runnable {

    private boolean Finished = false;
    private int Port = 9999;
    private MicroscopeCommandHandler CommandHandler;

    final Logger logger = LoggerFactory.getLogger(CommandServer.class);

    public void Launch(int port, MicroscopeCommandHandler commandHandler) {
        CommandHandler = commandHandler;
        Port = port;
        Thread thread = new Thread(this);
        thread.start();
    }

    private void LaunchServer(int port) {
        try {
            @SuppressWarnings("resource")
			ServerSocket socket = new ServerSocket(port);
            while (!Finished) {
                Socket client = socket.accept(); // wait for connection
                InputStream input = client.getInputStream();
                OutputStream output = client.getOutputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input));
                String command = reader.readLine();
                //logger.debug("Socket reader received : " + command);
                String result = ProcessCommand(command);

                if (result.equals("ok")) {
                } else {
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(result);
                }
            }

        } catch (IOException exception) {
            String message = "Error launching Socket Server: " + exception.getMessage();
            JOptionPane.showMessageDialog(null, message);
            logger.error(message);
        } catch (Exception e) {
            String message = "Non-IO error in socket server: " + e.getMessage();
            JOptionPane.showMessageDialog(null, message);
            logger.error(message);
        }
    }

    synchronized private void PublishRetractStageEvent() {
        CommandHandler.HandleRetractCommand();
    }

    synchronized private void PublishExtendStageEvent() {
        CommandHandler.HandleExtendCommand();
    }

    synchronized private void PublishImageEvent(String data) {
        CommandHandler.HandleImageCommand(data);
    }

    synchronized private String PublishStatusEvent() {
        return CommandHandler.HandleGetStatusCommand();
    }

    synchronized private void PublishAbortEvent() {
        CommandHandler.HandleAbortCommand();
    }

    synchronized public void Close() {
        Finished = true;
    }

    private String ProcessCommand(String command) {
        logger.trace("Server Command: " + command);

        if (command.equals("Extend Stage")) {
            PublishExtendStageEvent();
            return "ok";
        }
        if (command.equals("Retract Stage")) {
            PublishRetractStageEvent();
            return "ok";
        }
        if (command.equals("Status")) {
            return PublishStatusEvent();
        }
        if (command.startsWith("Image ")) {
            // Pass command which contains info for imaging
            PublishImageEvent(command);
            return "ok";
        }

        return "error";
    }

    @Override
    public void run() {
        LaunchServer(Port);
    }
}
