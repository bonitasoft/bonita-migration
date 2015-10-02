#Migration e2e tests

##Purpose

play end to end migration on BOS tomcat bundles

* download v6 and v7 tomcat bundle from qa release site
* download v1 and v2 migration tool
* configure bundles to run on local postgresql DB
* start v6 bundle to setup database
* stop v6 when portal is responding
* migrate to 7.0.0 using migration tool v1
* migrate to v7 using migration tool v2
* configure v7 bundle with migrated bonita home 
* start v7 bundle
* stop v7 bundle when portal is responding

##Run it

```shell
sh migration-e2e-6to7.sh "http://<server release>"
```
