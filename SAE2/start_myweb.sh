#!/bin/bash
echo "Starting my web server..."
nohup java src/ProgramAll.java > /var/log/myweb/error.log 2>&1 &
echo $! > /var/run/myweb.pid