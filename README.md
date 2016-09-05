#Gift Card Bot

REST service example: https://spring.io/guides/gs/actuator-service/

Run app from maven: mvn spring-boot:run

# Dev notes

Detailed reading on spring-boot: https://www.infoq.com/articles/microframeworks1-spring-boot
It describes micro services. Including access to database.

Quickly create pom files for spring boot apps: http://start.spring.io/

Use jsonschema2pojo maven plugin to generate java classes form json shema.
It frees you form manualy coding req / resp classes.

http://www.jsonschema2pojo.org

# Gyft account

Application muros-gift-app  
Key: y3b723chj4jgrhq55bx74bhu  
Secret: enFWHJ4E6h   
Status: active  
Created: 4 seconds ago  

Key Rate Limits  
10	Calls per second  
50,000	Calls per day  

# Data Base
Start H2 locally with command:  
$cd c:\h2\bin  
$java -cp h2-1.3.176.jar org.h2.tools.Server -baseDir c:\Dev\gyft\DB

V resouces diru moraš imeti data.sql, ki ga izvede ob vsakem zagonu aplikacije.
V data.sql pa imaš:  

insert into user(username, first_name, last_name, created_date) values ('danveloper', 'Dan', 'Woods', now());  
insert into user(username, first_name, last_name, created_date) values ('muros', 'Uroš', 'Mesarič', now());

## H2 install on Linux
$wget http://www.h2database.com/h2-2014-04-05.zip  
$unzip h2-2014-04-05.zip
$mkdir data

Run database using script:  
$cd h2/bin    
$java -cp h2-1.3.176.jar org.h2.tools.Server -baseDir /home/ec2-user/dev/gyft/data

# EC2
AWS Machine running demo giftbot  
$ssh -i "C:\Users\muros.HSL\Documents\AWS\ec2-key-pair-virginia.pem" ec2-user@52.203.56.236

## Install node.js and npm
$sudo yum update  
$sudo yum install gcc-c++ make  
$sudo yum install openssl-devel  
$sudo yum install git  
$git clone git://github.com/nodejs/node.git  
$cd node  
$git checkout v0.6.8  
$./configure
$make
$sudo make install

V /etc/sudoers pod /secure_path dodaj /usr/local/bin

Še NPM:  
$git clone https://github.com/isaacs/npm.git
$cd npm
$sudo make install  

# Stripe payment
Kreiral account na Stripe na službeni mail in LastPass passwd  
Link na Strip dashboard: 
https://dashboard.stripe.com/test/dashboard

# Fb Messenger API
Git clone example code:

$git clone https://github.com/fbsamples/messenger-platform-samples.git  

# Apache on EC2

sudo yum update -y  
sudo yum install -y httpd24  
sudo service httpd start  
sudo chkconfig httpd on  
chkconfig --list httpd  

AWS navodila:  
https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/install-LAMP.html  

# Letsencrypt certs

https://github.com/Daplie/letsencrypt-cli  
Check SSL on server:  
https://www.ssllabs.com  


# TODO
* Add payment info to transaction table
* Transaction update field names
* Add metadata to payment on Stripe

C:\Dev\gyft\target>scp -i "C:\Users\muros.HSL\Documents\AWS\ec2-key-pair-virgini
a.pem" gift-card-bot-rest-0.1.0.jar ec2-user@52.203.56.236:dev/gyft