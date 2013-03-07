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
 * FoodItemsResource
 * This class provides the POX 
 */
package com.asu.cse598.btanders.poxfoodmenubtandersnetbeans7;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * REST Web Service
 *
 * @author brandon
 */
@Path("FoodItems")
public class FoodItemsResource
{
    @Context
    private ServletContext context;
    private static int nextId;
    private static InputStream xmlFile = null;
    private static HashMap<Integer, FoodItem> foodItems = null;
    private static HashMap<String, Integer> reverseLookup = null;
    private static final Logger LOG = Logger.getLogger(FoodItemsResource.class.getName());

    /**
     * Creates a new instance of FoodItemsResource
     */
    public FoodItemsResource()
    {
        BasicConfigurator.configure();
        FoodItemsResource.nextId = -1;
    }

    private void init()
    {
        if (null == FoodItemsResource.xmlFile)
        {
            FoodItemsResource.xmlFile = this.context.getResourceAsStream("/xml/FoodItemData.xml");
        }

        if (null == FoodItemsResource.foodItems)
        {
            LOG.info("Initializing foodItems");
            FoodItemsResource.foodItems = this.readFoodItemsXml();

            if (null == FoodItemsResource.reverseLookup)
            {
                FoodItemsResource.reverseLookup = this.generateReverseLookup();
            }
        }
    }

    /**
     * Adds a food item
     *
     * @param content
     * @return an instance of java.lang.String
     */
    @POST
    @Path("AddFoodItem")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addFoodItem(String content)
    {
        ResponseBuilder builder;
        this.init();
        FoodItem result = this.parseFoodItem(content);

        if (null != result)
        {
            String resultStr = "<FoodItemAdded xmlns=”http://cse460.asu.edu/PoxAssignment”>\n";

            Integer id = FoodItemsResource.reverseLookup.get(result.name);

            if (null == id)
            {
                // FoodItem does not exist with that name already...
                synchronized (this)
                {
                    result.id = ++FoodItemsResource.nextId;
                }

                FoodItemsResource.foodItems.put(result.id, result);
                FoodItemsResource.reverseLookup.put(result.name, result.id);
                resultStr += "<FoodItemId>" + result.id + "</FoodItemId>\n";
                resultStr += "</FoodItemAdded>\n";
                builder = Response.ok(resultStr);
            }
            else
            {
                // Already exists...
                result = FoodItemsResource.foodItems.get(id);
                resultStr += "<FoodItemId>" + result.id + "</FoodItemId>\n";
                resultStr += "</FoodItemAdded>\n";
                builder = Response.status(409).entity(resultStr);
            }
        }
        else
        {
            builder = Response.status(400).entity("Invalid or incorrect input message");
        }

        return builder.build();
    }

    /**
     * Gets one or more food items
     *
     * @param
     * @return
     */
    @GET
    @Path("GetFoodItem")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoodItem(@QueryParam(value = "foodItemId") final List<Integer> foodItemId)
    {
        ResponseBuilder builder;
        boolean okResults = true;
        this.init();
        String xmlResult = "<RetrievedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>\n";

        for (int i = 0; i < foodItemId.size(); ++i)
        {
            FoodItem foodItem = FoodItemsResource.foodItems.get(foodItemId.get(i));

            if (null != foodItem)
            {
                xmlResult += foodItem.toString();
            }
            else
            {
                foodItem = new FoodItem();
                foodItem.id = foodItemId.get(i);
                xmlResult += foodItem.toString();
                okResults = false;
            }
        }

        xmlResult += "</RetrievedFoodItems >\n";

        if (okResults)
        {
            builder = Response.ok(xmlResult);
        }
        else
        {
            builder = Response.status(404).entity(xmlResult);
        }

        return builder.build();
    }

