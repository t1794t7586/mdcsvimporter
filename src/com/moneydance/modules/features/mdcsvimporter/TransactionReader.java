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

import com.moneydance.apps.md.model.Account;
import com.moneydance.apps.md.model.CurrencyType;
import com.moneydance.apps.md.model.OnlineTxn;
import com.moneydance.apps.md.model.OnlineTxnList;
import com.moneydance.modules.features.mdcsvimporter.formats.CustomReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author miki and Stan Towianski
 */
public abstract class TransactionReader
{
   private boolean customReaderFlag = false;
   private CustomReaderData customReaderData = null;
   
   protected static CustomReaderDialog customReaderDialog = null;
   
   protected CSVData csvData;
   protected Account account;
   protected OnlineTxnList transactionList;
   protected CurrencyType currency;

   protected abstract boolean canParse( CSVData data );

   protected abstract boolean parseNext( OnlineTxn txn )
      throws IOException;

   public abstract String getFormatName();

   public abstract String[] getSupportedDateFormats();

   public abstract String getDateFormat();

   public abstract void setDateFormat( String format );

   //public abstract int getFieldSeparator();

   //public abstract void setFieldSeparator( int xxx );

   protected final void throwException( String message )
      throws IOException
   {
      throw new IOException( message );
   }

   public final void parse( CSVData csvDataArg, Account account )
      throws IOException
   {
      this.csvData = csvDataArg;
      this.account = account;
      this.transactionList = account.getDownloadedTxns();
      this.currency = account.getCurrencyType();

      //csvData.reset();
        if ( this instanceof CustomReader )
            {
            csvData.parseIntoLines( customReaderData.getFieldSeparatorChar() );
            }
        else
            {
            csvData.parseIntoLines( 0 );
            }

      //System.err.println( "at parse getFieldSeparator() =" + (char)csvData.getReader().getFieldSeparator() + "=" );
      //csvData.getReader().setFieldSeparator( customReaderDialog.getFieldSeparatorChar() );
      //System.err.println( "at parse getFieldSeparator() after set =" + (char)csvData.getReader().getFieldSeparator() + "=" );

      //----- Skip Header Lines  -----
        if ( this instanceof CustomReader )
            {
//            int skipHeaderLines = customReaderDialog.getHeaderLines();
            int skipHeaderLines = getCustomReaderData().getHeaderLines();
            for ( int i = 0; i < skipHeaderLines; i++ )
                {
                System.err.println(  "skip header for customReader" );
                csvData.nextLine();
                }
            }
        else
            {
              if ( haveHeader() )
                {
                 csvData.nextLine(); // skip the header
                }
            }
      
      while ( csvData.nextLine() )
      {
         OnlineTxn txn = transactionList.newTxn();
         if ( parseNext( txn ) )
         {
            txn.setProtocolType( OnlineTxn.PROTO_TYPE_OFX );
            if ( account.balanceIsNegated() )
            {
               txn.setAmount( -txn.getAmount() );
               txn.setTotalAmount( -txn.getAmount() );
            }
            System.err.println( "will add transaction" );
            transactionList.addNewTxn( txn );
         }
      }
   }

   public void setCustomReaderDialog( CustomReaderDialog customReaderDialog )
        {
            System.err.println( "custreader set custreaderdialog" );
        this.customReaderDialog = customReaderDialog;
        }
   
   public int getNumberOfCustomReaderFieldsUsed()
        {
        if ( this.customReaderDialog == null ) 
            return 0;
        else 
            return this.customReaderDialog.getNumberOfCustomReaderFieldsUsed();
        }
   
   public static TransactionReader[] getCompatibleReaders( File selectedFile, ImportDialog importDialog )
   {
      ArrayList<TransactionReader> formats = new ArrayList<TransactionReader>();

      System.err.println( "getCompatibleReaders() call cust read canParse()" );
      
      for ( String key : Settings.getReaderHM().keySet() )
            {
            TransactionReader transactionReader = Settings.getReaderHM().get( key );
            System.err.println( "at canparse for transReader =" + key + "=" );
            
             try
             {
                CSVReader csvReader = new CSVReader( new FileReader( selectedFile ) );
                CSVData csvData = new CSVData( csvReader );
            
                if ( transactionReader.canParse( csvData ) )
                      {
                      System.err.println( "------- at canparse WORKS for =" + key + "=" );
                      formats.add( transactionReader );
                      }
                else
                      {
                      System.err.println( "------- at canparse not work for =" + key + "=" );
                      }
                csvReader.close();
             }
             catch ( Throwable x )
             {
                  System.err.println( "at canparse error reading file !" );
             }
            }
      
      /*
      if ( customerReaderName != null && ! customerReaderName.equals( "" ) )
        {

          System.err.println( "at canparse getFieldSeparator() =" + (char)data.getReader().getFieldSeparator() + "=" );

          //data.getReader().setFieldSeparator( customReaderDialog.getFieldSeparatorChar() );
          //System.err.println( "at canparse getFieldSeparator() after set =" + (char)data.getReader().getFieldSeparator() + "=" );

//s          System.err.println( "at canparse getFieldSeparator() after set =" + (char)data.getReader().getFieldSeparator() + "=" );

          customReader.setDateFormat( importDialog.comboDateFormatGetItem() );
          System.err.println( "at canparse importDialog.comboDateFormatGetItem() after set =" + importDialog.comboDateFormatGetItem() + "=" );
          
          if ( customReader.canParse( data ) )
              {
             formats.add( customReader );
            }
        }
      
      if ( citiBankCanadaReader.canParse( data ) )
      {
         formats.add( citiBankCanadaReader );
      }

      if ( ingNetherlandsReader.canParse( data ) )
      {
         formats.add( ingNetherlandsReader );
      }

      if ( simpleCreditDebitReader.canParse( data ) )
      {
         formats.add( simpleCreditDebitReader );
      }

      if ( wellsFargoReader.canParse( data ) )
      {
         formats.add( wellsFargoReader );
      }
       */
      
      TransactionReader[] retVal = new TransactionReader[formats.size()];
      formats.toArray( retVal );
      return retVal;
   }

   @Override
   public String toString()
   {
      return getFormatName();
   }

   protected abstract boolean haveHeader();
   
   /*
   protected String convertParensToMinusSign( String amt )
   {
       return amt.replaceAll( "\(.*\)", "-\$1" );
   }
    */

    public boolean isCustomReaderFlag() {
        return customReaderFlag;
    }

    public void setCustomReaderFlag(boolean customReaderFlag) {
        this.customReaderFlag = customReaderFlag;
    }

    public CustomReaderData getCustomReaderData() {
        return customReaderData;
    }

    public void setCustomReaderData(CustomReaderData customReaderData) {
        this.customReaderData = customReaderData;
    }

}

