# Setting Up AWS Elastic Beanstalk for Java Project

> Source: https://edwardsamuel.wordpress.com/2015/07/17/enable-https-and-http-redirect-on-aws-elastic-beanstalk/

**AWS Elastic Beanstalk** is powerful deployment tools on AWS. It allows users to create applications and push them to a definable set of AWS services, including Amazon EC2, Amazon RDS, Amazon Simple Notification Service (SNS), Amazon CloudWatch, Auto Scaling Group, and Elastic Load Balancer (ELB). The problem is: Elastic Beanstalk Web Console is not so powerful. But, it can be extended using script (`.ebextensions`) and CLI.

## Problem: HTTP and HTTPS Configuration on Elastic Beanstalk (Web Console)

![AWS Elastic Beanstalk Configuration](https://i1.wp.com/docs.aws.amazon.com/elasticbeanstalk/latest/dg/images/aeb-config-elb-loadbalancer.png "AWS Elastic Beanstalk Configuration: Load Balancer")

From the Elastic Beanstalk Web Console, you can configure a web application to listen HTTP and HTTPS port using Elastic Load Balancer (ELB). But, the ELB will forward/proxy the request into a single HTTP port. It means that HTTP and HTTPS will serve same response from user point-of-view. HTTPS connection is terminated (HTTPS-termination) in ELB.

Some users (at least me and some users in stackoverflow.com/serverfault.com) want:

* HTTP request is replied with HTTP redirection (3xx status code) to HTTPS.
* HTTPS request is replied by actual web app.
* No HTTPS-termination in ELB.

Solution: Configure Elastic Load Balancer using .ebextentions

Some of Elastic Beanstalk resources can be customized using .ebextenstions script (see: Customize Containers and Environment Resources). Now, we will configure the ELB to proxy HTTP and HTTPS request to different EC2 instance’s ports.

1. Create `.ebextensions` directory inside your app root path.
1. Create a file (e.g: `00-load-balancer.config`) inside `.ebextensions` directory.
1. Write the following configuration into the file (`.ebextensions/00-load-balancer.config`).

    ```javascript 
    {
      "Resources": {
        "AWSEBSecurityGroup": {
          "Type": "AWS::EC2::SecurityGroup",
          "Properties": {
            "GroupDescription": "Allow HTTP and HTTPS",
            "SecurityGroupIngress": [
              {
                "IpProtocol": "tcp",
                "FromPort": 80,
                "ToPort": 80,
                "CidrIp": "0.0.0.0/0"
              },
              {
                "IpProtocol": "tcp",
                "FromPort": 443,
                "ToPort": 443,
                "CidrIp": "0.0.0.0/0"
              }
            ]
          }
        },
        "AWSEBLoadBalancerSecurityGroup": {
          "Type": "AWS::EC2::SecurityGroup",
          "Properties": {
            "GroupDescription": "Allow HTTP and HTTPS",
            "SecurityGroupIngress": [
              {
                "IpProtocol": "tcp",
                "FromPort": 80,
                "ToPort": 80,
                "CidrIp": "0.0.0.0/0"
              },
              {
                "IpProtocol": "tcp",
                "FromPort": 443,
                "ToPort": 443,
                "CidrIp": "0.0.0.0/0"
              }
            ],
            "SecurityGroupEgress": [
              {
                "IpProtocol": "tcp",
                "FromPort": 80,
                "ToPort": 80,
                "CidrIp": "0.0.0.0/0"
              },
              {
                "IpProtocol": "tcp",
                "FromPort": 443,
                "ToPort": 443,
                "CidrIp": "0.0.0.0/0"
              }
            ]
          }
        },
        "AWSEBLoadBalancer": {
          "Type": "AWS::ElasticLoadBalancing::LoadBalancer",
          "Properties": {
            "HealthCheck": {
              "HealthyThreshold": "3",
              "Interval": "30",
              "Target": "HTTP:80/status.html",
              "Timeout": "5",
              "UnhealthyThreshold": "5"
            },
            "Listeners": [
              {
                "LoadBalancerPort": 80,
                "Protocol": "HTTP",
                "InstancePort": 80,
                "InstanceProtocol": "HTTP"
              },
              {
                "LoadBalancerPort": 443,
                "Protocol": "HTTPS",
                "InstancePort": 443,
                "InstanceProtocol": "HTTPS",
                "SSLCertificateId": "arn:aws:iam::123456789012:server-certificate/YourSSLCertificate"
              }
            ],
            "SecurityGroups": [
              { "Fn::GetAtt": [ "AWSEBLoadBalancerSecurityGroup", "GroupId" ] }
            ]
          }
        }
      }
    }
    ```

    In the above config, we modified 3 resources:

    * EC2 Instance Security Group, allow to listen on port HTTP (80) and HTTPS (443).
    * ELB Security Group, allow to listen on port HTTP (80) and HTTPS (443).
    * ELB, we modified ELB to:
        * Do health check EC2 instances on port 80 by HTTP request to /status.html. So, we need to create a hole in port HTTP to allow access the page (will be described later). Elastic Beanstalk doesn’t allow us to do health check using HTTPS request. If you want to do health check by checking TCP port 80, just remove this config section.
        * Make ELB listen to port 80 and forward it to EC2 instance’s port 80.
        * Make ELB listen to port 443 and forward it to EC2 instance’s port 443.

    Now the ELB configuration is ready. But, we need to configure web server inside EC2 instances.

Elastic Beanstalk provides some different type of environment (e.g: Java, Python, Ruby, Docker, etc.). Each environment might have different configuration. You can check it on Supported Platforms. At the time of writing this post, they use some web proxy/server (i.e. Apache 2.2, Apache 2.4, Nginx 1.6.2 and IIS 8.5) to listen at port 80 (HTTP).

In this post, I only tell you how to configure Single Docker Container Elastic Beanstalk, which is using Nginx 1.6.2 as proxy. Basically, Single Docker Container Elastic Beanstalk use Nginx to proxy the request to a Docker container. Each time you deploy a new update, Elastic Beanstalk agent inside EC2 instance will update Docker upstream in /etc/nginx/conf.d/elasticbeanstalk-nginx-docker-upstream.conf. Another environment can be configured slightly same.

1. Create a file (e.g: 01-nginx-proxy.config) inside .ebextensions directory.
1. Write the following configuration into the file (.ebextensions/01-nginx-proxy.config). Don’t forget to adjust some config (e.g: domain name, SSL certificate, etc.).

    ```yaml  
    files:
      "/etc/nginx/sites-available/000-default.conf":
        mode: "000644"
        owner: root
        group: root
        content: |
          map $http_upgrade $connection_upgrade {
            default   "upgrade";
            ""        "";
          }
      
          server {
            listen         80;
            server_name    your-domain.com;
      
            location = /status.html {
              proxy_pass          http://docker;
              proxy_http_version  1.1;
      
              proxy_set_header    Connection          $connection_upgrade;
              proxy_set_header    Upgrade             $http_upgrade;
              proxy_set_header    Host                $host;
              proxy_set_header    X-Real-IP           $remote_addr;
              proxy_set_header    X-Forwarded-For     $proxy_add_x_forwarded_for;
              proxy_set_header    X-Forwarded-Host    $host;
              proxy_set_header    X-Forwarded-Server  $host;
            }
      
            location / {
              return        301 https://$host$request_uri;
            }
          }
      
          server {
            listen 443;
      
            ssl                  on;
            ssl_session_timeout  5m;
            ssl_protocols        TLSv1 TLSv1.1 TLSv1.2;
            ssl_certificate      /opt/ssl/default-ssl.crt;
            ssl_certificate_key  /opt/ssl/default-ssl.pem;
            ssl_session_cache    shared:SSL:10m;
      
            location / {
              proxy_pass          http://docker;
              proxy_http_version  1.1;
      
              proxy_set_header    Connection          $connection_upgrade;
              proxy_set_header    Upgrade             $http_upgrade;
              proxy_set_header    Host                $host;
              proxy_set_header    X-Real-IP           $remote_addr;
              proxy_set_header    X-Forwarded-For     $proxy_add_x_forwarded_for;
              proxy_set_header    X-Forwarded-Host    $host;
              proxy_set_header    X-Forwarded-Server  $host;
            }
          }
      
      "/opt/ssl/default-ssl.crt":
        mode: "000400"
        owner: root
        group: root
        content: |
          -----BEGIN CERTIFICATE-----
          *
          * YOUR-CHAINED-SSL-CERTIFICATE-HERE
          *
          -----END CERTIFICATE-----
      
      
      "/opt/ssl/default-ssl.pem":
        mode: "000400"
        owner: root
        group: root
        content: |
          -----BEGIN RSA PRIVATE KEY-----
          *
          * YOUR-SSL-PRIVATE-KEY-HERE
          *
          -----END RSA PRIVATE KEY-----
      
    commands:
       00_enable_site:
        command: 'rm -f /etc/nginx/sites-enabled/* && ln -s /etc/nginx/sites-available/000-default.conf /etc/nginx/sites-enabled/000-default.conf'
    ```

    In the above config, we:

    * Create SSL certificate and key file.
    * Create Nginx site config:
        * Listen port 80 (HTTP) and redirect all request to HTTPS, except for /status.html. We create a hole here to allow load balancer do health check.
        * Listen port 443 (HTTPS) and proxy the request to actual web server (in this case, Docker container upstream, http://docker).
    * Remove all enabled-sites config and create symlink for the new Nginx config.

    After that, you can zip your app directory and deploy it to Elastic Beanstalk via Web Console or CLI.