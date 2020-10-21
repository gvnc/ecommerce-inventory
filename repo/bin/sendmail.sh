#!/bin/bash

toRecipients="guvenc.kazanci@gmail.com"
ccRecipients="guvenckazanci@yahoo.com"

function sendMail(){

    subject="alarm received from defconsync.com"

    (
      echo To: $toRecipients
      echo Cc: $ccRecipients
      echo "Content-Type: text/html; "
      echo Subject: $subject
      exec /home/defconsync/ecommerce/bin/email.template 
    ) | /usr/sbin/sendmail -t

    echo "Mail sent"

}

sendMail

