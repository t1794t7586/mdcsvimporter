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
package com.moneydance.modules.features.mdcsvimporter.formats;

import com.moneydance.apps.md.model.OnlineTxn;
import com.moneydance.modules.features.mdcsvimporter.CSVData;
import com.moneydance.modules.features.mdcsvimporter.CustomReaderData;
import com.moneydance.modules.features.mdcsvimporter.TransactionReader;
import com.moneydance.util.CustomDateFormat;
import com.moneydance.util.StringUtils;
import java.io.IOException;

/**
 *
 * @author Stan Towianski     August 2011
 */
public class CustomReader extends TransactionReader
{
   private static final String DATE_FORMAT_US = "MM/DD/YYYY";
   private static final String DATE_FORMAT_EU = "DD/MM/YY";
   private static final String DATE_FORMAT_JP = "YY/MM/DD";
   private static final String DATE_FORMAT_INTN = "YYYY-MM-DD";

   private String dateFormatStringSelected = DATE_FORMAT_US;
   
   private static final String[] SUPPORTED_DATE_FORMATS =
   {
      DATE_FORMAT_US
           , DATE_FORMAT_EU
           , DATE_FORMAT_JP
           , DATE_FORMAT_INTN
   };
   private CustomDateFormat dateFormat = new CustomDateFormat( DATE_FORMAT_US );
   
   private CustomReaderData customReaderData = null;
   
   public CustomReader( CustomReaderData customReaderData )
        {
        this.customReaderData = customReaderData;
        setCustomReaderFlag( true );
        }
   
