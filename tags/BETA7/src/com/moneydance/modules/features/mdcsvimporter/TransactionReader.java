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
import com.moneydance.util.CustomDateFormat;
import com.moneydance.util.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author miki
 */
public abstract class TransactionReader
{
   protected CSVReader reader;
   protected Account account;
   protected OnlineTxnList transactionList;
   protected CurrencyType currency;

   protected abstract boolean parseNext( OnlineTxn txn )
      throws IOException;

   protected abstract String getFormatName();

   protected final void throwException( String message )
      throws IOException
   {
      throw new IOException( message );
   }

   public final void parse( CSVReader reader, Account account )
      throws IOException
   {
      this.reader = reader;
      this.account = account;
      this.transactionList = account.getDownloadedTxns();
      this.currency = account.getCurrencyType();

      reader.nextLine(); // skip the header

      while ( reader.nextLine() )
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
            transactionList.addNewTxn( txn );
         }
      }
   }

   public static TransactionReader create( CSVReader reader )
      throws IOException
   {
      TransactionReader retVal = null;

      if ( !reader.nextLine() )
      {
         throw new IOException( "File is empty." );
      }

      List<String> columns = new ArrayList<String>();

      for ( String field = reader.nextField(); field != null; field = reader.nextField() )
      {
         field = field.trim();
         if ( field.length() == 0 )
         {
            throw new IOException( "Empty column names are not supported." );
         }
         field = field.toLowerCase();
         columns.add( field );
      }

      if ( columns.isEmpty() )
      {
         throw new IOException( "No columns found." );
      }

      if ( CitiBankCanadaReader.canParse( columns.iterator() ) )
      {
         retVal = new CitiBankCanadaReader();
      }
      else if ( INGNetherlandsReader.canParse( columns.iterator() ) )
      {
         retVal = new INGNetherlandsReader();
      }

      return retVal;
   }
}

class CitiBankCanadaReader
   extends TransactionReader
{
   private static final String TRANSACTION_DATE = "transaction date";
   private static final String POSTING_DATE = "posting date";
   private static final String DESCRIPTION = "description";
   private static final String AMOUNT = "amount";
   private CustomDateFormat dateFormat = new CustomDateFormat( "MM/DD/YYYY" );

   public static boolean canParse( Iterator<String> columns )
      throws IOException
   {
      try
      {
         return TRANSACTION_DATE.equals( columns.next() ) &&
            POSTING_DATE.equals( columns.next() ) &&
            DESCRIPTION.equals( columns.next() ) &&
            AMOUNT.equals( columns.next() ) &&
            !columns.hasNext();
      }
      catch ( Throwable x )
      {
         return false;
      }
   }

   @Override
   protected String getFormatName()
   {
      return "CitiBank Canada";
   }

   @Override
   protected boolean parseNext( OnlineTxn txn )
      throws IOException
   {
      String transactionDateString = reader.nextField();
      if ( transactionDateString == null )
      { // empty line
         return false;
      }
      if ( transactionDateString.equalsIgnoreCase( "Date downloaded:" ) )
      { // skip the footer line
         return false;
      }
      String postingDateString = reader.nextField();
      String description = reader.nextField();
      String amountString = reader.nextField();
      if ( amountString == null )
      {
         throwException( "Invalid line." );
      }

      long amount = 0;
      try
      {
         double amountDouble = StringUtils.parseDoubleWithException( amountString, '.' );
         amount = currency.getLongValue( amountDouble );
      }
      catch ( Exception x )
      {
         throwException( "Invalid amount." );
      }

      int transactionDate = dateFormat.parseInt( transactionDateString );
      int postingDate = dateFormat.parseInt( postingDateString );

      txn.setAmount( amount );
      txn.setTotalAmount( amount );
      txn.setMemo( description );
      txn.setFITxnId( postingDate + ":" + amountString + ":" + description );
      txn.setDatePostedInt( postingDate );
      txn.setDateInitiatedInt( transactionDate );
      txn.setDateAvailableInt( postingDate );

      return true;
   }
}

class INGNetherlandsReader
   extends TransactionReader
{
   private static final String DATUM = "datum";
   private static final String NAAM_OMSCHRIJVING = "naam / omschrijving";
   private static final String REKENING = "rekening";
   private static final String TEGENREKENING = "tegenrekening";
   private static final String CODE = "code";
   private static final String AF_BIJ = "af bij";
   private static final String BEDRAG_EUR = "bedrag (eur)";
   private static final String MUTATIESORT = "mutatiesoort";
   private static final String MEDEDELINGEN = "mededelingen";
   private CustomDateFormat dateFormat = new CustomDateFormat( "D-M-YYYY" );

   public static boolean canParse( Iterator<String> columns )
      throws IOException
   {
      try
      {
         return DATUM.equals( columns.next() ) &&
            NAAM_OMSCHRIJVING.equals( columns.next() ) &&
            REKENING.equals( columns.next() ) &&
            TEGENREKENING.equals( columns.next() ) &&
            CODE.equals( columns.next() ) &&
            AF_BIJ.equals( columns.next() ) &&
            BEDRAG_EUR.equals( columns.next() ) &&
            MUTATIESORT.equals( columns.next() ) &&
            MEDEDELINGEN.equals( columns.next() ) &&
            !columns.hasNext();
      }
      catch ( Throwable x )
      {
         return false;
      }
   }

   @Override
   protected String getFormatName()
   {
      return "ING The Netherlands";
   }

   @Override
   protected boolean parseNext( OnlineTxn txn )
      throws IOException
   {
      String datum = reader.nextField();
      if ( datum == null )
      { // empty line
         return false;
      }
      String naam = reader.nextField();
      String rekening = reader.nextField();
      String tegenrekening = reader.nextField();
      String code = reader.nextField();
      String af_bij = reader.nextField();
      String bedrag = reader.nextField();
      String mutatiesort = reader.nextField();
      String mededelingen = reader.nextField();
      if ( mededelingen == null )
      {
         throwException( "Invalid line." );
      }

      long amount = 0;
      try
      {
         double amountDouble = StringUtils.parseDoubleWithException( bedrag, ',' );
         amount = currency.getLongValue( amountDouble );
      }
      catch ( Exception x )
      {
         throwException( "Invalid amount." );
      }

      if ( af_bij.equalsIgnoreCase( "af" ) )
      {
         amount = -amount;
      }
      else if ( af_bij.equalsIgnoreCase( "bij" ) )
      {
      }
      else
      {
         throwException( "Value of Af/Bij field must be 'Af' or 'Bij'." );
      }

      int date = dateFormat.parseInt( datum );

      txn.setAmount( amount );
      txn.setTotalAmount( amount );
      txn.setMemo( mededelingen );
      txn.setFITxnId( tegenrekening );
      txn.setDatePostedInt( date );
      txn.setDateInitiatedInt( date );
      txn.setDateAvailableInt( date );
      txn.setPayeeName( naam );

      return true;
   }
}
