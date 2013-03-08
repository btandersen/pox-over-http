/*
 * Brandon Andersen
 * btanders@asu.edu
 * 1000878186
 * 
 * CSE 598
 * Spring 2013
 * Professor Calliss
 * 
 * Assignment - POX over HTTP
 * 
 * FoodItemsProxy
 * This is the proxy class used by a Java-based client to access the food items
 * resources
 */
package com.asu.cse598.btanders.poxfoodmenubtandersnetbeans7;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;

/**
 * Jersey REST client generated for REST resource:FoodItemsResource
 * [FoodItems]<br> USAGE:
 * <pre>
 *        FoodItemsProxy client = new FoodItemsProxy();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author brandon
 */
public class FoodItemsProxy
{
    private WebResource webResource;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/POX-FoodMenu-btanders-Netbeans-7.2.1/webresources";
    
    /**
     * Constructor for creating a new proxy
     */
    public FoodItemsProxy()
    {
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        webResource = client.resource(BASE_URI).path("FoodItems");
    }
    
    /**
     * getFoodItem - Allows a client to request one or more food items using URI tunneling
     * @param <T> - The response type desired
     * @param responseType - The response type
     * @param foodItemId - an array list of Strings that should represent the integer id's of the requested food items
     * @return - the response based on the type requested
     * @throws UniformInterfaceException 
     */
    public <T> T getFoodItem(Class<T> responseType, ArrayList<String> foodItemId) throws UniformInterfaceException
    {
        WebResource resource = webResource;
        if (foodItemId != null) // make sure the user passed a request
        {
            for (int i = 0; i < foodItemId.size(); ++i)
            {
                resource = resource.queryParam("foodItemId", foodItemId.get(i)); // add each passed id as a query parameter
            }
        }
        resource = resource.path("GetFoodItem"); // set the path to the resource
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType); // make a GET request and return the response
    }
    
    /**
     * getFoodItemByPost - The POX over HTTP version for getting reqeusted food items
     * This version uses POX over HTTP as discussed in "REST in Practice" section 3.5
     * where all messages are sent as POSTs with an XML message in the body.
     * @param requestEntity - the request as an XML message per the specification of the assignment
     * @return - the response containing an XML message per the specification of the assignment
     * @throws UniformInterfaceException 
     */
    public ClientResponse getFoodItemByPost(Object requestEntity) throws UniformInterfaceException
    {
        // POST the request and return the response...
        return webResource.path("GetFoodItemByPost").type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }
    /**
     * addFoodItem - The POX over HTTP implementation allowing a client to request a food item be added
     * using a POST request with an XML message in the body per the specification of the assignment
     * @param requestEntity - the request as an XML message per the specification of the assignment
     * @return - the response as an XML message per the specification of the assignment
     * @throws UniformInterfaceException 
     */
    public ClientResponse addFoodItem(Object requestEntity) throws UniformInterfaceException
    {
        // POST the request and return the response...
        return webResource.path("AddFoodItem").type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }
    /**
     * close - clean up the client when finished
     */
    public void close()
    {
        client.destroy();
    }
}
