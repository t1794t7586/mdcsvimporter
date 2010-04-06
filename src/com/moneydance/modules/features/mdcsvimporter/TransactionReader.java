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
import com.moneydance.modules.features.mdcsvimporter.formats.CitiBankCanadaReader;
import com.moneydance.modules.features.mdcsvimporter.formats.INGNetherlandsReader;
import com.moneydance.modules.features.mdcsvimporter.formats.SimpleCreditDebitReader;
import java.io.IOException;
import java.util.ArrayList;
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

   public abstract String getFormatName();

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
      else if ( SimpleCreditDebitReader.canParse( columns.iterator() ) )
      {
         retVal = new SimpleCreditDebitReader();
      }

      return retVal;
   }
}

