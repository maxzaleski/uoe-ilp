package uk.ac.ed.inf;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    public void testApp_IllegalArgument_Date()
    {
        for (String date : new String[]{"0000-00-00", "2023-12-1", "2023-9-1", "01-12-2023"})
        {
            try
            {
                App.main(new String[]{date, "", ""});
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                assertTrue(e.getMessage().contains("date"));
            }
        }
    }

    public void testApp_IllegalArgument_URL()
    {
        assertTrue(true);
        try
        {
            App.main(new String[]{"0000-00-00", "htt"});
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("date"));
        }
    }
}
