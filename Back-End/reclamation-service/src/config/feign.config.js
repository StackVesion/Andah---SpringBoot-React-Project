/**
 * Feign-like client configuration for the Node.js reclamation service
 * This allows the reclamation service to make authenticated requests to other services
 */

const fetch = require('node-fetch');

/**
 * Create a client for communicating with other services
 * @param {string} serviceName - The name of the service to communicate with
 * @param {string} token - The JWT token for authentication
 * @returns {Object} - Client object with methods for HTTP requests
 */
function createServiceClient(serviceName, token) {
    // Base URL from Eureka (when running in Docker)
    const baseUrl = `http://${serviceName}:8080/api`;
    
    // Headers to include in all requests
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
    
    // Add auth token if provided
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return {
        /**
         * Make a GET request to the service
         * @param {string} path - The path to request
         * @returns {Promise<Object>} - The response data
         */
        async get(path) {
            const response = await fetch(`${baseUrl}${path}`, {
                method: 'GET',
                headers
            });
            
            if (!response.ok) {
                throw new Error(`Error calling ${serviceName}: ${response.status} ${response.statusText}`);
            }
            
            return await response.json();
        },
        
        /**
         * Make a POST request to the service
         * @param {string} path - The path to request 
         * @param {Object} data - The data to send
         * @returns {Promise<Object>} - The response data
         */
        async post(path, data) {
            const response = await fetch(`${baseUrl}${path}`, {
                method: 'POST',
                headers,
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                throw new Error(`Error calling ${serviceName}: ${response.status} ${response.statusText}`);
            }
            
            return await response.json();
        },
        
        /**
         * Make a PUT request to the service
         * @param {string} path - The path to request
         * @param {Object} data - The data to send
         * @returns {Promise<Object>} - The response data
         */
        async put(path, data) {
            const response = await fetch(`${baseUrl}${path}`, {
                method: 'PUT',
                headers,
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                throw new Error(`Error calling ${serviceName}: ${response.status} ${response.statusText}`);
            }
            
            return await response.json();
        },
        
        /**
         * Make a DELETE request to the service
         * @param {string} path - The path to request
         * @returns {Promise<Object>} - The response data
         */
        async delete(path) {
            const response = await fetch(`${baseUrl}${path}`, {
                method: 'DELETE',
                headers
            });
            
            if (!response.ok) {
                throw new Error(`Error calling ${serviceName}: ${response.status} ${response.statusText}`);
            }
            
            return await response.json();
        }
    };
}

module.exports = {
    createServiceClient
};
