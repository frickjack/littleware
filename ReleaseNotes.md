#notes for the bundled littleware releases

## 2011/04/07 littleware-2.4, catalog-1.3 ##

[littleDist\_2.4\_20110407.zip](http://code.google.com/p/littleware/downloads/)

  * bump up to guice-3.0 and guava-rc08, standardize ee\_client ivy config
  * big refactor - move asset stuff out of littleware build project and into new littleAsset project along with old littleApps project code
  * reset ivy files with bunch of new configs (client, client\_junit, clint\_compile)
  * setup ivy template to use internal local repository to allow multiple build environments under same user
  * split out app, client, and server bootstrap code
  * introduce littleId and idWeb sub-projects - openId support
  * introduce littleScala ivy module