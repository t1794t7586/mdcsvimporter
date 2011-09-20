/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moneydance.modules.features.mdcsvimporter;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author miki
 */
public class CSVDataTest
{
   public CSVDataTest()
   {
   }

   @BeforeClass
   public static void setUpClass()
      throws Exception
   {
   }

   @AfterClass
   public static void tearDownClass()
      throws Exception
   {
   }

   @Before
   public void setUp()
   {
   }

   @After
   public void tearDown()
   {
   }
   private final String test1 = "\"Column 1\",\"Column 2\"\n\"value 11\",\"value 12\"\n" +
      "\"value 21\",\"value 22\"";

   @Test
   public void simpleTest1()
      throws IOException
   {
      StringReader data = new StringReader( test1 );
      CSVReader reader = new CSVReader( data );
      CSVData csvdata = new CSVData( reader );
      doTest1( csvdata );
   }

   private void doTest1( CSVData csvData )
      throws IOException
   {
//        if ( this instanceof CustomReader )
//            {
//            csvData.parseIntoLines( customReaderData.getFieldSeparatorChar() );
//            }
//        else
//            {
            csvData.parseIntoLines( 0 );
//            }

      assertFalse( csvData.nextField() );
      assertTrue( csvData.nextLine() );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "Column 1" );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "Column 2" );
      assertFalse( csvData.nextField() );
      assertTrue( csvData.nextLine() );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "value 11" );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "value 12" );
      assertFalse( csvData.nextField() );
      assertTrue( csvData.nextLine() );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "value 21" );
      assertTrue( csvData.nextField() );
      assertEquals( csvData.getField(), "value 22" );
      assertFalse( csvData.nextField() );
      assertFalse( csvData.nextLine() );
      assertFalse( csvData.nextField() );
   }
}