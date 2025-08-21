package com.github.koosty.gatling.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple TCP echo server for testing purposes.
 * It listens on a specified port and echoes back messages received from clients.
 * Messages are prefixed with a 2-byte length header.
 */
public class TcpTestServer {
    final int port;
    static final AtomicBoolean running = new AtomicBoolean(true);
    public TcpTestServer(int port) {
        this.port = port;
        run();
    }
    public void run(){
        ServerThread serverThread = new ServerThread(port);
        serverThread.start();
    }
    public void stop() {
        running.set(false);
    }

    /**
     * A thread class that handles the server's main functionality.
     * It listens for client connections and echoes back received messages.
     */
    static class ServerThread extends Thread {
        int port;

        /**
         * Constructor to initialize the server thread with a specific port.
         *
         * @param port The port number on which the server will listen.
         */
        public ServerThread(int port) {
            this.port = port;
        }

        /**
         * The main logic of the server thread.
         * It listens for client connections, reads messages with a 2-byte length header,
         * and echoes them back to the client.
         */
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(this.port)) {
                System.out.println("Echo Server started on port " + port);

                while (running.get()) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                        InputStream in = clientSocket.getInputStream();
                        OutputStream out = clientSocket.getOutputStream();

                        while (true) {
                            // Read 2-byte length header
                            byte[] header = new byte[2];
                            int bytesRead = 0;
                            while (bytesRead < 2) {
                                int read = in.read(header, bytesRead, 2 - bytesRead);
                                if (read == -1) {
                                    System.out.println("Client disconnected");
                                    break;
                                }
                                bytesRead += read;
                            }
                            if (bytesRead < 2) break; // Connection closed

                            // Convert header to message length
                            int messageLength = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);

                            // Read the message
                            byte[] message = new byte[messageLength];
                            bytesRead = 0;
                            while (bytesRead < messageLength) {
                                int read = in.read(message, bytesRead, messageLength - bytesRead);
                                if (read == -1) {
                                    throw new IOException("Connection closed before reading full message");
                                }
                                bytesRead += read;
                            }

                            // Convert message to string for logging (assuming UTF-8 text)
                            String inputLine = new String(message, StandardCharsets.UTF_8);
                            System.out.println("Received: " + inputLine);

                            // Send response with length header
                            byte[] inputLineBytes = inputLine.getBytes(StandardCharsets.UTF_8);
                            byte[] lengthHeader = createLengthHeader(inputLineBytes.length);
                            byte[] result = new byte[lengthHeader.length + inputLineBytes.length];
                            System.arraycopy(lengthHeader, 0, result, 0, lengthHeader.length);
                            System.arraycopy(inputLineBytes, 0, result, lengthHeader.length, inputLineBytes.length);
                            System.out.println("Returning: " + result.length);
                            out.write(result);
                            out.flush();
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port " + port);
            }
        }

        /**
         * Creates a 2-byte length header for a given message length.
         *
         * @param messageLength The length of the message.
         * @return A 2-byte array representing the length header.
         */
        private byte[] createLengthHeader(int messageLength) {
            return new byte[] {
                    (byte) ((messageLength >> 8) & 0xFF), // High byte
                    (byte) (messageLength & 0xFF)        // Low byte
            };
        }
    }
}
