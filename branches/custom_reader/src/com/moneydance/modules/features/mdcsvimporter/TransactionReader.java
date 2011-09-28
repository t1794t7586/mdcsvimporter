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

import com.moneydance.apps.md.model.TransactionSet;
import com.moneydance.apps.md.model.Account;
import com.moneydance.apps.md.model.RootAccount;
import com.moneydance.apps.md.model.CurrencyType;
import com.moneydance.apps.md.model.AbstractTxn;
import com.moneydance.apps.md.model.OnlineTxn;
import com.moneydance.apps.md.model.OnlineTxnList;
import com.moneydance.apps.md.model.ParentTxn;
import com.moneydance.apps.md.model.SplitTxn;

import com.moneydance.apps.md.model.TxnSet;
import com.moneydance.modules.features.mdcsvimporter.formats.CustomReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author miki and Stan Towianski
 */
public abstract class TransactionReader
{
   private boolean customReaderFlag = false;
   private CustomReaderData customReaderData = null;
   
   protected ImportDialog importDialog = null;
   protected static CustomReaderDialog customReaderDialog = null;
   
   protected CSVData csvData;
   protected Account account;
   protected OnlineTxnList transactionList;
   protected TransactionSet txnSet;
   protected CurrencyType currency;
   protected HashSet tsetMatcherKey = new HashSet<String>();
   
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

