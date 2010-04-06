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

import com.moneydance.modules.features.mdcsvimporter.TransactionReader;
import com.moneydance.apps.md.model.OnlineTxn;
import com.moneydance.util.CustomDateFormat;
import com.moneydance.util.StringUtils;
import java.io.IOException;
import java.util.Iterator;

public class CitiBankCanadaReader
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
   public String getFormatName()
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

