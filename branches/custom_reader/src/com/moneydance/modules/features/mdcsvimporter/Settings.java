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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miki and Stan Towianski
 */
public final class Settings
{
   private static File getFilename()
   {
      File moneydanceHome = new File( System.getProperty( "user.home" ), ".moneydance" );
      return new File( moneydanceHome, "mdcsvimporter.props" );
   }

   private static Properties load()
      throws IOException
   {
      Properties retVal = new Properties();
      InputStream is;
      try
      {
         is = new FileInputStream( getFilename() );
      }
      catch ( FileNotFoundException ex )
      {
         return retVal; // no file is normal condition to start with empty props object
      }
      try
      {
         retVal.load( is );
      }
      finally
      {
         is.close();
      }
      return retVal;
   }

   private static void save( Properties props )
      throws IOException
   {
      OutputStream os = new FileOutputStream( getFilename() );
      try
      {
         props.store( os, "MDCSVImporter - Moneydance CSV Importer" );
      }
      finally
      {
         os.close();
      }
   }

   public static String get( String name )
   {
      try
      {
         return load().getProperty( name );
      }
      catch ( IOException ex )
      {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
         return null;
      }
   }
           
   public static HashMap<String, CustomReaderData> createReaderConfigsHM()
   {
      HashMap<String, CustomReaderData> ReaderConfigsHM = new HashMap<String, CustomReaderData>();
      
      try
      {
        Properties props = load();

        for ( Enumeration enu = props.propertyNames(); enu.hasMoreElements(); )
            {
            String key = (String) enu.nextElement();
            System.out.println( "props key =" + key + "=" );
            if ( key.startsWith( "reader:" ) && key.endsWith( ".Name" ) )
                {
                String readerName = key.replaceAll( "reader\\:(.*)\\..*", "reader:$1" );
                System.err.println(  "readerName >" + readerName + "<" );
                   
                CustomReaderData customReaderData = new CustomReaderData();
                customReaderData.setReaderName( props.getProperty( readerName + ".Name" ) );
                customReaderData.setFieldSeparatorChar( Integer.parseInt( props.getProperty( readerName + ".FieldSeparator" ) ) );
                customReaderData.setDateFormatString( props.getProperty( readerName + ".DateFormatString" ) );

                customReaderData.setHeaderLines( Integer.parseInt( props.getProperty( readerName + ".HeaderLines" ) ) );

                customReaderData.setDataTypesList( new ArrayList<String>(Arrays.asList( props.getProperty( readerName + ".DataTypesList" ).split( "[\\[\\],]" ) ) ) );
                customReaderData.setEmptyFlagsList( new ArrayList<String>(Arrays.asList( props.getProperty( readerName + ".EmptyFlagsList" ).split( "[\\[\\],]" ) ) ) );

                int max = customReaderData.getDataTypesList().size();
                for ( int c = 1; c < max; c++ )
                    {
                    customReaderData.getDataTypesList().set( c - 1,customReaderData.getDataTypesList().get( c ).trim() );
                    customReaderData.getEmptyFlagsList().set( c - 1,customReaderData.getEmptyFlagsList().get( c ).trim() );
                    }

                System.err.println( "props readerName =" + customReaderData.getReaderName() + "=" );
                System.err.println( "props getFieldSeparatorChar() =" + customReaderData.getFieldSeparatorChar() + "=" );
                System.err.println( "props getDateFormatString() =" + customReaderData.getDateFormatString()+ "=" );
                System.err.println( "props getHeaderLines() =" + customReaderData.getHeaderLines() + "=" );
                System.err.println( "props getDataTypesList() =" + customReaderData.getDataTypesList() + "=" );
                System.err.println( "props getEmptyFlagsList() =" + customReaderData.getEmptyFlagsList() + "=" );
                
                ReaderConfigsHM.put( props.getProperty( readerName + ".Name" ), customReaderData );
                }
            }
          }
      catch ( IOException ex )
         {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
         return null;
         }
      
      return ReaderConfigsHM;
   }

