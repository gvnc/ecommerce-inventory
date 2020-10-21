#!/bin/bash

APP_HOME=/home/defconsync/ecommerce

logfile=$APP_HOME/log/monitoring.log

echo "----------------------------------------" >> $logfile
echo ""
date >> $logfile
free -m >> $logfile
echo "" >> $logfile
 
