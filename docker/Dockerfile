FROM payara/server-full:5.2022.5

# Payara слуша на 8080 по подразбиране
EXPOSE 8080

# Копираме готовия WAR от build-a
# Ако файлът ти се казва ROOT.war - смени името
COPY CAT.war /opt/payara/appserver/glassfish/domains/domain1/autodeploy/CAT.war

# Стартираме domain1 във foreground
CMD ["asadmin", "start-domain", "-v"]