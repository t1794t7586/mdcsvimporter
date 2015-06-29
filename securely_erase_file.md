Many people use a password to secure their Moneydance data files. This provides decent security against prying eyes, both from people who have access to your computer, but even more importantly in case your computer is stolen.

If this is a concern, then it is also prudent to secure all other traces of your finances. Most people do not realise that files they download from their financial institutions contains a lot of sensitive information. Therefore it is advisable to discard these temporary files in a secure way, rather then simply moving them to the trash, as trashed files can easily be recovered.

The effects of checking _Securely erase file after processing_ in the _Import_ dialog are as following:

  * If the import operation fails, selected file will not be touched.
  * If the import operation succeeds, the .csv file will be:
    * completely overwritten three times by value 255, then random values, then 0's
    * deleted from the system

This means that a securely erased file will not contain any usable information even if it can be un-deleted. So you should not select this option if you require the contents of this file after it has been imported.