    /**
     * Gets one or more food items
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Path("GetFoodItemByPost")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoodItemByPost(String content)
    {
        ResponseBuilder builder;
        boolean okResults = true;
        this.init();
        String xmlResult = "<RetrievedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>\n";
        ArrayList<FoodItem> requestedFoodItems = this.parseGetRequest(content);

        for (int i = 0; i < requestedFoodItems.size(); ++i)
        {
            FoodItem foodItem = requestedFoodItems.get(i);

            if (null != foodItem)
            {
                xmlResult += foodItem.toString();
            }

            if (null == foodItem.name)
            {
                okResults = false;
            }
        }

        xmlResult += "</RetrievedFoodItems >\n";

        if (okResults)
        {
            builder = Response.ok(xmlResult);
        }
        else
        {
            builder = Response.status(404).entity(xmlResult);
        }

        return builder.build();
    }

    private HashMap<Integer, FoodItem> readFoodItemsXml()
    {
        HashMap<Integer, FoodItem> result = new HashMap();
        String line;
        Scanner sc = new Scanner(FoodItemsResource.xmlFile);

        while (sc.hasNextLine())
        {
            line = sc.nextLine().trim();

            if (line.matches("<FoodItem country=\"(.+)\">"))
            {
                boolean finished = false;
                FoodItem foodItem = new FoodItem();
                foodItem.country = this.parseCountry(line);

                while (sc.hasNextLine() && !finished)
                {
                    line = sc.nextLine().trim();

                    if (line.matches("<id>([0-9]+)</id>"))
                    {
                        foodItem.id = Integer.parseInt(this.parseValue(line));

                        if (foodItem.id > FoodItemsResource.nextId)
                        {
                            FoodItemsResource.nextId = foodItem.id + 1;
                        }
                    }
                    else if (line.matches("<name>(.+)</name>"))
                    {
                        foodItem.name = this.parseValue(line);
                    }
                    else if (line.matches("<description>(.+)</description>"))
                    {
                        foodItem.description = this.parseValue(line);
                    }
                    else if (line.matches("<category>(.+)</category>"))
                    {
                        foodItem.category = this.parseValue(line);
                    }
                    else if (line.matches("<price>([0-9]*[.][0-9][0-9])</price>"))
                    {
                        foodItem.price = Double.parseDouble(this.parseValue(line));
                    }
                    else if (line.matches("</FoodItem>"))
                    {
                        finished = true;
                        result.put(foodItem.id, foodItem);
                    }
                    else
                    {
                        LOG.info("Discarding: " + line);
                    }
                }
            }
        }

        return result;
    }

    private String parseValue(String str)
    {
        return str.substring(str.indexOf(">") + 1, str.lastIndexOf("<"));
    }

    private String parseCountry(String str)
    {
        String regex = "(?i)(.*)<FoodItem country=\"([a-zA-Z]+)\">(.*)";
        
        String result;
        
        try
        {
            if (str.matches(regex))
            {
                result = str.replaceAll(regex, "$2");
            }
            else
            {
                result = null;
            }
        }
        catch (Exception e)
        {
            result = null;
        }
        
        return result;
    }

    private FoodItem parseFoodItem(String str)
    {
        FoodItem result = new FoodItem();

        // Note: Do not set the id here until we do a reverse lookup and see if the food item already exists
        try
        {
            result.country = this.parseCountry(str);
            result.name = this.extractElementContent(str, "name");
            result.description = this.extractElementContent(str, "description");
            result.category = this.extractElementContent(str, "category");
            result.price = Double.parseDouble(this.extractElementContent(str, "price"));
        }
        catch (Exception e)
        {
            result = null;
        }
        
        if (!result.isValidFoodItem())
        {
            result = null;
        }

        return result;
    }

    private ArrayList<FoodItem> parseGetRequest(String str)
    {
        ArrayList<FoodItem> results = new ArrayList<FoodItem>();
        String request = this.extractElementContent(str, "SelectedFoodItems");
        int index = 0;
        String testStr;

        for (int i = 0; i < request.length(); ++i)
        {
            testStr = request.substring(index, i + 1);

            if (testStr.matches("<FoodItemId>([0-9]+)</FoodItemId>"))
            {
                index = i + 1;
                FoodItem foodItem = null;
                int id = Integer.parseInt(this.extractElementContent(testStr, "FoodItemId"));
                foodItem = FoodItemsResource.foodItems.get(new Integer(id));

                if (null == foodItem)
                {
                    foodItem = new FoodItem();
                    foodItem.id = id;
                }

                results.add(foodItem);
            }
        }

        return results;
    }

    private String extractElementContent(String str, String tagName)
    {
        String regex = "(?i)(.*)(<" + tagName + ")(>|\\s.+?>)(.+?)(</" + tagName + ")(>|\\s?>)(.*)";

        if (str.matches(regex))
        {
            return str.trim().replaceAll(regex, "$4");
        }
        else
        {
            return null;
        }
    }

    private HashMap<String, Integer> generateReverseLookup()
    {
        HashMap<String, Integer> result = new HashMap();

        if (null != FoodItemsResource.foodItems)
        {
            for (Map.Entry<Integer, FoodItem> entry : FoodItemsResource.foodItems.entrySet())
            {
                result.put(entry.getValue().name, entry.getKey());
            }
        }

        return result;
    }
}
