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
 * This class provides the POX over HTTP Web Services AddFoodItem and
 * GetFoodItem. There are two versions of the GetFoodItem service, one using URI
 * tunneling, via a simple GET request with query parameters, while the other 
 * uses a standard POX over HTTP model with an XML message passed via a POST (as 
 * discussed in the course book "REST in Practice" section 3.5).
 * 
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
 * POX over HTTP Web Service
 *
 * @author Brandon Andersen
 */
@Path("FoodItems")
public class FoodItemsResource
{
    @Context
    private ServletContext context;
    private static int nextId; // Used for issuing new id when adding a new food item
    private static InputStream xmlFile = null; // Input stream for reading in the initial food items from the provided XML document
    private static HashMap<Integer, FoodItem> foodItems = null; // Map for looking up food items by their id
    private static HashMap<String, Integer> reverseLookup = null; // Map for looking up a food item's id by name
    private static final Logger LOG = Logger.getLogger(FoodItemsResource.class.getName()); // Logging utility

    /**
     * Creates a new instance of FoodItemsResource
     */
    public FoodItemsResource()
    {
        BasicConfigurator.configure(); // Initialize the logger with a standard configuration
        FoodItemsResource.nextId = -1; // Set the next id to issue to -1 to start
    }

    /**
     * init - Initializes, if required, the food item maps and the XML document
     * input stream
     */
    private boolean init()
    {
        boolean result;

        // Get the input stream for the XML document
        if (null == FoodItemsResource.xmlFile)
        {
            LOG.info("Initializing XML File input stream");
            FoodItemsResource.xmlFile = this.context.getResourceAsStream("/xml/FoodItemData.xml");
        }

        // Initialize the maps
        if (null == FoodItemsResource.foodItems)
        {
            LOG.info("Initializing foodItems");
            FoodItemsResource.foodItems = this.readFoodItemsXml();

            if (null == FoodItemsResource.reverseLookup)
            {
                FoodItemsResource.reverseLookup = this.generateReverseLookup();
            }
        }

        result = (null != FoodItemsResource.xmlFile) && (null != FoodItemsResource.foodItems) && (null != FoodItemsResource.reverseLookup);

        return result;
    }

    /**
     * Adds a food item
     *
     * @param content - This should be an XML message (matching the predefined
     * format for the assignment) containing a food item to add.
     *
     * @return a response that contains the results of the add operation
     *
     * 200 - Implies the addition succeeded and the returned id is the id of the
     * newly added food item
     *
     * 400 - Implies the XML message provided did not meet the predefined format
     * and the add was rejected
     *
     * 409 - Implies a food item with the specified name already exists and the
     * returned id is the id of the preexisting food item
     */
    @POST
    @Path("AddFoodItem")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addFoodItem(String content)
    {
        ResponseBuilder builder; // a response builder for building the response

        if (this.init()) // Make sure everything is ready...
        {
            FoodItem result = this.parseFoodItem(content); // attempt to parse the XML message and see it it corresponds to a valid food item

            // if the message was okay...
            if (null != result)
            {
                // start preping the response message
                String resultStr = "<FoodItemAdded xmlns=”http://cse460.asu.edu/PoxAssignment”>\n";
                // do a reverse lookup on the new food item's name to see if it does not already exist
                Integer id = FoodItemsResource.reverseLookup.get(result.name);

                // if the id is null, the name does not already exist, so proceed with add
                if (null == id)
                {
                    // lock the static variable nextId to make sure no other threads play with it while getting the next id...
                    synchronized (this)
                    {
                        result.id = ++FoodItemsResource.nextId; // assign the next id to the new food item
                    }

                    FoodItemsResource.foodItems.put(result.id, result); // stick it in the map
                    FoodItemsResource.reverseLookup.put(result.name, result.id); // also in the reverse lookup map
                    resultStr += "<FoodItemId>" + result.id + "</FoodItemId>\n"; // ad id to the response message
                    resultStr += "</FoodItemAdded>\n"; // close the response message
                    builder = Response.ok(resultStr); // build the response with 200 OK!!!
                }
                else
                {
                    // Already exists...
                    result = FoodItemsResource.foodItems.get(id); // get the preexisting food item
                    resultStr += "<FoodItemId>" + result.id + "</FoodItemId>\n"; // ad id to the response message
                    resultStr += "</FoodItemAdded>\n"; // close the response message
                    builder = Response.status(409).entity(resultStr); // build the response with 409 CONFLICT!!!
                }
            }
            else
            {
                builder = Response.status(400).entity("Invalid or incorrect input message"); // bad XML message format somewhere...
            }
        }
        else
        {
            builder = Response.status(500);
        }

        return builder.build(); // return the response via the builder
    }

