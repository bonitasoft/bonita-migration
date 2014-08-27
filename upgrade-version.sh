#!/bin/zsh
#BONITA_PREVIOUS_VERSION=6.3.3
#BONITA_CURRENT_VERSION=6.3.4
#BONITA_NEXT_VERSION=6.3.5

VERSIONS_FOLDER_NAME="bonita-migration-distrib/src/main/resources/versions"
MIGRATION_DB_FILLER_PREFIX="bonita-migration-db-filler-"

usage()
{
cat << EOF
usage: $0 options

OPTIONS:
   -h                                                   Show this message
   -p<previous_version>, --previous=<previous_version>  previous bonita version 
   -c<current_version>, --current=<current_version>     current bonita version 
   -n<next_version>, --next=<next_version>              next bonita version 
   -m<migration_tag>, --migration=<migration_tag>       last migration tag
Example:
./upgrade-version.sh --previous=6.3.4 --current=6.3.3 --next=6.3.5 -m1.10.0

Warning: If current version or previous version are not provided, they will be 
         detected with the content of the $VERSIONS_FOLDER_NAME folder

EOF
}

findCurrentAndPreviousVersion(){
    VERSIONS_FOLDER=($(ls $VERSIONS_FOLDER_NAME | tr -d ' '))
    LAST_VERSION_FOLDER=${VERSIONS_FOLDER[${VERSIONS_FOLDER[#]}-1]}
    echo "cut -d '-' <<< $LAST_VERSION_FOLDER"
    echo "${BONITA_PREVIOUS_VERSION:=$(cut -d '-' -f 1 <<< $LAST_VERSION_FOLDER)}"
    echo "${BONITA_CURRENT_VERSION:=$(cut -d '-' -f 2 <<< $LAST_VERSION_FOLDER)}"
}

updatePomVersion(){
    sed -e 's:<previous.bonita.version>.*</previous.bonita.version>:<previous.bonita.version>'"$BONITA_CURRENT_VERSION"'-SNAPSHOT</previous.bonita.version>:g' pom.xml > tmp.out
    sed -e 's:<current.bonita.version>.*</current.bonita.version>:<current.bonita.version>'"$BONITA_NEXT_VERSION"'-SNAPSHOT</current.bonita.version>:g' tmp.out > tmp2.out
    sed -e 's:<next.bonita.version>.*</next.bonita.version>:<next.bonita.version>'"$BONITA_NEXT_VERSION"'</next.bonita.version>:g' tmp2.out > tmp3.xml
    rm -f tmp.out
    rm -f tmp2.out
}

createNewMigrationFolder(){
    cd bonita-migration-versions-updated
    echo "mvn clean install -Pupdate -Dlast.version.to.migrate=$BONITA_PREVIOUS_VERSION -Dlast.bonita.version=$BONITA_CURRENT_VERSION -Dnext.bonita.version=$BONITA_NEXT_VERSION -Dlast.migration.tag=$LAST_MIGRATION_TAG"
    cd ..
}

createNewDBFiller(){
    cd bonita-migration-versions-updated
    CURRENT_MIGRATION_VERSION=$(grep '<version>.*-SNAPSHOT<' pom.xml | sed -r 's:</?version>::g' | tr -d ' ')
    echo "mvn archetype:generate -DarchetypeArtifactId=bonita-migration-db-filler-archetype -DarchetypeGroupId=org.bonitasoft.migration -DarchetypeVersion=$CURRENT_MIGRATION_VERSION -Dbonita-version=$BONITA_NEXT_VERSION -Ddb-filler-suffix=${BONITA_NEXT_VERSION//./_}"
    cd ..
}

updateGAVersionInCurrentDistrib(){
    sed -e 's:<bonita.version>.*</bonita.version>:<bonita.version>'"$BONITA_CURRENT_VERSION"'</bonita.version>:g' $MIGRATION_DB_FILLER_PREFIX$BONITA_CURRENT_VERSION/pom.xml > $MIGRATION_DB_FILLER_PREFIX$BONITA_CURRENT_VERSION/tmp.out
    sed -e 's:<bonita.version>.*</bonita.version>:<bonita.version>'"$BONITA_NEXT_VERSION"'-SNAPSHOT</bonita.version>:g' $MIGRATION_DB_FILLER_PREFIX$BONITA_NEXT_VERSION/pom.xml > $MIGRATION_DB_FILLER_PREFIX$BONITA_NEXT_VERSION/tmp.out
}

OPTS=$(getopt -o p:n:c:m:h -l previous:,current:,next:,migration: -- "$@")

if [ $? != 0 ]
then
    exit 1
fi

eval set -- "$OPTS"


while true ; do
    case "$1" in
        -p) BONITA_PREVIOUS_VERSION=$2; shift;;
        -c) BONITA_CURRENT_VERSION=$2; shift;;
        -n) BONITA_NEXT_VERSION=$2; shift;;
        -m) LAST_MIGRATION_TAG=$2; shift;;
        
        -d) checkDestination; DEBUG=true
           if [[ "$2" != "Community" ]]; then
              DESTINATION=$HOME_WEB_SP$DEBUG_DESTINATION_SUFFIX
           else
              DESTINATION=$HOME_WEB$DEBUG_DESTINATION_SUFFIX
           fi; 
           shift;;
        -h) usage ; shift;;
        --previous) BONITA_PREVIOUS_VERSION=$2; shift 2;;
        --current) BONITA_CURRENT_VERSION=$2; shift 2;;
        --next) BONITA_NEXT_VERSION=$2; shift 2;;
        --migration) LAST_MIGRATION_TAG=$2; shift 2;;
        --) shift; break;;
        *) shift;;
    esac
done
findCurrentAndPreviousVersion
echo $BONITA_PREVIOUS_VERSION
echo $BONITA_CURRENT_VERSION
echo "$BONITA_NEXT_VERSION ${BONITA_NEXT_VERSION//./_}"
if [[ -z $BONITA_PREVIOUS_VERSION || -z $BONITA_CURRENT_VERSION || -z $BONITA_NEXT_VERSION || -z $LAST_MIGRATION_TAG ]]; then
  usage
  exit
fi
echo "Great!"
updatePomVersion
createNewMigrationFolder
createNewDBFiller
updateGAVersionInCurrentDistrib