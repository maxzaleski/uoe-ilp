package uk.ac.ed.inf;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    public void testApp_validateArgs_IllegalArgument_Date()
    {
        for (String date : new String[]{null, "", "0000-00-00", "2023-12-1", "2023-9-1", "01-12-2023"})
        {
            try
            {
                App.validateArgs(date, "");
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (Exception e)
            {
                assert e.getMessage().contains("date");
            }
        }
    }

    public void testApp_validateArgs_Date()
    {
        try
        {
            App.validateArgs("2023-12-01", "https://test.test");
        } catch (IllegalArgumentException e)
        {
            fail("expected 'IllegalArgumentException' to be thrown");
        }
    }

    public void testApp_validateArgs_IllegalArgument_Url()
    {
        for (String url : new String[]{null, "", "www.example.com/file[/].html", "127.0.0.1"})
        {
            try
            {
                App.validateArgs("2023-12-01", url);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (Exception e)
            {
                assert e.getMessage().contains("url");
            }
        }
    }

    public void testApp_validateArgs_Url()
    {
        for (String url : new String[]{
                "http://ilp-rest.azurewebsites.net",
                "https://ilp-rest.azurewebsites.net",
                "http://127.0.0.0",
                "https://127.0.0.0",
        })
        {
            try
            {
                App.validateArgs("2023-12-01", url);
            } catch (Exception e)
            {
                fail("didn't expect 'IllegalArgumentException' to be thrown");
            }
        }
    }
}
