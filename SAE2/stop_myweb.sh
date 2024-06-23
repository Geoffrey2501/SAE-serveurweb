#!/bin/bash
if [ -f /var/run/myweb.pid ]; then
    PID=$(cat /var/run/myweb.pid)
    echo "Stopping my web server..."
    kill $PID
    echo "" > /var/run/myweb.pid
else
    echo "No PID file found. Is the web server running?"
fi