    /**
     * Gets one or more food items using GET with URI tunneling
     *
     * @param foodItemId - This is one or more food item id's provided as query
     * parameters in the URI which get mapped to a List of Integers for easy
     * handling...
     *
     * @return a response that contains the results of the get operation
     *
     * 200 - Implies the get succeeded and the found food items are in the
     * returned XML message
     *
     * 404 - Implies the one or more food items requested were not found, though
     * ones that were found will be included in the return message
     */
    @GET
    @Path("GetFoodItem")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoodItem(@QueryParam(value = "foodItemId") final List<Integer> foodItemId)
    {
        ResponseBuilder builder; // ... for building responses

        if (this.init()) // Make sure everything is ready...
        {
            boolean okResults = true; // keep track of whether or not all requested items were found

            String xmlResult = "<RetrievedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>\n"; // start building response message

            // iterate over the list of requested id's
            for (int i = 0; i < foodItemId.size(); ++i)
            {
                // try to pull the current id out of the map...
                FoodItem foodItem = FoodItemsResource.foodItems.get(foodItemId.get(i));

                // if it was found, add it to the response message
                if (null != foodItem)
                {
                    xmlResult += foodItem.toString();
                }
                else
                {
                    // if not found, create a dummy food item and add it to the response
                    foodItem = new FoodItem();
                    foodItem.id = foodItemId.get(i);
                    xmlResult += foodItem.toString();
                    okResults = false; // indicate that at least one requested item was not found
                }
            }

            xmlResult += "</RetrievedFoodItems >\n"; // close out the response message

            // if all items requested were found...
            if (okResults)
            {
                builder = Response.ok(xmlResult); // 200 OK!!!
            }
            else
            {
                builder = Response.status(404).entity(xmlResult); // ... otherwise 404!!!
            }
        }
        else
        {
            builder = Response.status(500);
        }

        return builder.build(); // send the response back
    }

    /**
     * Gets one or more food items using POST with POX over HTTP
     *
     * @param foodItemId - This is one or more food item id's provided as an XML
     * message
     *
     * @return a response that contains the results of the get operation
     *
     * 200 - Implies the get succeeded and the found food items are in the
     * returned XML message
     *
     * 404 - Implies the one or more food items requested were not found, though
     * ones that were found will be included in the return message
     */
    @POST
    @Path("GetFoodItemByPost")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoodItemByPost(String content)
    {
        ResponseBuilder builder; // builder for responses

        if (this.init()) // Make sure everything is ready...
        {
            boolean okResults = true; // track if all requested items were found

            String xmlResult = "<RetrievedFoodItems xmlns=”http://cse460.asu.edu/PoxAssignment”>\n"; // start the response message
            ArrayList<FoodItem> requestedFoodItems = this.parseGetRequest(content); // parse the XML request message

            // For all the food items returned...
            for (int i = 0; i < requestedFoodItems.size(); ++i)
            {
                // get the item
                FoodItem foodItem = requestedFoodItems.get(i);

                // make sure it is not null
                if (null != foodItem)
                {
                    xmlResult += foodItem.toString(); // add to resposne message
                }

                // if one of the returned food items has a null name, that indicates it was not really in the map
                if (null == foodItem.name)
                {
                    okResults = false; // so at least one was not found
                }
            }

            xmlResult += "</RetrievedFoodItems >\n"; // close out the message

            // OK...
            if (okResults)
            {
                builder = Response.ok(xmlResult); // 200 OK!!!
            }
            else
            {
                builder = Response.status(404).entity(xmlResult); // ... otherwise 404!!!
            }
        }
        else
        {
            builder = Response.status(500);
        }

        return builder.build(); // return the response
    }

