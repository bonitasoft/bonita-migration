Bonita migration tool
=======================

How to run it
-------------
bin

Configuration
--------------

All configuration are put inside the Config.properties at the root folder of the migration tool

Available system properties
--------------------------
Can be useful for automation

    db.vendor     : the database vendor (mysql, postgres, oracle or sqlserver)
    db.url        : the url of the database
    db.user       : the user to connect to the database
    db.password   : password of the user
    db.driverClass: class of the database driver to user
    bonita.home   : the path of the bonita home to migrate
    target.version: the version in which to migrate