   public static String get( String name, String defaultValue )
   {
      try
      {
         String retVal = load().getProperty( name );
         if ( retVal == null )
         {
            return defaultValue;
         }
         return retVal;
      }
      catch ( IOException ex )
      {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
         return defaultValue;
      }
   }

   public static void set( String name, String value )
   {
      try
      {
         Properties props = load();

         // skip if values match (I am sorry for not optimizing the condition, it is early morning...)
         String oldValue = props.getProperty( name );
         if ( (oldValue != null && oldValue.equals( value )) ||
            (value != null && value.equals( oldValue )) )
         {
            return;
         }

         props.setProperty( name, value );
         save( props );
      }
      catch ( IOException ex )
      {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
      }
   }

   public static void setOnly( Properties props, String name, String value )
   {
         // skip if values match (I am sorry for not optimizing the condition, it is early morning...)
         String oldValue = props.getProperty( name );
         if ( (oldValue != null && oldValue.equals( value )) ||
            (value != null && value.equals( oldValue )) )
         {
            return;
         }

         props.setProperty( name, value );
   }

   public static boolean getBoolean( String name )
   {
      return getBoolean( name, false );
   }

   public static boolean getBoolean( String name, boolean defaultValue )
   {
      String value = get( name );
      if ( value == null )
      {
         return defaultValue;
      }

      if ( value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "yes" ) ||
         value.equalsIgnoreCase( "1" ) )
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public static void setBoolean( String name, boolean value )
   {
      set( name, value ? "true" : "false" );
   }

   public static void setYesNo( String name, boolean value )
   {
      set( name, value ? "yes" : "no" );
   }

   public static int getInteger( String name )
   {
      return getInteger( name, 0 );
   }

   public static int getInteger( String name, int defaultValue )
   {
      String value = get( name );
      if ( value == null )
      {
         return defaultValue;
      }

      return Integer.parseInt( value );
   }

   public static void setInteger( String name, int value )
   {
      set( name, Integer.toString( value ) );
   }

   public static void setCustomReaderConfig( CustomReaderData customReaderData )
   {
      try
      {
         Properties props = load();

         setOnly( props, "reader:" + customReaderData.getReaderName() + ".Name", customReaderData.getReaderName() );
         setOnly( props, "reader:" + customReaderData.getReaderName() + ".HeaderLines", Integer.toString( customReaderData.getHeaderLines() ) );
         setOnly( props, "reader:" + customReaderData.getReaderName() + ".FieldSeparator", Integer.toString( customReaderData.getFieldSeparatorChar() ) );
         setOnly( props, "reader:" + customReaderData.getReaderName() + ".DateFormatString", customReaderData.getDateFormatString() );
         setOnly( props, "reader:" + customReaderData.getReaderName() + ".DataTypesList", customReaderData.getDataTypesList().toString() );
         setOnly( props, "reader:" + customReaderData.getReaderName() + ".EmptyFlagsList", customReaderData.getEmptyFlagsList().toString() );

         save( props );
      }
      catch ( IOException ex )
      {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
      }
   }

   public static void removeCustomReaderConfig( CustomReaderData customReaderData )
   {
      try
      {
         Properties props = load();
         
         props.remove( "reader:" + customReaderData.getReaderName() + ".Name" );
         props.remove( "reader:" + customReaderData.getReaderName() + ".HeaderLines" );
         props.remove( "reader:" + customReaderData.getReaderName() + ".FieldSeparator" );
         props.remove( "reader:" + customReaderData.getReaderName() + ".DateFormatString" );
         props.remove( "reader:" + customReaderData.getReaderName() + ".DataTypesList" );
         props.remove( "reader:" + customReaderData.getReaderName() + ".EmptyFlagsList" );

         save( props );
      }
      catch ( IOException ex )
      {
         Logger.getLogger( Settings.class.getName() ).log( Level.SEVERE, null, ex );
      }
   }
}