    /**
     * readFoodItemsXml - Reads food items from XML file...
     *
     * @return HashMap<Integer, FoodItem> constructed from the XML file
     */
    private HashMap<Integer, FoodItem> readFoodItemsXml()
    {
        HashMap<Integer, FoodItem> result = new HashMap(); // new map to populate
        String line; // for the scanner...

        // try / catch for exceptions while parsing input file...
        try
        {
            Scanner sc = new Scanner(FoodItemsResource.xmlFile); // open a scanner with the input stream

            // iterate through the XML file
            while (sc.hasNextLine())
            {
                line = sc.nextLine().trim(); // get the next line and trim it up...

                // see if it matches the start of a food item definition
                if (line.matches("<FoodItem country=\"(.+)\">"))
                {
                    // if so, go to work...
                    boolean finished = false;
                    FoodItem foodItem = new FoodItem();
                    foodItem.country = this.parseCountry(line); // grab the country

                    // keep looping to find the end of the current food item
                    while (sc.hasNextLine() && !finished)
                    {
                        line = sc.nextLine().trim();

                        // check for id
                        if (line.matches("<id>([0-9]+)</id>"))
                        {
                            foodItem.id = Integer.parseInt(this.parseValue(line));

                            if (foodItem.id > FoodItemsResource.nextId)
                            {
                                FoodItemsResource.nextId = foodItem.id + 1;
                            }
                        }
                        // get the name
                        else if (line.matches("<name>(.+)</name>"))
                        {
                            foodItem.name = this.parseValue(line);
                        }
                        // get the description
                        else if (line.matches("<description>(.+)</description>"))
                        {
                            foodItem.description = this.parseValue(line);
                        }
                        // get the category
                        else if (line.matches("<category>(.+)</category>"))
                        {
                            foodItem.category = this.parseValue(line);
                        }
                        // get the price
                        else if (line.matches("<price>([0-9]*[.][0-9][0-9])</price>"))
                        {
                            foodItem.price = Double.parseDouble(this.parseValue(line));
                        }
                        // reached the end
                        else if (line.matches("</FoodItem>"))
                        {
                            finished = true;
                            result.put(foodItem.id, foodItem); // stick it in the map
                        }
                        else
                        {
                            LOG.info("Discarding: " + line); // found a line in the food item definition to discard, log it
                        }
                    }
                }
            }
        }
        // any exceptions that occur, while they are a problem, it is most likely due to a bad XML file, but we provide that so...
        catch (Exception e)
        {
            LOG.info("Encountered error parsing XML file: " + e.getMessage());
            result = null; // set result to null if a parsing error occurred
        }

        return result; // return the hopefully non-null map
    }

    /**
     * parseValue - primitive first attempt at extracting values from between
     * two xml tags (only use it for parsing initial file)
     *
     * @param str
     * @return - the extracted value from the tag element
     */
    private String parseValue(String str)
    {
        return str.substring(str.indexOf(">") + 1, str.lastIndexOf("<"));
    }

    /**
     * parseCountry - grabs the country identifier from the <FoodItem> opening
     * tag
     *
     * @param str - the tag to extract the country from
     * @return - the extracted country code
     */
    private String parseCountry(String str)
    {
        // rather specific to the implementation... thus not very forgiving
        String regex = "(?i)(.*)<FoodItem country=\"([a-zA-Z]+)\">(.*)";

        String result;

        try
        {
            // make sure it is even worth trying to extract
            if (str.matches(regex))
            {
                // if so, get the country code
                result = str.replaceAll(regex, "$2");
            }
            else
            {
                // otherwise, set result to null
                result = null;
            }
        }
        catch (Exception e)
        {
            // another error occurred, set result to null
            result = null;
        }

        // return the result
        return result;
    }

