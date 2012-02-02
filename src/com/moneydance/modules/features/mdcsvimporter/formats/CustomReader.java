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
    public static final String DATA_TYPE_BLANK = "";
    public static final String DATA_TYPE_IGNORE = "ignore";
    public static final String DATA_TYPE_PAYMENT = "-Payment-";
    public static final String DATA_TYPE_DEPOSIT = "-Deposit-";
    public static final String DATA_TYPE_DATE = "date";
    public static final String DATA_TYPE_DATE_AVAILABLE = "date available";
    public static final String DATA_TYPE_DATE_INITIATED = "date initiated";
    public static final String DATA_TYPE_DATE_POSTED = "date posted";
    public static final String DATA_TYPE_DATE_PURCHASED = "date purchased";
    public static final String DATA_TYPE_CHECK_NUMBER = "check number";
    public static final String DATA_TYPE_DESCRIPTION = "description";
    public static final String DATA_TYPE_MEMO = "memo";
    public static final String DATA_TYPE_ACCOUNT_NAME = "account name";

    private static final String DATE_FORMAT_US = "MM/DD/YYYY";
    private static final String DATE_FORMAT_EU = "DD/MM/YY";
    private static final String DATE_FORMAT_JP = "YY/MM/DD";
    private static final String DATE_FORMAT_INTN = "YYYY-MM-DD";

    private String dateFormatStringSelected = DATE_FORMAT_US;
   
   private static String[] SUPPORTED_DATE_FORMATS =
   {
      DATE_FORMAT_US
           , DATE_FORMAT_EU
           , DATE_FORMAT_JP
           , DATE_FORMAT_INTN
   };

    private CustomDateFormat dateFormat = new CustomDateFormat( DATE_FORMAT_US );
    //private CustomDateFormat dateFormat;
   
    //private String[] compatibleDateFormats;
    private String dateFormatString;

    private long amount = 0;
    private int date = 0;
    private int dateAvailable = 0;
    private int dateInitiated = 0;
    private int datePosted = 0;
    private int datePurchased = 0;
    private String description = "";
    private String checkNumber = "";
    private String phoneString;
    private String memo;
    private String accountName;
   
   public CustomReader( CustomReaderData customReaderData )
        {
        setCustomReaderData( customReaderData );
        setCustomReaderFlag( true );
        }
   
   
   @Override
    public void setSupportedDateFormats( String[] supportedDateFormats ) 
        {
        SUPPORTED_DATE_FORMATS = supportedDateFormats;
        }
    
    public void createSupportedDateFormats( String dateFormatArg ) 
        {
         System.err.println(  "\n---------   entered createSupportedDateFormats() dateFormatArg =" + dateFormatArg + "=  -------------" );
        String[] tmp = new String[1];
        tmp[0] = dateFormatArg;
        SUPPORTED_DATE_FORMATS = tmp;
        setDateFormat( dateFormatArg );
        }
	
   @Override
   public boolean canParse( CSVData data )
        {
         System.err.println(  "\n---------   entered customerReader().canParse() as type =" + getFormatName() + "=  -------------" );
        try {
            data.parseIntoLines( getCustomReaderData().getFieldSeparatorChar() );
            } 
        catch (IOException ex) 
            {
            //Logger.getLogger(CustomReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            }

      //if ( data.getReader() == null )
       //   System.err.println( "data.getReader() == null" );
          
      //System.err.println( "at parse getFieldSeparator() =" + (char)csvData.getReader().getFieldSeparator() + "=" );
      //csvData.getReader().setFieldSeparator( getCustomReaderData().getFieldSeparatorChar() );
      //System.err.println( "at parse getFieldSeparator() after set =" + (char)csvData.getReader().getFieldSeparator() + "=" );
        
        int skipHeaderLines = getHeaderCount();
        for ( int i = 0; i < skipHeaderLines; i++ )
            {
            System.err.println( "skip header line" );
            data.nextLine();
            }
      
      boolean retVal = true;
      int maxFieldIndex = getCustomReaderData().getNumberOfCustomReaderFieldsUsed();

      setDateFormat( getCustomReaderData().getDateFormatString() );
      System.err.println(  "using dateFormat string =" + getCustomReaderData().getDateFormatString() + "=" );
      
      while ( retVal && data.nextLine() )
         {
         System.err.println(  "------- next line ---------------" );
         if ( ! data.hasZeroFields() )
            {
            continue; // skip empty lines
            }

         if ( ! data.hasEnoughFieldsPerCurrentLine( maxFieldIndex - 1 ) )
            {
            System.err.println(  "Have too few fields. Needed >= " + ( maxFieldIndex - 2 ) );
            retVal = false;
            }

         int fieldIndex = 0;
         System.err.println(  "maxFieldIndex =" + maxFieldIndex );
         
         for (           ; retVal && fieldIndex < maxFieldIndex; fieldIndex ++ )
             {
             String dataTypeExpecting = getCustomReaderData().getDataTypesList().get( fieldIndex );
             System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  fieldIndex = " + fieldIndex );

             data.nextField();
//             if ( ! data.nextField() )
//                {
//                System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but have no data left." );
//                retVal = false;
//                break;
//                }
             String fieldString = data.getField();
             
             if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_IGNORE ) )
                {
                continue;
                }
             else if ( ( fieldString == null || fieldString.equals( DATA_TYPE_BLANK ) ) )
                {
                if ( ! getCustomReaderData().getEmptyFlagsList().get( fieldIndex ).equals( "Can Be Blank" ) )
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
                
             if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE ) 
                   ||  dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_AVAILABLE )
                   ||  dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_INITIATED )
                   ||  dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_POSTED )
                   ||  dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_PURCHASED )
                     )
                {
                System.err.println(  "date >" + fieldString + "<" );
                System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );
                
                /*
                  // find guessable date formats
                //  if ( retVal )
                  {
                  DateGuesser guesser = new DateGuesser();
                  guesser.checkDateString( fieldString );
 
                     //compatibleDateFormats = guesser.getPossibleFormats();
                     SUPPORTED_DATE_FORMATS = guesser.getPossibleFormats();
                     importDialog.popComboDateFormatList( SUPPORTED_DATE_FORMATS );
                     if ( dateFormatStringSelected == null ||
                        ! findDateFormat( SUPPORTED_DATE_FORMATS, dateFormatStringSelected ) )
                     {
                        setDateFormat( guesser.getBestFormat() );
                     }
                  }
                */
              /**/
                 if ( dateFormat.parseInt( fieldString ) != dateFormat.parseInt( dateFormat.format( dateFormat.parseInt( fieldString ) ) ) )
                 {
                    retVal = false;
                    break;
                 }
              /**/
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_PAYMENT ) 
                         || dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DEPOSIT ) )   // was only amount before
                {
                System.err.println(  "amountString >" + fieldString + "<" );
                fieldString = fieldString.replaceAll( "\\((.*)\\)", "-$1" );

                try
                     {
                        //StringUtils.parseDoubleWithException( fieldString, '.' );
                        String tmp = fieldString.replace( '$', '0' );
                        //System.err.println(  "check modified amountString 1 >" + tmp + "<" );
                        tmp = tmp.replace( '-', '0' );
                        //System.err.println(  "check modified amountString 2 >" + tmp + "<" );
                        tmp = tmp.replaceAll( " ", "" );
                        //System.err.println(  "check modified amountString 3 >" + tmp + "<" );
                        tmp = tmp.replaceAll( ",", "" );
                        //System.err.println(  "check modified amountString 4 >" + tmp + "<" );
                        tmp = tmp.replaceAll( "\\.", "" );
                        //System.err.println(  "check modified amountString 5 >" + tmp + "<" );
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
             else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DESCRIPTION ) )
                {
                System.err.println(  "description >" + fieldString + "<" );
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_MEMO) )
                {
                System.err.println(  "memo >" + fieldString + "<" );
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( "tag" ) )
                {
                System.err.println(  "tag >" + fieldString + "<" );
                }
             else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_ACCOUNT_NAME ) )
                {
                System.err.println(  "accountName >" + fieldString + "<" );
                accountName = fieldString;

                if ( rootAccount.getAccountByName( accountName ) == null )
                    {
                    System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but that account does not exist =" + fieldString + "= and STOP ON ERROR" );
                    retVal = false;
                    break;
                    }
                this.accountNameFromCSV = accountName;
                }
             } // end for
      }

      return retVal;
   }

   @Override
   public String getFormatName()
   {
      return getCustomReaderData().getReaderName();
   }

   /*
    * Note: This really parses a whole line at a time.
    */
   @Override
   protected boolean parseNext() throws IOException
   {
     amount = 0;
     date = 0;
     dateAvailable = 0;
     dateInitiated = 0;
     datePosted = 0;
     datePurchased = 0;
     description = "";
     checkNumber = "";
     phoneString = "";
     memo = "";
     accountName = "";

     int fieldIndex = 0;
     int maxFieldIndex = getCustomReaderData().getNumberOfCustomReaderFieldsUsed();
     System.err.println(  "maxFieldIndex =" + maxFieldIndex );

     setDateFormat( getCustomReaderData().getDateFormatString() );
     System.err.println(  "using dateFormat string =" + getCustomReaderData().getDateFormatString() + "=" );
     
     System.err.println(  "----------------------" );
     if ( ! csvData.hasZeroFields() )
        {
        System.err.println(  "skip empty line" );
        return false; // skip empty lines
        }

     for (           ; fieldIndex < maxFieldIndex; fieldIndex ++ )
         {
         String dataTypeExpecting = getCustomReaderData().getDataTypesList().get( fieldIndex );
         System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  EmptyFlagsList = " + getCustomReaderData().getEmptyFlagsList().get( fieldIndex ) + "=" );

         csvData.nextField();
         String fieldString = csvData.getField();
         System.err.println(  "fieldString =" + fieldString + "=  fieldIndex = " + fieldIndex );

         if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_IGNORE ) )
            {
            continue;
            }
         else if ( ( fieldString == null || fieldString.equals( "" ) )
                    && ! getCustomReaderData().getEmptyFlagsList().get( fieldIndex )
					.equals( "Can Be Blank" ) )
            {
            System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but got no value =" + fieldString + "= and STOP ON ERROR" );
            throwException( "dataTypeExpecting =" + dataTypeExpecting + "=  but got no value =" + fieldString + "= and STOP ON ERROR" );
            }
         
         if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE ) )
            {
            System.err.println(  "date >" + fieldString + "<" );
            System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            date = dateFormat.parseInt( fieldString );

//            txn.setDatePostedInt( date );
//            txn.setDateInitiatedInt( date );
//            txn.setDateAvailableInt( date );
          /*
             if ( !date.equals( dateFormat.format( dateFormat.parseInt( csvData.getField() ) ) ) )
             {
                retVal = false;
                break;
             }
          */
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_AVAILABLE ) )
            {
            System.err.println(  "dateAvailable >" + fieldString + "<" );
            System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            dateAvailable = dateFormat.parseInt( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_INITIATED) )
            {
            System.err.println(  "dateInitiated >" + fieldString + "<" );
            System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            dateInitiated = dateFormat.parseInt( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_POSTED ) )
            {
            System.err.println(  "datePosted >" + fieldString + "<" );
            System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            datePosted = dateFormat.parseInt( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DATE_PURCHASED ) )
            {
            System.err.println(  "datePurchased >" + fieldString + "<" );
            System.err.println(  "fieldString =" + fieldString + "=   date formatted >" + dateFormat.format( dateFormat.parseInt( fieldString ) ) + "<" );

            datePurchased = dateFormat.parseInt( fieldString );
            }
         else if ( ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_PAYMENT )
                      || dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DEPOSIT ) )
                                        &&
                     ! ( fieldString == null || fieldString.equals( "" ) ) )
            {
            System.err.println(  "amountString >" + fieldString + "<" );
            fieldString = fieldString.replaceAll( "\\((.*)\\)", "-$1" );
            
            try
                {
                double amountDouble = StringUtils.parseDoubleWithException( fieldString, '.' );
                if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_PAYMENT ) )
                    {
                    amount += currency.getLongValue( amountDouble );
                    }
                else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DEPOSIT ) )
                    {
                    System.err.println(  "flip sign for deposit" );
                    amount -= currency.getLongValue( amountDouble );
                    }
                }
            catch ( Exception x )
                {
                throwException( "Invalid amount." );
                }
