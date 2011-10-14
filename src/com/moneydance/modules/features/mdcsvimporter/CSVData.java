/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moneydance.modules.features.mdcsvimporter;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author miki and Stan Towianski
 */
public class CSVData
{
   private String[][] data;
   private int currentLineIndex = -1;
   private int currentFieldIndex = -1;

   public CSVReader reader;
   
   public CSVData( CSVReader readerArg )
   {
       this.reader = readerArg;
   }

   public void reset()
   {
      currentLineIndex = -1;
      currentFieldIndex = -1;
   }

   public void parseIntoLines( int fieldSeparator )
      throws IOException
   {
      ArrayList<String> line = new ArrayList<String>();
      ArrayList<String[]> file = new ArrayList<String[]>();

      if ( fieldSeparator > 0 )
        {
        reader.setFieldSeparator( fieldSeparator );
        }
      
      while ( reader.nextLine() )
      {
         for ( String s = reader.nextField(); s != null; s = reader.nextField() )
            {
            //System.err.println( "         line.add string =" + s + "=" );
            line.add( s );
            }

         //System.err.println( "         line.size() =" + line.size() + "=" );
         String[] newLine = new String[ line.size() ];
         line.toArray( newLine );
         file.add( newLine );
         line.clear();
      }

      data = new String[file.size()][];
      file.toArray( data );
      //System.err.println( "         lines total =" + file.size() + "=" );
      
      currentLineIndex = -1;
      currentFieldIndex = -1;      
   }

   public boolean nextLine()
   {
      if ( currentLineIndex < data.length )
      {
         ++currentLineIndex;
         currentFieldIndex = -1;
      }

      //System.err.println(  "nextLine() ----  currentLineIndex =" + currentLineIndex + "=    data.length =" + data.length + "   ans =" + (currentLineIndex < data.length ? "true" : "false" ) );
      return currentLineIndex < data.length;
   }

   public boolean hasEnoughFieldsPerCurrentLine( int neededFields )
   {
      //System.err.println(  "fieldsPerCurrentLine()   data[currentLineIndex].length + 1 =" + (data[currentLineIndex].length + 1)  );
      return data[currentLineIndex].length + 1 >= neededFields;
   }

   public boolean nextField()
   {
      //System.err.println(  "nextField() ----  currentLineIndex =" + currentLineIndex + "=    data.length =" + data.length );
      if ( currentLineIndex < 0 || currentLineIndex >= data.length )
      {
      //System.err.println(  "nextField() ----  return false" );
         return false;
      }

      if ( currentFieldIndex < data[currentLineIndex].length )
      {
         ++currentFieldIndex;
      }

      //System.err.println(  "nextField()2 ----  currentLineIndex =" + currentLineIndex + "=    data.length =" + data.length + "   ans =" + (currentFieldIndex < data[currentLineIndex].length ? "true" : "false" ) );
      return currentFieldIndex < data[currentLineIndex].length;
   }

   public boolean hasZeroFields()
   {
      //System.err.println(  "hasZeroFields() ----  currentLineIndex =" + currentLineIndex + "=    data.length =" + data.length );
      if ( currentLineIndex < 0 || currentLineIndex >= data.length )
      {
      //System.err.println(  "hasZeroFields() ----  return false" );
         return false;
      }

      //System.err.println(  "hasZeroFields()2 ----  currentLineIndex =" + currentLineIndex + "=    data.length =" + data.length + "   ans =" + (0 < data[currentLineIndex].length ? "true" : "false" ) );
      return 0 < data[currentLineIndex].length;
   }

   public String getField()
   {
      if ( currentLineIndex < 0 || currentLineIndex >= data.length )
      {
         return null;
      }
      if ( currentFieldIndex < 0 || currentFieldIndex >= data[currentLineIndex].length )
      {
         return null;
      }

      return data[currentLineIndex][currentFieldIndex];
   }

    public CSVReader getReader() {
        return this.reader;
    }

    public void setReader(CSVReader reader) {
        this.reader = reader;
    }
   
   
}