    /**
     * parseFoodItem - extract the food item from an XML add message
     *
     * @param str - XML message to parse
     * @return FoodItem - the extracted food item or null if the message was
     * bad...
     */
    private FoodItem parseFoodItem(String str)
    {
        FoodItem result = new FoodItem(); // start with a default food item

        // Note: Do not set the id here until we do a reverse lookup and see if the food item already exists
        try
        {
            // get all the passed parameters
            result.country = this.parseCountry(str);
            result.name = this.extractElementContent(str, "name");
            result.description = this.extractElementContent(str, "description");
            result.category = this.extractElementContent(str, "category");
            result.price = Double.parseDouble(this.extractElementContent(str, "price"));
        }
        catch (Exception e)
        {
            result = null; // any problems occur, null it
        }

        // make sure all required parameters were populated, the food item knows this, so ask it...
        if (!result.isValidFoodItem())
        {
            result = null;
        }

        return result; // return the result
    }

    /**
     * parseGetRequest - Used for parsing the POST POX over HTTP version of the
     * get request
     *
     * @param str - the XML request message
     * @return - an array list of food items, possibly containing some bad
     * (default) food items
     */
    private ArrayList<FoodItem> parseGetRequest(String str)
    {
        ArrayList<FoodItem> results = new ArrayList<FoodItem>(); // return array list
        String request = this.extractElementContent(str, "SelectedFoodItems"); // extract the request
        int index = 0; // used for crawling the test string in segments
        String testStr; // test string for crawling

        // start crawling along the test string
        for (int i = 0; i < request.length(); ++i)
        {
            // get the substring
            testStr = request.substring(index, i + 1);

            // see if we have crawled far enough to get a match...
            if (testStr.matches("<FoodItemId>([0-9]+)</FoodItemId>"))
            {
                index = i + 1; // if so, update the new start index
                FoodItem foodItem; // food item
                int id = Integer.parseInt(this.extractElementContent(testStr, "FoodItemId")); // get the requested id
                foodItem = FoodItemsResource.foodItems.get(new Integer(id)); // try and pull it from the map

                // if the food item is null, that is, not found...
                if (null == foodItem)
                {
                    foodItem = new FoodItem(); // create a default (dummy) food item
                    foodItem.id = id; // set the id for returning invalid food item
                }

                results.add(foodItem); // add it to the results
            }
        }

        return results; // return the results
    }

    /**
     * extractElementContent - pulls the content from an XML element with the
     * specified name passed
     *
     * @param str - the string to extract content from
     * @param tagName - the tag name of the element of interest
     * @return String - the extracted content
     */
    private String extractElementContent(String str, String tagName)
    {
        // the regex...
        String regex = "(?i)(.*)(<" + tagName + ")(>|\\s.+?>)(.+?)(</" + tagName + ")(>|\\s?>)(.*)";

        // check for a match
        if (str.matches(regex))
        {
            // if so, get the 4th element from the regex...
            return str.trim().replaceAll(regex, "$4");
        }
        else
        {
            // no match, return null...
            return null;
        }
    }

    /**
     * generateReverseLookup - creates the reverse lookup map from the food
     * items map
     *
     * @return HashMap<String, Integer> - the reverse lookup map
     */
    private HashMap<String, Integer> generateReverseLookup()
    {
        HashMap<String, Integer> result = new HashMap();

        // make sure the food item map is there
        if (null != FoodItemsResource.foodItems)
        {
            // for each entry in the food item map...
            for (Map.Entry<Integer, FoodItem> entry : FoodItemsResource.foodItems.entrySet())
            {
                // add a reverse lookup by name...
                result.put(entry.getValue().name, entry.getKey());
            }
        }
        else
        {
            result = null; // if the food item map is null, then this is too...
        }

        return result; // return the map
    }
}
