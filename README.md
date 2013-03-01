"Download as ZIP" Share action
================================

This extension allows you to add "Download as ZIP" action in Alfresco Share Document Library web tier (available in both Document Library site and repository).  
Works with: 
- Alfresco Community 3.4.x
- Alfresco Community 4.0.x
- Alfresco Enterprise 3.4.x
- Alfresco Enterprise 4.0.x
- Alfresco Enterprise 4.1.x


Building the module
-------------------
Check out the project if you have not already done so 

        git clone git://github.com/atolcd/alfresco-zip-and-download.git

An Ant build script is provided to build AMP files **OR** JAR files containing the custom files.  
Before building, ensure you have edited the `build.properties` file to set the path to your Alfresco SDK.  

To build AMP files, run the following command from the base project directory:

        ant dist-amp

If you want to build JAR files, run the following command:

        ant dist-jar


Installing the module
---------------------
This extension is a standard Alfresco Module, so experienced users can skip these steps and proceed as usual.

### 1st method: Installing AMPs (recommended)
1. Stop Alfresco
2. Use the Alfresco [Module Management Tool](http://wiki.alfresco.com/wiki/Module_Management_Tool) to install the modules in your Alfresco and Share WAR files:

        java -jar alfresco-mmt.jar install zip-contents-alfresco-module-vX.X.X.amp $TOMCAT_HOME/webapps/alfresco.war -force
        java -jar alfresco-mmt.jar install zip-contents-share-module-vX.X.X.amp $TOMCAT_HOME/webapps/share.war -force

3. Delete the `$TOMCAT_HOME/webapps/alfresco/` and `$TOMCAT_HOME/webapps/share/` folders.  
**Caution:** please ensure you do not have unsaved custom files in the webapp folders before deleting.
4. Start Alfresco


### 2nd method: Installing JARs
1. Stop Alfresco
2. Copy JAR files
    - Copy `share-zip-contents-action.jar` into the `/tomcat/shared/lib/` folder of your Alfresco.
    - Copy `zip-contents-alfresco-webscript.jar` into the `/tomcat/webapps/alfresco/WEB-INF/lib/` folder of your Alfresco.
3. Start Alfresco


Using the module
---------------------

"Compress and download" action will now be available in the folder's actions list.  

**This extension will be a [native feature](http://blogs.alfresco.com/wp/kevinr/2012/09/20/alfresco-community-4-2/) in Alfresco 4.2 and above versions.**


LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.  
Created by: [Julien BERTHOUX] (https://github.com/jberthoux) and [Bertrand FOREST] (https://github.com/bforest)  


Our company
---------------------
[Atol Conseils et Développements] (http://www.atolcd.com) is Alfresco [Gold Partner] (http://www.alfresco.com/partners/atol)  
Follow us on twitter [ @atolcd] (https://twitter.com/atolcd)  