#!/usr/bin/zsh


#########################################
# version settings
export BASE_URL=$1


export VERSION6_URL=${BASE_URL}"/6.5.x/6.5.0/BonitaBPMCommunity-6.5.0/BonitaBPMCommunity-6.5.0-Tomcat-7.0.55.zip"
export VERSION6="6.5.0"

export VERSION7_URL=${BASE_URL}"/7.1.x/7.1.0/BonitaBPMCommunity-7.1.0/BonitaBPMCommunity-7.1.0-Tomcat-7.0.55.zip"
export VERSION7="7.1.0"


export MIGRATION1="1.23.1"
export MIGRATION1_URL=${BASE_URL}"/migration_tools/1.x%20(up_to_7.0.0)/1.23.1/bonita-migration-distrib-${MIGRATION1}.zip"

export MIGRATION2="2.3.0"
export MIGRATION2_URL=${BASE_URL}"/migration_tools/2.x/2.3.0/bonita-migration-distrib-${MIGRATION2}.zip"
#########################################


echo "setup working directories"

export WORK_DIR=$(pwd)"/migration"
export TEMP_DIR=${WORK_DIR}"/temp"
export VERSION6_DIR=${TEMP_DIR}/BonitaBPMCommunity-${VERSION6}-Tomcat-7.0.55
export VERSION7_DIR=${TEMP_DIR}/BonitaBPMCommunity-${VERSION7}-Tomcat-7.0.55
export MIGRATION1_DIR=${TEMP_DIR}/migration-${MIGRATION1}
export MIGRATION2_DIR=${TEMP_DIR}/bonita-migration-distrib-${MIGRATION2}

# create directories
rm -rf ${TEMP_DIR}
mkdir -p ${VERSION6_DIR}
mkdir -p ${VERSION7_DIR}
mkdir -p ${MIGRATION1_DIR}

echo "#########################################"
echo "download artifacts"

wget --quiet ${VERSION6_URL} -O ${WORK_DIR}"/BonitaBPMCommunity-${VERSION6}-Tomcat-7.0.55.zip"
wget --quiet ${VERSION7_URL} -O ${WORK_DIR}"/BonitaBPMCommunity-${VERSION7}-Tomcat-7.0.55.zip"
wget --quiet ${MIGRATION1_URL} -O ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION1}.zip"
wget --quiet ${MIGRATION2_URL} -O ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION2}.zip"

unzip -q ${WORK_DIR}"/BonitaBPMCommunity-${VERSION6}-Tomcat-7.0.55.zip" -d ${TEMP_DIR}
unzip -q ${WORK_DIR}"/BonitaBPMCommunity-${VERSION7}-Tomcat-7.0.55.zip" -d ${TEMP_DIR}
unzip -q ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION1}.zip" -d ${MIGRATION1_DIR}
unzip -q ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION2}.zip" -d ${TEMP_DIR}

echo "#########################################"
echo "unzipped artifacts:"
tree -L 2 ${TEMP_DIR}

# add dbvendor driver
cp -b -f drivers/*.jar ${MIGRATION1_DIR}/lib
cp -b -f drivers/*.jar ${MIGRATION2_DIR}/lib
cp -b -f drivers/*.jar ${VERSION6_DIR}/lib/bonita
cp -b -f drivers/*.jar ${VERSION7_DIR}/lib/bonita

#v6: no BDM datasources
echo "#########################################"
echo "setup dbvendor"
cp -b -f postgres/bos/v6/setenv.sh ${VERSION6_DIR}/bin/setenv.sh
cp -b -f postgres/bos/v6/bitronix-resources.properties ${VERSION6_DIR}/conf/bitronix-resources.properties
cp -b -f postgres/bos/v6/bonita.xml ${VERSION6_DIR}/conf/Catalina/localhost/bonita.xml

#v7: with BDM datasources
cp -b -f postgres/bos/v7/setenv.sh ${VERSION7_DIR}/bin/setenv.sh
cp -b -f postgres/bos/v7/bitronix-resources.properties ${VERSION7_DIR}/conf/bitronix-resources.properties
cp -b -f postgres/bos/v7/bonita.xml ${VERSION7_DIR}/conf/Catalina/localhost/bonita.xml

echo "#########################################"
echo "clean database"
# TODO: make if fail if unable to clean the DB or kill sessions
gradle -b ../build.gradle cleanDb

# run v6
echo "#########################################"
echo "start engine v"${VERSION6}
${VERSION6_DIR}/bin/startup.sh


echo "#########################################"
echo "wait until login.jsp is reponding"
until [ "`curl --silent --show-error --connect-timeout 1 -I http://localhost:8080/bonita/login.jsp | grep '200 OK'`" != "" ];
do
  sleep 5
done

# TODO fill engine with organization/processes/etc.

echo "#########################################"
echo "stop engine v"${VERSION6}
${VERSION6_DIR}/bin/shutdown.sh


echo "#########################################"
echo "run migration v"${MIGRATION1}
echo "folder "${MIGRATION1_DIR}

echo "bonita.home=${VERSION6_DIR}/bonita" > ${MIGRATION1_DIR}/Config.properties
echo "db.vendor=postgres" >> ${MIGRATION1_DIR}/Config.properties
echo "db.url=jdbc:postgresql://localhost:5432/migration" >> ${MIGRATION1_DIR}/Config.properties
echo "db.driverClass=org.postgresql.Driver" >> ${MIGRATION1_DIR}/Config.properties
echo "db.user=bonita" >> ${MIGRATION1_DIR}/Config.properties
echo "db.password=bpm" >> ${MIGRATION1_DIR}/Config.properties

echo "migration settings:"
cat ${MIGRATION1_DIR}/Config.properties
echo "#########################################"


cd ${MIGRATION1_DIR}
./migration.sh

echo "#########################################"
echo "run migration v"${MIGRATION2}
echo "folder "${MIGRATION2_DIR}

echo "bonita.home=${VERSION6_DIR}/bonita" > ${MIGRATION2_DIR}/Config.properties
echo "db.vendor=postgres" >> ${MIGRATION2_DIR}/Config.properties
echo "db.url=jdbc:postgresql://localhost:5432/migration" >> ${MIGRATION2_DIR}/Config.properties
echo "db.driverClass=org.postgresql.Driver" >> ${MIGRATION2_DIR}/Config.properties
echo "db.user=bonita" >> ${MIGRATION2_DIR}/Config.properties
echo "db.password=bpm" >> ${MIGRATION2_DIR}/Config.properties

echo "migration settings:"
cat ${MIGRATION2_DIR}/Config.properties
echo "#########################################"

cd ${MIGRATION2_DIR}/bin

# not working yet
#./bonita-migration-distrib -Dtarget.version=7.1.1 -Dauto.accept=true
./bonita-migration-distrib

echo "#########################################"
echo "copy migrated bonita home to new bundle"
mv  ${VERSION7_DIR}/bonita  ${VERSION7_DIR}/bonita.orig
cp -rf ${VERSION6_DIR}/bonita ${VERSION7_DIR}/bonita

echo "#########################################"
echo "start migrated platform in v"${VERSION7}
${VERSION7_DIR}/bin/startup.sh


echo "#########################################"
echo "wait until login.jsp is reponding"
until [ "`curl --silent --show-error --connect-timeout 1 -I http://localhost:8080/bonita/login.jsp | grep '200 OK'`" != "" ];
do
  sleep 5
done

echo "#########################################"
echo "stop engine v"${VERSION7}
${VERSION7_DIR}/bin/shutdown.sh
