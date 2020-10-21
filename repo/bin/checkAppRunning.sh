#!/bin/bash

echo "-----------------------------------------------"
echo ""
date
echo "Started to check the app."
 
BIN=/home/defconsync/ecommerce/bin
inProgressFile=$BIN/.checkAppRunningInProgress
previousAlarmFoundFile=$BIN/.previousAlarmFound

inProgress=$(cat $inProgressFile)
previousAlarmFound=$(cat $previousAlarmFoundFile)

if [ $inProgress == "true" ]
then
   echo "Already in progress, exit !"
   exit
fi

echo "true" > $inProgressFile

processInterrupted=false

# check mysqldb and start it again
mysqldProcess=$(ps auxw|grep "mysqld"|grep -v grep|wc -l)

if [ $mysqldProcess == 0 ]
then
   echo "Warning, mysqld is not running !"
   processInterrupted=true
   echo "Running: docker start ecom_mysql"
   docker start ecom_mysql
   echo "Sleep for 90 seconds to wait for mysqld to start"
   sleep 90 
else
   echo "Mysql is running."
fi


# check ecommerce app and start it again
appProcess=$(ps auxw|grep "ecommerce"|grep "java"|grep -v grep|wc -l)

if [ $appProcess == 0 ]
then
   echo "Warning, app is not running !"
   processInterrupted=true
   echo "Running: $BIN/start.sh"
   $BIN/start.sh
   echo "Sleep for 60 seconds to wait for app to start"
   sleep 60
else
   echo "App is running."
fi

if [ $processInterrupted == true ]
then
   if [ $previousAlarmFound == "false" ]
   then
      echo "true" > $previousAlarmFoundFile 
      echo "Sending email to notify people."
      $BIN/sendmail.sh
   else
      echo "An email sent already, not sending again."
   fi
else
   echo "false" > $previousAlarmFoundFile
fi


echo "false" > $inProgressFile

echo "Completed checking the app."
echo ""
