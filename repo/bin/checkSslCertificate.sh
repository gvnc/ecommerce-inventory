#!/bin/bash

APP_HOME=/home/defconsync/ecommerce

echo "Starting to check expiry date"
expiryDate=$(sudo certbot certificates --cert-name defconsync.com | grep Expiry | sed 's/^.*VALID: \(.*\) days.*$/\1/')

echo "Certificate is valid for $expiryDate days."

if [ $expiryDate -lt 2 ]
then
   echo "Renewing certificate !"
   certbot renew
   echo "Regenerating keystore.p12 file"
   openssl pkcs12 -export -in /etc/letsencrypt/live/defconsync.com/fullchain.pem -inkey /etc/letsencrypt/live/defconsync.com/privkey.pem  -out /etc/letsencrypt/live/defconsync.com/keystore.p12 -name tomcat -CAfile /etc/letsencrypt/live/defconsync.com/chain.pem -caname root -password pass:mykeystorepassword
   $APP_HOME/bin/restart.sh
fi
