/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
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

import com.moneydance.apps.md.controller.FeatureModule;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.model.RootAccount;
import java.awt.Image;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;

/**
 *
 * @author miki
 */
public class Main extends FeatureModule
{
   private static final int VERSION = 3;
   private static final String NAME = "CSV Importer (Citibank Canada)";
   private static final String VENDOR = "Milutin Jovanović";
   private static final String URL = "http://code.google.com/p/mdcsvimporter/";
   private static final String DESCRIPTION =
      "Moneydance CitiBank Canada CSV Importer Plug-In version BETA 3. " +
      "To report problems or make suggestions please go to the web side below.\n\n" +
      "This software is distributed under GNU Lesser General Public License (see " +
      "http://www.gnu.org/licenses/ for details). If you continue, you acknowledge " +
      "accepting terms of this license.";
   
   private static Image image;
   {
      try
      {
         image = ImageIO.read(
            Main.class.getResourceAsStream( "import.png" ) );
      }
      catch ( IOException x )
      {
         // ignore error; nothing we can do about it
      }
   }

   public Main()
   {
   }

   @Override
   public void init()
   {
      FeatureModuleContext context = getContext();
      if ( context == null )
      {
         return;
      }

      context.registerFeature( this, "import", image, "Import CitiBank Canada CSV File" );
   }

   RootAccount getRootAccount()
   {
      FeatureModuleContext context = getContext();

      return context.getRootAccount();
   }

   @Override
   public String getName()
   {
      return NAME;
   }

   @Override
   public int getBuild()
   {
      return VERSION;
   }

   @Override
   public String getDescription()
   {
      return DESCRIPTION;
   }

   @Override
   public void invoke( String uri )
   {
      StringTokenizer tokenizer = new StringTokenizer( uri, ":" );

      int count = tokenizer.countTokens();
      String url = count + " tokens(";
      while ( tokenizer.hasMoreTokens() )
         url = url.concat( tokenizer.nextToken() );
      url += ")";

      ImportDialog dialog = new ImportDialog( null, this );
      dialog.setLocationRelativeTo( null );
      dialog.setVisible( true );
   }

   @Override
   public String getVendorURL()
   {
      return URL;
   }

   @Override
   public String getVendor()
   {
      return VENDOR;
   }

   @Override
   public Image getIconImage()
   {
      return image;
   }
}
