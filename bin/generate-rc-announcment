#!/usr/bin/env bash

if [ $# -ne 1 ]; then
  echo "Usage: $0 <version>" >&2
  exit 1
fi

bits="http://mirrors.jenkins-ci.org/war-stable-rc/$1/jenkins.war"
test_plan="https://wiki.jenkins-ci.org/display/JENKINS/LTS+${1%?}x+RC+Testing"

if [ $(curl -LkSs -w "%{http_code}" -I  -o /dev/null "$bits") != "200" ]; then
  echo "$bits does not exist" >&2
  exit 1
fi

if [ $(curl -LkSs -w "%{http_code}" -I  -o /dev/null "$test_plan") != "200" ]; then
  echo "$test_plan does not exist" >&2
  exit 1
fi

body="Hello everyone,

Latest LTS RC was made public and it is ready to be tested. Release is
scheduled for $(/usr/bin/date -d 'next Wednesday + 1 week' '+%Y-%m-%d'). <<< !!! CHECK THE DATE !!!

Report your findings in this thread or on the test plan wiki page.

Download bits from $bits
Check community maintained LTS test plan $test_plan

Thanks
"

xdg-email --utf8 --subject "Jenkins $1 LTS RC testing started" --body "${body}" "jenkinsci-dev@googlegroups.com"
