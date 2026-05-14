#!/bin/sh
chown -R payara:payara /opt/payara/appserver/glassfish/domains/domain1 || true
chmod -R u+rwX /opt/payara/appserver/glassfish/domains/domain1 || true

exec "$@"