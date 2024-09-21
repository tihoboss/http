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

import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private final String path;
    private final Map<String, List<String>> queryParams;

    public Request(String requestLine) {
        String[] parts = requestLine.split(" ");
        String[] pathAndQuery = parts[1].split("\\?");
        this.path = pathAndQuery[0];
        this.queryParams = parseQueryParams(pathAndQuery.length > 1 ? pathhAndQuery[1] : "");
    }

    private Map<String, List<String>> parseQueryParams(String queryString) {
        Map<String, List<String>> paramMap = new HashMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] keyValue = pair.split("=");
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = (keyValue.length > 1) ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            paramMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return paramMap;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
}