//            txn.setAmount( amount );
//            txn.setTotalAmount( amount );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_CHECK_NUMBER ) )
            {
            //origCheckNumber = fieldString;
            /*  changed matching to use original check number which contained leading 0's so go back to using that. Stan
            if ( fieldString != null )
                {
                    // NOTE: I had to do this because I could set ck # = 004567 but get() returns 4567 so matching would not work. Stan
                fieldString = fieldString.replaceAll( "^0*(.*)", "$1" );
                }
                 */
            System.err.println(  "check number >" + fieldString + "<" );
			checkNumber = fieldString;
//            txn.setCheckNum( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_DESCRIPTION ) )
            {
            System.err.println(  "description >" + fieldString + "<" );
//            txn.setName( fieldString );
            description = fieldString;
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_MEMO ) )
            {
            System.err.println(  "memo >" + fieldString + "<" );
			memo = fieldString;
//            txn.setMemo( fieldString );
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( "tag" ) )
            {
            System.err.println(  "tag in phone field >" + fieldString + "<" );
            // storing it into phone field for now since onlinetxn cannot handle tags. A kludge for now.  Stan
//            txn.setPhone( fieldString );
			phoneString = fieldString;
            }
         else if ( dataTypeExpecting.equalsIgnoreCase( DATA_TYPE_ACCOUNT_NAME ) )
            {
            System.err.println(  "accountName >" + fieldString + "<" );
            accountName = fieldString;

            if ( rootAccount.getAccountByName( accountName ) == null )
                {
                System.err.println(  "dataTypeExpecting =" + dataTypeExpecting + "=  but that account does not exist =" + fieldString + "= and STOP ON ERROR" );
                throwException( "dataTypeExpecting =" + dataTypeExpecting + "=  but that account does not exist =" + fieldString + "= and STOP ON ERROR" );
                }
            this.accountNameFromCSV = accountName;
            }
         } // end for

     // MOVED to TransactionReader so everyone creates it the same way.
