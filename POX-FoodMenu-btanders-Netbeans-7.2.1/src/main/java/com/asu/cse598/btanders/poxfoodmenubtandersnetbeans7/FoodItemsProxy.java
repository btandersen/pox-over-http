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
    
    public FoodItemsProxy()
    {
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        client = Client.create(config);
        webResource = client.resource(BASE_URI).path("FoodItems");
    }
    
    public <T> T getFoodItem(Class<T> responseType, ArrayList<String> foodItemId) throws UniformInterfaceException
    {
        WebResource resource = webResource;
        if (foodItemId != null)
        {
            for (int i = 0; i < foodItemId.size(); ++i)
            {
                resource = resource.queryParam("foodItemId", foodItemId.get(i));
            }
        }
        resource = resource.path("GetFoodItem");
        return resource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }
    
    public ClientResponse getFoodItemByPost(Object requestEntity) throws UniformInterfaceException
    {
        return webResource.path("GetFoodItemByPost").type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }
    
    public ClientResponse addFoodItem(Object requestEntity) throws UniformInterfaceException
    {
        return webResource.path("AddFoodItem").type(javax.ws.rs.core.MediaType.APPLICATION_XML).post(ClientResponse.class, requestEntity);
    }
    
    public void close()
    {
        client.destroy();
    }
}
