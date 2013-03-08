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
 * FoodItem
 * Simple Transfer Object-like class with all members public and no getters or 
 * setters. No transformations done to data members so all access is via direct 
 * assignment or reference. It does have one method to check validity of a
 * created instance.
 * 
 */
package com.asu.cse598.btanders.poxfoodmenubtandersnetbeans7;

/**
 *
 * @author brandon
 */
public class FoodItem
{
    // public members since no transformations or abstractions are needed
    public String country;
    public Integer id;
    public String name;
    public String description;
    public String category;
    public Double price;

    // Default constructor setting everything to null...
    public FoodItem()
    {
        this.country = null;
        this.id = null;
        this.name = null;
        this.description = null;
        this.category = null;
        this.price = null;
    }

    // private method to return an XML representation of the instance for the
    // toString() method...
    private String toXml()
    {
        String result = "";

        // if the name is null, then this is treated as an invalid food item
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

    // check to see if an instance is valid, that being all members except id
    // are not null...
    public boolean isValidFoodItem()
    {
        // don't care about id since that may be assigned at a later time after creation
        return (this.country != null
                && this.name != null
                && this.description != null
                && this.category != null
                && this.price != null);
    }

    // overridden toString method using the privte toXml method to represent the
    // instance as an XML message
    @Override
    public String toString()
    {
        return this.toXml();
    }
}
