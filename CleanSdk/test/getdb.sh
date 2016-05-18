#!/bin/bash
localpath=$1
dbdir=databasedir
svnurl="http://smartphone/repositories/DocsAndTools/TechnicMap/settings/管家安全与大数据/清理SDK以及数据分析/数据库"
rm -rf $localpath/$dbdir
cd $localpath
mkdir $dbdir
cd $dbdir
svn checkout $svnurl ./
dirlist=` ls -F |grep "/$" `
dir=""
for d in $dirlist
do
	dir=$d
done
echo "seleted db dir is "$dir
cd $dir
zipfiles=`ls |grep zip`
zipf=""
for z in $zipfiles
do
	zipf=$z
done
echo "seleted db file is "$zipf
unzip $zipf -d ./
mv cleaninfo.db $localpath/assets/cleaninfo.db
mv version.ini $localpath/assets/version.ini
cd $localpath
rm -rf $localpath/$dbdir
