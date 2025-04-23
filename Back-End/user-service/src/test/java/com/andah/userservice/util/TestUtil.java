package com.andah.userservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Utility class for testing controllers
 */
public class TestUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create a POST request with a JSON body
     */
    public static MockHttpServletRequestBuilder postJson(String uri, Object body) throws Exception {
        return MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
    
    /**
     * Create a PUT request with a JSON body
     */
    public static MockHttpServletRequestBuilder putJson(String uri, Object body) throws Exception {
        return MockMvcRequestBuilders.put(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
    
    /**
     * Authenticate a user and get JWT token (example implementation)
     */
    public static String getAuthToken(MockMvc mockMvc, String username, String password) throws Exception {
        // Implementation depends on your authentication controller
        // This is just a placeholder for the concept
        /*
        String response = mockMvc.perform(postJson("/api/auth/login", 
                Map.of("username", username, "password", password)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        return JsonPath.parse(response).read("$.token");
        */
        return "test-token";
    }
}
