FROM payara/server-full:5.2022.5-jdk11
USER root
RUN mkdir -p /opt/payara/deployments \
/opt/payara/appserver/glassfish/domains/domain1/osgi-cache \
/opt/payara/appserver/glassfish/domains/domain1/generated \
&& chown -R payara:payara /opt/payara/appserver/glassfish /opt/payara/deployments
COPY dist/*.war /opt/payara/deployments/app.war
USER payara
EXPOSE 8080 4848
