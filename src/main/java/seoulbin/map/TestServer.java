package seoulbin.map;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TestServer {
    public static void main(String[] args) throws IOException {
        // 로컬 서버를 포트 8088에서 실행
        HttpServer server = HttpServer.create(new InetSocketAddress(8088), 0);
        // 1. /map
        server.createContext("/map", exchange -> {
        	InputStream is = TestServer.class.getClassLoader().getResourceAsStream("map.html");
            if (is == null) {
                String response = "404 /map \n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] fileBytes = is.readAllBytes();
            is.close();

            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        });
        // 2. /app.js
        server.createContext("/app.js", exchange -> {
        	InputStream is = TestServer.class.getClassLoader().getResourceAsStream("app.js");
            if (is == null) {
                String response = "404 /app \n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] fileBytes = is.readAllBytes();
            is.close();

            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        });
        // 3. /recycle.png, /general.png
        server.createContext("/", exchange -> {
            // 요청한 경로 가져오기
            String requestedPath = exchange.getRequestURI().getPath();
            String filePath = "static" + requestedPath; // static 폴더 기준 경로 설정

            InputStream is = TestServer.class.getClassLoader().getResourceAsStream(filePath);
            if (is == null) {
                String response = "404 Not Found \n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] fileBytes = is.readAllBytes();
            is.close();

            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8088");
    }
}