   public final void parse( CSVData csvDataArg, Account account, RootAccount rootAccount )
      throws IOException
   {
      System.err.println(  "\n---------   entered TransactionReader().parse()  -------------" );

      this.csvData = csvDataArg;
      this.account = account;
      this.transactionList = account.getDownloadedTxns();
      this.txnSet = rootAccount.getTransactionSet();
      this.tsetMatcherKey = new HashSet();
      this.currency = account.getCurrencyType();
      long totalProcessed = 0;
      long totalAccepted = 0;
      long totalRejected = 0;
      long totalDuplicates = 0;
      
      System.err.println(  "\n---------   beg: make set of existing account transactions  -------------" );
      //System.err.println(  "number of trans list =" +this.txnSet.getTransactionsForAccount( account ).getSize()  );
      System.err.println(  "number of trans list =" +this.txnSet.getAllTxns().getSize()  );
      // cannot get just for account because I am putting them into a temp/empty account !
      //Enumeration<AbstractTxn> tenums = this.txnSet.getTransactionsForAccount( account ).getAllTxns();
      TxnSet tset = this.txnSet.getAllTxns();
      System.err.println(  "tset.getSize() =" +tset.getSize()  );
      
      /*
     while ( tenums.hasMoreElements() ) 
            {
            AbstractTxn key = tenums.nextElement();
            System.err.println( "key.getDescription() =" + key.getDescription() + "=   key.getFiTxnId( 0 )" + key.getFiTxnId( 0 ) + "=" );
            tsetMatcherKey.add( key.getFiTxnId( 0 ) );
            }
       * 
       */
      
      // Create a pattern to match comments
      Pattern ckNumPat = Pattern.compile( "^.*\\\"chknum\\\" = \\\"(.*?)\\\"\n.*$", Pattern.MULTILINE );
      String origCheckNumber = null;
      
      //int k = 0;
      for ( AbstractTxn atxn : tset )
          {
            //System.err.println( "key.getDescription() =" + atxn.getDescription() + "=   atxn.getFiTxnId( 1 ) =" + atxn.getFiTxnId( 1 ) + "=" );
            //System.err.println( "atxn.getFiTxnId( 1 ) [" + k + "] =" + atxn.getFiTxnId( 1 ) + "=   atxn.getFiTxnId( 0 ) [" + k + "] =" + atxn.getFiTxnId( 0 ) + "=" );
            //tsetMatcherKey.add( atxn.getFiTxnId( 1 ) );
            
              // Here I am manually recreating the FiTxnId that I set on imported txn's because I could not figure
              // out how to simply read it.
              
            
            //String tmp = atxn.getDateInt() + ":" + currency.format( atxn.getValue(), '.' ) + ":" + atxn.getDescription() + ":" + atxn.getCheckNumber();

              /*
  <TAG>
   <KEY>ol.orig-txn</KEY>
   <VAL>{&#10;  "dtinit-int" = "20110824"&#10;  "name" = "whatever desc"&#10;  "amt" = "-9824"&#10;  "fitxnid" = "20110824:-98.24:whatever desc"&#10;  "dtpstd-int" = "20110824"&#10;  "dtavail-int" = "20110824"&#10;  "invst.totalamt" = "-9824"&#10;  "chknum" = "001234"&#10;  "ptype" = "1"&#10;}&#10;</VAL>
  </TAG>
              */
              
              String origtxn = atxn.getTag( "ol.orig-txn" );
              //String origCheckNumber = origtxn.replaceAll( ".*\\\"chknum\\\" = \\\"(.*?)\\\"\\\n.*", "$1" );

              //System.out.println( "\norigtxn ="+origtxn + "=" );

              // Run some matches
              if ( origtxn != null )
                  {
                  Matcher m = ckNumPat.matcher( origtxn );
                  if ( m.find() )
                      origCheckNumber = m.group( 1 );
                  else
                      origCheckNumber = "";
                  //System.out.println("Found orig check num ="+m.group( 1 ) + "=" );
                  }
              else
                  {
                  origCheckNumber = "";
                  }
              
            // This new way compare using the ORIGINAL payee and memo fields so if the user changes them, it will still match. Stan
            String tmp = atxn.getDateInt() + ":" + currency.format( atxn.getValue(), '.' ) + ":" 
                               + (atxn.getTag( "ol.orig-payee" ) == null ? "" : atxn.getTag( "ol.orig-payee" )) + ":" 
                               + origCheckNumber
                               + (atxn.getTag( "ol.orig-memo" ) == null ? "" : atxn.getTag( "ol.orig-memo" )) + ":" 
                                      ;            
            
            //System.err.println( "tmp string [" + "k" + "] =" + tmp + "=" );
            tsetMatcherKey.add( tmp );
            
            //k++;
            //if ( k > 9 )
             //   break;
          }
      System.err.println(  "\n---------   end: make set of existing account transactions  -------------" );
      
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
            
            if ( ! tsetMatcherKey.contains( txn.getFITxnId( ) ) )
                {
                System.err.println( "will add transaction with txn.getFITxnId( ) =" + txn.getFITxnId( ) + "=" );
                
                /*  NOTE: This is to convert the online txn to an regular txn. This would let me set categories and tags 
                 * on incoming txn's,  but it automatically sets the category to the default account one and I like it
                 * better using the onlineTxn where it prompts the user to select a category for imported txn's. Stan
                 */
                //ParentTxn pTxn = onlineToParentTxn( account, rootAccount, txn );
                //txnSet.addNewTxn( pTxn );
                
                transactionList.addNewTxn( txn );
                totalAccepted ++;
                }
            else
                {
                System.err.println( "will NOT add Duplicate transaction with txn.getFITxnId( ) =" + txn.getFITxnId( ) + "=" );
                totalDuplicates ++;
                }
         totalProcessed ++;
         }
      }
      
      JOptionPane.showMessageDialog( importDialog, "Total Records Process: " + totalProcessed
                                                                            + "\nRecords Imported: " + totalAccepted
                                                                            + "\nDuplicates Skipped: " + totalDuplicates
                                                        );
   }
   
   /*
    * Note: Create a ParentTxn from a filled out OnlineTxn
    */
 //  @ Override
   protected ParentTxn onlineToParentTxn( Account account, RootAccount rootAccount, OnlineTxn oTxn )
      throws IOException
   {
        Account category = null;

        ParentTxn pTxn = new ParentTxn( oTxn.getDateInitiatedInt(), oTxn.getDateInitiatedInt(), oTxn.getDateInitiatedInt()
                                                          , oTxn.getCheckNum(), account, oTxn.getName(), oTxn.getMemo()
                                                          , -1, AbstractTxn.STATUS_UNRECONCILED );
        
        SplitTxn sptxn = new SplitTxn( pTxn, oTxn.getAmount(), oTxn.getAmount(), 1.0, com.moneydance.apps.md.model.AccountUtil.getDefaultCategoryForAcct(account)  /* category */
                                                    , pTxn.getDescription(), -1, AbstractTxn.STATUS_UNRECONCILED );
        sptxn.setIsNew( true );
        pTxn.addSplit( sptxn );
        
        pTxn.setIsNew( true );
        return pTxn;
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
      importDialog = importDialog;
      
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

