/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.integration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for all REST tests. It contains the URL, the creation of the client and
 * the cleanup of the responses.
 * 
 * @author jef
 */
class AbstractRestTest {
    protected static final String REST_URL = "http://localhost:8080/checklist/rest";
    
    Client client;
    Response response;

    @Before
    public void setup() {
        client = ClientBuilder.newBuilder().build();
    }

    @After
    public void tearDown() {
        if (response != null) {
            response.close(); // You should close connections!
        }
    }
    
}
