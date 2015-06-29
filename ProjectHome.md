## Overview ##

[Moneydance](http://moneydance.com/) plug-in to import transactions from CSV files. You can define, simply, on a screen, any number of 'Custom Readers' yourself, one for each Account file. You define the order and fields that will be imported into MD, like Date, Amount, Description, Category, etc... You can ignore fields, and specify that certain fields "Can Be Blank". You can test if a file parses properly, and have it find and list files to import that are tied by file name to a Custom Reader. You can do Regex parsing of csv files for tricky situations. And importantly, it will not import transactions that you previously imported, by doing matching.
There is a How To doc you can download with an example of how to define your own custom readers.

  * Latest version: only from here - version right in Money Dance is: 15.7.7 at this time, so if you want the latest, get it from here.
Remove old version. Restart. Install via 'Add From File'.

The plug-in is still considered BETA, however, it works well for people who have tried it.

It is distributed under GNU LGPL. Among other things this means that it is free, but that the authors cannot take any responsibility for you using this code.

## Downloads are Here Now - google quit allowing new download files ##

The new home for mdcsvimporter is at github. This will eventually go away.

== For Moneydance 2015:

mdcsvimporter-v19 is available. You can get it here:

[mdcsvimporter2015](https://github.com/stant/mdcsvimporter2015/releases)


Thanks,
Stan Towianski

== For Moneydance 2014 and Below:

[mdcsvimporter-beta-18.zip](http://retonews.com/downloads/mdcsvimporter-beta-18.zip)  ([download count](http://retonews.com/downloads/mdcsvimporter-beta-18-count.html)) - Dec/31/2014 Fix -  Regex parser was not working correctly. It was always using my hardcoded test regex strings :-( Someone tried to use it so I found out. Thanks.

[mdcsvimporter-beta-17.zip](http://retonews.com/downloads/mdcsvimporter-beta-17.zip)  ([download count](http://retonews.com/downloads/mdcsvimporter-beta-17-count.html)) - Nov/23/2014 New Enhancements! -  Added:
  * filename matcher (regex) for each reader to find files (So now, if you pick a Reader first instead of a file, like say DiscoverCard, then hit 'Find import files for this reader', it will give you a list of files in the drop down that match DiscoverCard filenames like DFS-...csv),
  * handle MMM (Dec) date (though only locale.english for now),
  * Esc(ape) to close windows,
  * mark field that parsed in error in red.

[mdcsvimporter-beta-16.zip with instructions](http://retonews.com/downloads/mdcsvimporter-beta-16.zip)  ([download count](http://retonews.com/downloads/mdcsvimporter-count.html)) - Aug/2/2014 New Enhancements! - Added: Preview window, Add Regex reader, Remove hard-coded internal readers, Turn off automatic testing of readers, Let users test, Fix import to acct bug -

[Instructions for beta-16](http://retonews.com/downloads/How-to-use-mdcsvimporter-beta-16.zip)

## Installation ##

  * This plug-in has been added to the default list of plug-ins right in Moneydance and can be installed right from there.
  * You can go to _Extensions_ menu, and choose _Manage Extentions_, and choose _CSV Importer_ and Install.
  * or
  * Download the latest plugin from the _[Downloads](http://code.google.com/p/mdcsvimporter/downloads/list)_ tab above.
  * Unzip to any folder if you get a zip file.  Otherwise rename a straight mxt file to plain mdcsvimporter.mxt to use.
  * Open Moneydance, open _Extensions_ menu, and choose _Add From File...
  * Select the [.mxt](http://code.google.com/p/mdcsvimporter/wiki/mxt) file you just unzipped and click_Next_.
  * Click_Yes_to allow the plug-in to be loaded.
  * Inspect the plug-in information and click_Finish_._

## Usage ##

  * Download your monthly bills. Make sure you select [CSV file format](http://en.wikipedia.org/wiki/Comma-separated_values).
  * Open Moneydance, open _Extensions_ and select _Import File_.
  * In the dialog that opens, select the .csv file you downloaded.
  * Select account that the transactions should be added to.
  * Optionally select "[Securely erase file after processing](http://code.google.com/p/mdcsvimporter/wiki/securely_erase_file)" if you wish.
  * Click _Process_.
  * If all goes well, dialog will close. Your imported transactions will appear at the bottom of the account view as _You have un-recorded downloaded transactions_.

## Comments and Help ##

To report a bug please use the issues [link](http://code.google.com/p/mdcsvimporter/issues/list) above.

All questions and development discussion should be in the [discussion group](http://groups.google.com/group/mdcsvimporter).

## Moneydance Support ##

Since BETA12, the plug-in should fully support Moneydance 2010 and 2011.
January 2013 I successfully imported files into Moneydance 2012.3

## BETA15 ##

Added more funtionality.  Will list when I have time.

## BETA14 ##

September 2, 2011

ADD: New Custom Readers. This lets users manage multiple reader settings for their own banks. Create configurations for say: Discover card, VISA, private bank, etc... You denote data columns that can be -Payment-, -Deposit-, date, amount, etc... When you choose a file, it will fill out a drop down list of all the readers that match your csv file format by testing your data. You can then select one to use to import your data by.

Stan Towianski

## BETA13 ##

BETA13 has been released on June 23, 2011. There are no functional changes in the plugin. Only plugin description has been shortened, as a workaround to MoneyDance dialog resizing issue, which meant plugins with long descriptions could not be installed. For detailed changes see [release notes](http://code.google.com/p/mdcsvimporter/wiki/ReleaseNotes).