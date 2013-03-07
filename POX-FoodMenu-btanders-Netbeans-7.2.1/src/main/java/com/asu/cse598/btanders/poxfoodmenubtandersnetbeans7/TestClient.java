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

        String addFoodItem = "";

        addFoodItem += "<NewFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        addFoodItem += "<FoodItem country=\"G\">";
        addFoodItem += "<name>Cornish Pasty</name>";
        addFoodItem += "<description>Tender cubes of steak, potatoes and sweede wrapped in flakey short crust pastry.  Seasoned with lots of pepper.  Served with mashed potatoes, peas and a side of gravy</description>";
        addFoodItem += "<category>Dinner</category>";
        addFoodItem += "<price>15.95</price>";
        addFoodItem += "</FoodItem>";
//        addFoodItem += "<FoodItem country=\"GB\">";
//        addFoodItem += "<name>bangers and mash</name>";
//        addFoodItem += "<description>the yumzors!!!</description>";
//        addFoodItem += "<category>anytime</category>";
//        addFoodItem += "<price>9.99</price>";
//        addFoodItem += "</FoodItem>";
        addFoodItem += "</NewFoodItems >";

        String getFoodItem = "";

        getFoodItem += "<SelectedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>";
        getFoodItem += "<FoodItemId>100</FoodItemId>";
        getFoodItem += "<FoodItemId>156</FoodItemId>";
        getFoodItem += "<FoodItemId>302</FoodItemId>";
        getFoodItem += "</SelectedFoodItems >";

        try
        {

            ClientResponse c = foodProxy.addFoodItem(addFoodItem);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));

            c = foodProxy.addFoodItem(addFoodItem);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        try
        {
            ClientResponse c = foodProxy.getFoodItemByPost(getFoodItem);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));

            ArrayList<String> foodItemIdList = new ArrayList<String>();

            foodItemIdList.add("100");
            foodItemIdList.add("101");
            foodItemIdList.add("302");

            c = foodProxy.getFoodItem(ClientResponse.class, foodItemIdList);
            System.out.println(c.getStatus() + ": " + c.getClientResponseStatus());
            System.out.println(c.getEntity(String.class));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
