package seoulbin;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;

public class TestServer {
    public static void main(String[] args) throws IOException {
        URL resource = TestServer.class.getClassLoader().getResource("map.html");
        // 로컬 서버를 포트 8088에서 실행
        HttpServer server = HttpServer.create(new InetSocketAddress(8088), 0);
        server.createContext("/map", exchange -> {
            File file = new File(resource.getFile());
            if (!file.exists()) {
                String response = "404\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] fileBytes = fis.readAllBytes();
            fis.close();

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

