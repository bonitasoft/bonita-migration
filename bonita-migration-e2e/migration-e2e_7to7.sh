#!/usr/bin/zsh


#########################################
# version settings
export TOMCAT_VERSION="Tomcat-7.0.55"

export BASE_URL=$1


export VERSION_FROM=$2
export VERSION_FROM_X=`echo ${VERSION_FROM} | sed -e 's/\.[0-9]$/.x/g'`
export VERSION_FROM_URL=${BASE_URL}"/${VERSION_FROM_X}/${VERSION_FROM}/BonitaBPMCommunity-${VERSION_FROM}/BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}.zip"

export VERSION_TARGET=$3
export VERSION_TARGET_X=`echo ${VERSION_TARGET} | sed -e 's/\.[0-9]$/.x/g'`
export VERSION_TARGET_URL=${BASE_URL}"/${VERSION_TARGET_X}/${VERSION_TARGET}/BonitaBPMCommunity-${VERSION_TARGET}/BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}.zip"

export MIGRATION_VERSION=$4
#export MIGRATION_VERSION_URL=${BASE_URL}"/migration_tools/1.x%20(up_to_7.0.0)/1.23.1/bonita-migration-distrib-${MIGRATION_VERSION}.zip"
export MIGRATION_VERSION_URL=${BASE_URL}"/migration_tools/2.x/${MIGRATION_VERSION}/bonita-migration-distrib-${MIGRATION_VERSION}.zip"

#########################################
# dbvendor settings
export DB_VENDOR="postgres"
export DB_DATABASE="migration"
export DB_URL="jdbc:postgresql://192.168.1.34:5432/${DB_DATABASE}"
export DB_DRIVER="org.postgresql.Driver"
export DB_USER="bonita"
export DB_PASSWORD="bpm"

#########################################


echo "#########################################"
echo " - migration end to end tests (v7) - "
echo "from version          :"${VERSION_FROM}
echo "to version            :"${VERSION_TARGET}
echo "migration tool version:"${MIGRATION_VERSION}
echo "db vendor             :"${DB_VENDOR}
echo "download artifacts from:"${BASE_URL}

echo "#########################################"
echo "setup working directories"

export WORK_DIR=$(pwd)"/migration"
export TEMP_DIR=${WORK_DIR}"/temp"
export VERSION_FROM_DIR=${TEMP_DIR}/BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}
export VERSION_TARGET_DIR=${TEMP_DIR}/BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}
export MIGRATION_VERSION_DIR=${TEMP_DIR}/bonita-migration-distrib-${MIGRATION_VERSION}


# create directories
rm -rf ${TEMP_DIR}
mkdir -p ${VERSION_FROM_DIR}
mkdir -p ${VERSION_TARGET_DIR}
mkdir -p ${MIGRATION_VERSION_DIR}

echo "#########################################"
echo "download artifacts"

if [ ! -f ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}.zip" ]
then
  echo "download BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}.zip"
  wget --quiet ${VERSION_FROM_URL} -O ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}.zip"
fi

if [ ! -f ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}.zip" ]
then
  echo "download BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}.zip"
  wget --quiet ${VERSION_TARGET_URL} -O ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}.zip"
fi

if [ ! -f ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION_VERSION}.zip" ]
then
  echo "download bonita-migration-distrib-${MIGRATION_VERSION}.zip"
  wget --quiet ${MIGRATION_VERSION_URL} -O ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION_VERSION}.zip"
fi


unzip -q ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_FROM}-${TOMCAT_VERSION}.zip" -d ${TEMP_DIR}
unzip -q ${WORK_DIR}"/BonitaBPMCommunity-${VERSION_TARGET}-${TOMCAT_VERSION}.zip" -d ${TEMP_DIR}
unzip -q ${WORK_DIR}"/bonita-migration-distrib-${MIGRATION_VERSION}.zip"  -d ${TEMP_DIR}

echo "#########################################"
echo "unzipped artifacts:"
tree -L 2 ${TEMP_DIR}

