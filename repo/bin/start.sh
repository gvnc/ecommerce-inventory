#!/bin/bash
APP_PATH=/home/defconsync/ecommerce
nohup java -jar $APP_PATH/app.jar -Duser.timezone=GMT-4 --spring.config.location=file:$APP_PATH/configuration/configuration.properties > /dev/null 2>&1 &

