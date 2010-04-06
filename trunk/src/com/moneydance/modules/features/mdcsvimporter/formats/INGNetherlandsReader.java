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

public class INGNetherlandsReader
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
   public String getFormatName()
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

      Integer hashCode = naam.hashCode() ^ rekening.hashCode() ^
         tegenrekening.hashCode() ^ code.hashCode() ^ af_bij.hashCode() ^
         mutatiesort.hashCode() ^ mededelingen.hashCode();

      txn.setAmount( amount );
      txn.setTotalAmount( amount );
      txn.setMemo( mededelingen );
      txn.setFITxnId( datum + ":" + bedrag + ":" + hashCode.toString() );
      txn.setDatePostedInt( date );
      txn.setDateInitiatedInt( date );
      txn.setDateAvailableInt( date );
      txn.setPayeeName( naam );

      return true;
   }
}
