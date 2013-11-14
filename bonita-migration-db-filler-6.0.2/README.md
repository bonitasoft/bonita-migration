Database Filler 6.0.2
=====================

Usage
-----
run the DatabaseFiller main methode and set bonita.home system variable to use

Keep resulting database/bonita.home
-----------------------------------

copy the database (on mysql):

>    mysqldump -uroot -proot bonita > database.sql

and copy the bonita home

>    cp -r /tmp/home .

use dumped database again

>    mysql -uroot -proot --database bonita < database.sql