# add dbvendor driver
cp -b -f drivers/*.jar ${MIGRATION_VERSION_DIR}/lib
cp -b -f drivers/*.jar ${VERSION_FROM_DIR}/lib/bonita
cp -b -f drivers/*.jar ${VERSION_TARGET_DIR}/lib/bonita

#v7: with BDM datasources
cp -b -f postgres/bos/v7/setenv.sh ${VERSION_FROM_DIR}/bin/setenv.sh
cp -b -f postgres/bos/v7/setenv.sh ${VERSION_TARGET_DIR}/bin/setenv.sh

cp -b -f postgres/bos/v7/bitronix-resources.properties ${VERSION_FROM_DIR}/conf/bitronix-resources.properties
cp -b -f postgres/bos/v7/bitronix-resources.properties ${VERSION_TARGET_DIR}/conf/bitronix-resources.properties

cp -b -f postgres/bos/v7/bonita.xml ${VERSION_FROM_DIR}/conf/Catalina/localhost/bonita.xml
cp -b -f postgres/bos/v7/bonita.xml ${VERSION_TARGET_DIR}/conf/Catalina/localhost/bonita.xml


echo "#########################################"
echo "clean database"
# TODO: make if fail if unable to clean the DB or kill sessions
gradle -b ../build.gradle clean cleanDb -Ddb.vendor=${DB_VENDOR} -Ddb.user=${DB_USER} -Ddb.password=${DB_PASSWORD} -Ddb.url=${DB_URL}

echo "Hit enter to continue..."
read a

echo "#########################################"
echo "start engine v"${VERSION_FROM}
${VERSION_FROM_DIR}/bin/startup.sh

echo "#########################################"
echo "wait until login.jsp is reponding"
until [ "`curl --silent --show-error --connect-timeout 1 -I http://localhost:8080/bonita/login.jsp | grep '200 OK'`" != "" ];
do
  sleep 2
done

# TODO fill engine with organization/processes/etc.

echo "Hit enter to stop engine..."
read a

echo "#########################################"
echo "stop engine v"${VERSION_FROM}
${VERSION_FROM_DIR}/bin/shutdown.sh



echo "#########################################"
echo "run migration v"${MIGRATION_VERSION}
echo "folder "${MIGRATION_VERSION_DIR}

echo "bonita.home=${VERSION_FROM_DIR}/bonita" > ${MIGRATION_VERSION_DIR}/Config.properties
echo "db.vendor=${DB_VENDOR}" >> ${MIGRATION_VERSION_DIR}/Config.properties
echo "db.url=${DB_URL}" >> ${MIGRATION_VERSION_DIR}/Config.properties
echo "db.driverClass=${DB_DRIVER}" >> ${MIGRATION_VERSION_DIR}/Config.properties
echo "db.user=${DB_USER}" >> ${MIGRATION_VERSION_DIR}/Config.properties
echo "db.password=${DB_PASSWORD}" >> ${MIGRATION_VERSION_DIR}/Config.properties
echo "target.version=${VERSION_TARGET}" >> ${MIGRATION_VERSION_DIR}/Config.properties


echo "migration settings:"
cat ${MIGRATION_VERSION_DIR}/Config.properties
echo "#########################################"

echo "Hit enter to run migration..."
read a

cd ${MIGRATION_VERSION_DIR}/bin
# not working yet
#./bonita-migration-distrib -Dtarget.version=7.1.1 -Dauto.accept=true
./bonita-migration-distrib

echo "#########################################"
echo "copy migrated bonita home to new bundle"
mv  ${VERSION_TARGET_DIR}/bonita  ${VERSION_TARGET_DIR}/bonita.orig
cp -rf ${VERSION_FROM_DIR}/bonita ${VERSION_TARGET_DIR}/bonita

echo "#########################################"
echo "start migrated platform in v"${VERSION_TARGET}

echo "Hit enter to start migrated engine..."
read a

${VERSION_TARGET_DIR}/bin/startup.sh


echo "#########################################"
echo "wait until login.jsp is reponding"
until [ "`curl --silent --show-error --connect-timeout 1 -I http://localhost:8080/bonita/login.jsp | grep '200 OK'`" != "" ];
do
  sleep 2
done

echo "Hit enter to stop engine..."
read a

echo "#########################################"
echo "stop engine v"${VERSION_TARGET}
${VERSION_TARGET_DIR}/bin/shutdown.sh

jps -v | grep Tomcat

echo "end."