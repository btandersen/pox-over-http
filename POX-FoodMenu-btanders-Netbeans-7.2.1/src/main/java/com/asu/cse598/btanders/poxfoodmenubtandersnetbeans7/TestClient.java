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
 * TestClient
 * 
 * Simple Java program with a main method that instantiates the web service
 * food items proxy and makes various add and get requests, printing the results
 * to the console or output pane on Netbeans.
 */
package com.asu.cse598.btanders.poxfoodmenubtandersnetbeans7;

import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;

/**
 *
 * @author brandon
 */
public class TestClient
{
    public static void main(String[] args)
    {
        FoodItemsProxy foodProxy = new FoodItemsProxy();

        String addFoodItem1 = "";
        String addFoodItem2 = "";
        String addFoodItem3 = "";

        // A new food item to add...
        addFoodItem1 += "<NewFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        addFoodItem1 += "<FoodItem country=\"MX\">";
        addFoodItem1 += "<name>Taco</name>";
        addFoodItem1 += "<description>A traditional Mexican dish usually made with corn or flour tortillas containing a variety of fillings.</description>";
        addFoodItem1 += "<category>Lunch</category>";
        addFoodItem1 += "<price>2.00</price>";
        addFoodItem1 += "</FoodItem>";
        addFoodItem1 += "</NewFoodItems >";

        // A repeat of the item above to see if we can trick the system into inserting it twice...
        addFoodItem2 += "<NewFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        addFoodItem2 += "<FoodItem country=\"MX\">";
        addFoodItem2 += "<name>Taco</name>";
        addFoodItem2 += "<description>A traditional Mexican dish usually made with corn or flour tortillas containing a variety of fillings.</description>";
        addFoodItem2 += "<category>Lunch</category>";
        addFoodItem2 += "<price>2.00</price>";
        addFoodItem2 += "</FoodItem>";
        addFoodItem2 += "</NewFoodItems >";

        // Another new dish, but with malformed XML...
        addFoodItem3 += "<NewFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        addFoodItem3 += "<FoodItem country=\"IN\">";
        addFoodItem3 += "<name>Dal makhani"; // missing the closing tag for the name... too bad since this dish is tasty
        addFoodItem3 += "<description>Whole black lentil and red kidney beans in a ruch sauce</description>";
        addFoodItem3 += "<category>Dinner</category>";
        addFoodItem3 += "<price>9.99</price>";
        addFoodItem3 += "</FoodItem>";
        addFoodItem3 += "</NewFoodItems >";

        String getFoodItem1 = "";
        String getFoodItem2 = "";
        String getFoodItem3 = "";

        // A get request using an XML message, should succeed with 200...
        getFoodItem1 += "<SelectedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        getFoodItem1 += "<FoodItemId>100</FoodItemId>"; // Steak and Kidney Pie
        getFoodItem1 += "<FoodItemId>105</FoodItemId>"; // Traditional English Breakfast
        getFoodItem1 += "<FoodItemId>302</FoodItemId>"; // This should be the item we added above, Taco...
        getFoodItem1 += "</SelectedFoodItems >";

        // A get request using an XML message, this one should partially succeed with 404...
        getFoodItem2 += "<SelectedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        getFoodItem2 += "<FoodItemId>999</FoodItemId>"; // Should not exist so an invalid food item...
        getFoodItem2 += "<FoodItemId>100</FoodItemId>"; // Steak and Kidney Pie
        getFoodItem2 += "</SelectedFoodItems >";

        // A get request using an XML message, this one won't succeed -> 404...
        getFoodItem3 += "<SelectedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        getFoodItem3 += "<FoodItemId>999</FoodItemId>"; // Should notr exist so an invalid food item...
        getFoodItem3 += "</SelectedFoodItems >";

        // This one is for the URI tunneling version of the get request, should succeed with 200...
        ArrayList<String> foodItemIdList1 = new ArrayList<String>();
        foodItemIdList1.add("100"); // Steak and Kidney Pie
        foodItemIdList1.add("105"); // Traditional English Breakfast
        foodItemIdList1.add("302"); // This should be the item we added above, Taco...

        // This one is for the URI tunneling version of the get request, this one should partially succeed with 404...
        ArrayList<String> foodItemIdList2 = new ArrayList<String>();
        foodItemIdList2.add("999"); // Should not exist so an invalid food item...
        foodItemIdList2.add("100"); // Steak and Kidney Pie

        // This one is for the URI tunneling version of the get request, this one won't succeed -> 404...
        ArrayList<String> foodItemIdList3 = new ArrayList<String>();
        foodItemIdList3.add("999"); // Should notr exist so an invalid food item...

        try
        {
            System.out.println("\nAdding a new food item, should succeed with 200 OK...\n");
            ClientResponse c = foodProxy.addFoodItem(addFoodItem1);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));

            System.out.println("\nAdding the same food item again, should not succedd -> 409 Conflict...\n");
            c = foodProxy.addFoodItem(addFoodItem2);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));

            System.out.println("\nAdding a new food item with malformed XML, should not succedd -> 400 Invalid or incorrect input message...\n");
            c = foodProxy.addFoodItem(addFoodItem3);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        try
        {
            // POX over HTTP version of get request...
            System.out.println("\nTime to make some POX over HTTP get requests...\n");
            System.out.println("\nGetting three food items, should succeed with 200 OK\n");
            ClientResponse c = foodProxy.getFoodItemByPost(getFoodItem1);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
            
            System.out.println("\nGetting two food items, the first of which does not exist, should partially succeed with 404 Not Found\n");
            c = foodProxy.getFoodItemByPost(getFoodItem2);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
            
            System.out.println("\nGetting one food item that does not exist, should not succeed -> 404 Not Found\n");
            c = foodProxy.getFoodItemByPost(getFoodItem3);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));

            // URI tunneling version of get request
            System.out.println("\nNow we will make get requests (the same as above) but using URI tunneling...\n");
            System.out.println("\nGetting three food items, should succeed with 200 OK\n");
            c = foodProxy.getFoodItem(ClientResponse.class, foodItemIdList1);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
            
            System.out.println("\nGetting two food items, the first of which does not exist, should partially succeed with 404 Not Found\n");
            c = foodProxy.getFoodItem(ClientResponse.class, foodItemIdList2);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
            
            System.out.println("\nGetting one food item that does not exist, should not succeed -> 404 Not Found\n");
            c = foodProxy.getFoodItem(ClientResponse.class, foodItemIdList3);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        foodProxy.close();
        
        System.out.println("\nThe client is exiting...\n");
    }
}
