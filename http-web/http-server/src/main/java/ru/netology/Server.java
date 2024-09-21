package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9999;
    private static final int THREAD_POOL = 64;
    private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL);

    void start() {
        try (final var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            final var requestLine = in.readLine();
            if (requestLine == null || !isValidRequest(requestLine)) {
                sendResponse(out, "404 Not Found", "text/plain", 0);
                return;
            }

             Request request = new Request(requestLine);
            String path = request.getPath();
            if (!VALID_PATHS.contains(path)) {
                sendResponse(out, "404 Not Found", "text/plain", 0);
                return;
            }
            serveFile(out, path, request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidRequest(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length == 3 && "GET".equalsIgnoreCase(parts[0]) &&
                parts[2].equals("HTTP/1.1");
    }

    private void serveFile(OutputStream out, String path, Request request) throws IOException {
        var filePath = Path.of(".", "public", path);
        var mimeType = Files.probeContentType(filePath);

        if (path.equals("/classic.html")) {
            String template = Files.readString(filePath);
            String content = template.replace("{time}", LocalDateTime.now().toString());
            sendResponse(out, "200 OK", mimeType, content.getBytes().length);
            out.write(content.getBytes());
        } else {
            long length = Files.size(filePath);
            sendResponse(out, "200 OK", mimeType, length);
            Files.copy(filePath, out);
        }
        out.flush();
    }

    private void sendResponse(OutputStream out, String status, String contentType, long contentLength) throws IOException {
        out.write(("HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes());
    }

    private void shutdown() {
        try {
            threadPool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
