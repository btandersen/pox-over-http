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

/**
 *
 * @author brandon
 */
public class FoodItem
{
    public String country;
    public Integer id;
    public String name;
    public String description;
    public String category;
    public Double price;

    public FoodItem()
    {
        this.country = null;
        this.id = null;
        this.name = null;
        this.description = null;
        this.category = null;
        this.price = null;
    }

    private String toXml()
    {
        String result = "";

        if (null != name)
        {
            result += "<FoodItem country=\"" + this.country + "\">\n";
            result += "<id>" + this.id + "</id>\n";
            result += "<name>" + this.name + "</name>\n";
            result += "<description>" + this.description + "</description>\n";
            result += "<category>" + this.category + "</category>\n";
            result += "<price>" + this.price + "</price>\n";
            result += "</FoodItem>\n";
        }
        else
        {
            result += "<InvalidFoodItem>\n";
            result += "<FoodItemId>" + this.id + "</FoodItemId>\n";
            result += "</InvalidFoodItem >\n";
        }

        return result;
    }

    public boolean isValidFoodItem()
    {
        return (this.country != null
                && this.name != null
                && this.description != null
                && this.category != null
                && this.price != null);
    }

    @Override
    public String toString()
    {
        return this.toXml();
    }
}
