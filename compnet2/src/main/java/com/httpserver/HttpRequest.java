package com.httpserver;

import java.util.Map;
import java.util.List;

record HttpRequest(String method, 
            String url, 
            Map<String, 
            List<String>> header, 
            byte[] body) {
}