//      txn.setFITxnId( date + ":" + currency.format( amount, '.' ) + ":" + description + ":" + txn.getCheckNum() + ":" + txn.getMemo() );
//      System.err.println(  "FITxnld >" + date + ":" + currency.format( amount, '.' ) + ":" + description + ":" + txn.getCheckNum() + ":" + txn.getMemo() + "<" );

      return true;
   }

   @Override
   protected boolean assignDataToTxn( OnlineTxn txn ) throws IOException
    {
    txn.setAmount( amount );
    txn.setTotalAmount( amount );

    // ---  set a default date to be used if particular dates are not set  ---s
    if ( date == 0 )
        {
        if ( dateInitiated != 0 )
            {
            date = dateInitiated;
            }
        else if ( datePurchased != 0 )
            {
            date = datePurchased;
            }
        else if ( datePosted != 0 )
            {
            date = datePosted;
            }
        else if ( dateAvailable != 0 )
            {
            date = dateAvailable;
            }
        else
            {
            System.err.println(  "*** Error: No Date field is set !" );
            throwException( "*** Error: No Date field is set !" );
            }
        }
    
    if ( dateAvailable != 0 )
        {
        txn.setDateAvailableInt( dateAvailable );
        }
    else
        {
        txn.setDateAvailableInt( date );
        }
    
    if ( dateInitiated != 0 )
        {
        txn.setDateInitiatedInt( dateInitiated );
        }
    else
        {
        txn.setDateInitiatedInt( date );
        }
    
    if ( datePosted != 0 )
        {
        txn.setDatePostedInt( datePosted );
        }
    else
        {
        txn.setDatePostedInt( date );
        }
    
    if ( datePurchased != 0 )
        {
        txn.setDatePurchasedInt( datePurchased );
        }
    else
        {
        txn.setDatePurchasedInt( date );
        }

//    System.err.println(  "date >" + date + "<" );
//    System.err.println(  "date >" + txn.getDateAvailableInt() + "<" );
//    System.err.println(  "date >" + txn.getDateInitiatedInt() + "<" );
//    System.err.println(  "date >" + txn.getDatePostedInt() + "<" );
//    System.err.println(  "date >" + txn.getDatePurchasedInt() + "<" );

    txn.setCheckNum( checkNumber );
    txn.setName( description );
    txn.setMemo( memo );
    txn.setPhone( phoneString );
		// MOVED to TransactionReader so everyone creates it the same way.
//		txn.setFITxnId( date + ":" + currency.format( amount, '.' )
//				+ ":" + description + ":" + txn.getCheckNum() + ":" + txn.getMemo() );
    //System.err.println(  "FITxnld >" + date + ":" + currency.format( amount, '.' )
    //                        + ":" + description + ":" + txn.getCheckNum() + ":" + txn.getMemo() + "<" );
//(date == 0 ? datePurchased : date)
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
      if ( format == null )
      {
         return;
      }

   System.err.println(  "setDateFormat() format =" + format + "=   dateFormatString =" + dateFormatString + "=" );
      if ( ! format.equals( dateFormatStringSelected ) )
      {
         dateFormat = new CustomDateFormat( format );
         dateFormatStringSelected = format;
      }

      /*
   dateFormatStringSelected = getCustomReaderData().getDateFormatString();
   System.err.println(  "customReader setDateFormat() =" + dateFormatStringSelected + "<" );
   System.err.println(  "customReader customReaderDialog.getDateFormatSelected() >" + getCustomReaderData().getDateFormatString() + "<" );
   dateFormat = new CustomDateFormat( getCustomReaderData().getDateFormatString() );
   */
      
      /*
      if ( !DATE_FORMAT_US.equals( format ) )
      {
         throw new UnsupportedOperationException( "Not supported yet." );
      }
       * 
       */
   }

   private static boolean findDateFormat( String[] compatibleDateFormats, String dateFormatStringArg )
   {
      if ( dateFormatStringArg == null )
      {
         return false;
      }

      for ( String s : compatibleDateFormats )
      {
         if ( s.equals( dateFormatStringArg ) )
         {
            return true;
         }
      }

      return false;
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
   protected int getHeaderCount()
   {
      return getCustomReaderData().getHeaderLines();
   }   
   
}
