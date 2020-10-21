#!/bin/bash

APP_HOME=/home/defconsync/ecommerce

echo "Killing ecommerce process"

sudo kill -9 $(ps aux | grep java | grep '[e]commerce' | awk '{print $2}')

echo "Process Killed !"
echo "Starting ecommerce app ..."

$APP_HOME/bin/start.sh
