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
import com.moneydance.modules.features.mdcsvimporter.TransactionReader;
import com.moneydance.util.CustomDateFormat;
import com.moneydance.util.StringUtils;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author miki
 */
public class SimpleCreditDebitReader
   extends TransactionReader
{
   private static final String DATE = "date";
   private static final String DESCRIPTION = "description";
   private static final String CREDIT = "credit";
   private static final String DEBIT = "debit";
   private CustomDateFormat dateFormat = new CustomDateFormat( "DD/MM/YYYY" );

   public static boolean canParse( Iterator<String> columns )
      throws IOException
   {
      try
      {
         return DATE.equals( columns.next() ) &&
            DESCRIPTION.equals( columns.next() ) &&
            CREDIT.equals( columns.next() ) &&
            DEBIT.equals( columns.next() ) &&
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
      return "Simple Date/Description/Credit/Debit";
   }

   @Override
   protected boolean parseNext( OnlineTxn txn )
      throws IOException
   {
      String dateString = reader.nextField();
      if ( dateString == null || dateString.length() == 0 )
      { // empty line
         return false;
      }
      String description = reader.nextField();
      String credit = reader.nextField();
      String debit = reader.nextField();
      if ( credit == null && debit == null )
      {
         throwException( "Invalid line." );
      }
      
      if ( credit.length() == 0 && debit.length() == 0 )
      {
         throwException( "Credit and debit fields are both empty." );
      }

      long amount = 0;
      try
      {
         double amountDouble;
         if ( credit.length() > 0 )
         {
            amountDouble = StringUtils.parseDoubleWithException( credit, '.' );
         }
         else
         {
            amountDouble = -StringUtils.parseDoubleWithException( debit, '.' );
         }
         amount = currency.getLongValue( amountDouble );
      }
      catch ( Exception x )
      {
         throwException( "Invalid amount." );
      }

      int date = dateFormat.parseInt( dateString );

      txn.setAmount( amount );
      txn.setTotalAmount( amount );
      txn.setMemo( description );
      txn.setFITxnId( date + ":" + currency.format( amount, '.' ) + ":" + description );
      txn.setDatePostedInt( date );
      txn.setDateInitiatedInt( date );
      txn.setDateAvailableInt( date );

      return true;
   }
}
