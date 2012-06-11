These files are examples and alternative configuration for those loaded by default.

    - DinapterUnstableProperties.xml  -  Modifications to the default parameters for faster but fewer results.
    - rules.clp                       -  Copy of the rules embeded. These can be loaded using the properties.

To change the properties file to be loaded it must be declared the system property dinapter.properties

Example:
    > java -Ddinapter.properties=etc/DinapterUnstableProperties.xml -jar Dinapter.jar input/e001-ftp_tiny -v