   @Override
   public boolean canParse( CSVData data )
        {
        data.reset();

        int skipHeaderLines = customReaderData.getHeaderLines();
        for ( int i = 0; i < skipHeaderLines; i++ )
            {
            data.nextLine();
            }
      
      boolean retVal = true;
      
      while ( retVal && data.nextLine() )
         {
         System.err.println(  "----------------------" );
         if ( ! data.hasZeroFields() )
            {
            continue; // skip empty lines
            }

         int fieldIndex = 0;
         int maxFieldIndex = customReaderData.getNumberOfCustomReaderFieldsUsed();
         System.err.println(  "maxFieldIndex =" + maxFieldIndex );
         
         for (           ; retVal && fieldIndex < maxFieldIndex; fieldIndex ++ )
             {
             String dataTypeExpecting = customReaderData.getDataTypesList().get( fieldIndex );
             System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  fieldIndex = " + fieldIndex );

             data.nextField();
//             if ( ! data.nextField() )
//                {
//                System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but have no data left." );
//                retVal = false;
//                break;
//                }
             String fieldString = data.getField();
             
             if ( dataTypeExpecting.equalsIgnoreCase( "ignore" ) )
                {
                continue;
                }
             else if ( ( fieldString == null || fieldString.equals( "" ) ) )
                {
                if ( ! customReaderData.getEmptyFlagsList().get( fieldIndex ).equals( "Can Be Blank" ) )
                    {
                    System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but got no value =" + fieldString + "= and STOP ON ERROR" );
                    retVal = false;
                    break;
                    }
                else
                    {
                    System.err.println(  "ok to skip this blank field" );
                    continue;
                    }
                }
                
             if ( dataTypeExpecting.equalsIgnoreCase( "date" ) )
                {
                System.err.println(  "date >" + fieldString + "<" );
                System.err.println(  "date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );
              /*
                 if ( !date.equals( dateFormat.format( dateFormat.parseInt( data.getField() ) ) ) )
                 {
                    retVal = false;
                    break;
                 }
              */
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( "-Payment-" ) 
                         || dataTypeExpecting.equalsIgnoreCase( "-Deposit-" ) )   // was only amount before
                {
                System.err.println(  "amountString >" + fieldString + "<" );
                fieldString = fieldString.replaceAll( "\\((.*)\\)", "-$1" );

                try
                     {
                        //StringUtils.parseDoubleWithException( fieldString, '.' );
                        String tmp = fieldString.replace( '$', '0' );
                        System.err.println(  "check modified amountString 1 >" + tmp + "<" );
                        tmp = tmp.replace( '-', '0' );
                        System.err.println(  "check modified amountString 2 >" + tmp + "<" );
                        tmp = tmp.replaceAll( " ", "" );
                        System.err.println(  "check modified amountString 3 >" + tmp + "<" );
                        tmp = tmp.replaceAll( ",", "" );
                        System.err.println(  "check modified amountString 4 >" + tmp + "<" );
                        tmp = tmp.replaceAll( "\\.", "" );
                        System.err.println(  "check modified amountString 5 >" + tmp + "<" );
                        tmp = tmp.replaceAll( "\\d", "" );
                        System.err.println(  "check modified amountString 6 >" + tmp + "<" );
                        //Number number = NumberFormat.getNumberInstance().parse( tmp );
                        if ( tmp.equals( "" ) ) //number instanceof Double || number instanceof Long )
                            {
                            System.err.println(  "ok number" );
                            ;
                            }
                        else
                            {
                            retVal = false;
                            break;
                            }
                     }
                     catch ( Exception x )
                     {
                        retVal = false;
                        break;
                     }
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( "description" ) )
                {
                System.err.println(  "description >" + fieldString + "<" );
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( "memo" ) )
                {
                System.err.println(  "memo >" + fieldString + "<" );
                }
             } // end for
      }

      return retVal;
   }

   @Override
   public String getFormatName()
   {
      return customReaderData.getReaderName();
   }

   /*
    * Note: This really parses a whole line at a time.
    */
   @Override
   protected boolean parseNext( OnlineTxn txn )
      throws IOException
   {
     long amount = 0;
     int date = 0;
     String description = "";
     int fieldIndex = 0;
     int maxFieldIndex = customReaderData.getNumberOfCustomReaderFieldsUsed();
     System.err.println(  "maxFieldIndex =" + maxFieldIndex );

     System.err.println(  "----------------------" );
     if ( ! reader.hasZeroFields() )
        {
        System.err.println(  "skip empty line" );
        return false; // skip empty lines
        }

     for (           ; fieldIndex < maxFieldIndex; fieldIndex ++ )
         {
         String dataTypeExpecting = customReaderData.getDataTypesList().get( fieldIndex );
         System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  EmptyFlagsList = " + customReaderData.getEmptyFlagsList().get( fieldIndex ) + "=" );

         reader.nextField();
         String fieldString = reader.getField();
         System.err.println(  "fieldString =" + fieldString + "=  fieldIndex = " + fieldIndex );

         if ( dataTypeExpecting.equalsIgnoreCase( "ignore" ) )
            {
            continue;
            }
         else if ( ( fieldString == null || fieldString.equals( "" ) )
                    && ! customReaderData.getEmptyFlagsList().get( fieldIndex ).equals( "Can Be Blank" ) )
            {
            System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but got no value =" + fieldString + "= and STOP ON ERROR" );
            throwException( "dataTypeExpecting =" + dataTypeExpecting + "=  but got no value =" + fieldString + "= and STOP ON ERROR" );
            }
         
         if ( dataTypeExpecting.equalsIgnoreCase( "date" ) )
            {
            System.err.println(  "date >" + fieldString + "<" );
            System.err.println(  "date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            date = dateFormat.parseInt( fieldString );

            txn.setDatePostedInt( date );
            txn.setDateInitiatedInt( date );
            txn.setDateAvailableInt( date );
          /*
             if ( !date.equals( dateFormat.format( dateFormat.parseInt( reader.getField() ) ) ) )
             {
                retVal = false;
                break;
             }
          */
            }
        else if ( ( dataTypeExpecting.equalsIgnoreCase( "-Payment-" ) 
                      || dataTypeExpecting.equalsIgnoreCase( "-Deposit-" ) )
                                        &&
                     ! ( fieldString == null || fieldString.equals( "" ) ) )
            {
            System.err.println(  "amountString >" + fieldString + "<" );
            fieldString = fieldString.replaceAll( "\\((.*)\\)", "-$1" );
            
            try
                {
                double amountDouble = StringUtils.parseDoubleWithException( fieldString, '.' );
                if ( dataTypeExpecting.equalsIgnoreCase( "-Payment-" ) )
                    {
                    amount += currency.getLongValue( amountDouble );
                    }
                else if ( dataTypeExpecting.equalsIgnoreCase( "-Deposit-" ) )
                    {
                    System.err.println(  "flip sign for deposit" );
                    amount -= currency.getLongValue( amountDouble );
                    }
                }
            catch ( Exception x )
                {
                throwException( "Invalid amount." );
                }
            txn.setAmount( amount );
            txn.setTotalAmount( amount );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( "check number" ) )
            {
            System.err.println(  "check number >" + fieldString + "<" );
            txn.setCheckNum( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( "description" ) )
            {
            System.err.println(  "description >" + fieldString + "<" );
            txn.setName( fieldString );
            description = fieldString;
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( "memo" ) )
            {
            System.err.println(  "memo >" + fieldString + "<" );
            txn.setMemo( fieldString );
            }
         } // end for

      txn.setFITxnId( date + ":" + currency.format( amount, '.' ) + ":" + description );
      System.err.println(  "FITxnld >" + date + ":" + currency.format( amount, '.' ) + ":" + description + "<" );

      return true;
   }

   @Override
   public String[] getSupportedDateFormats()
   {
      return SUPPORTED_DATE_FORMATS;
   }

   @Override
   public String getDateFormat()
   {
   System.err.println(  "customReader getDateFormat() >" + dateFormatStringSelected + "<" );
   return dateFormatStringSelected;
   }

   @Override
   public void setDateFormat( String format )
   {
   dateFormatStringSelected = customReaderData.getDateFormatString();
   System.err.println(  "customReader setDateFormat() =" + dateFormatStringSelected + "<" );
   System.err.println(  "customReader customReaderDialog.getDateFormatSelected() >" + customReaderData.getDateFormatString() + "<" );
   dateFormat = new CustomDateFormat( customReaderData.getDateFormatString() );
   
      /*
      if ( !DATE_FORMAT_US.equals( format ) )
      {
         throw new UnsupportedOperationException( "Not supported yet." );
      }
       * 
       */
   }

//   @Override
//       public void setFieldSeparatorChar( int xxx) {
//        fieldSeparatorChar.setText( String.valueOf( Character.toString( (char) xxx ) ) );
//    }
//
//   @Override
//    public int getFieldSeparatorChar() {
//        return fieldSeparator;
//    }
    

   @Override
   protected boolean haveHeader()
   {
      return true;
   }

    public CustomReaderData getCustomReaderData() {
        return customReaderData;
    }

    public void setCustomReaderData(CustomReaderData customReaderData) {
        this.customReaderData = customReaderData;
    }
   
   
}
