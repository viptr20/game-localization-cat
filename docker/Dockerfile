FROM payara/server-full:5.2022.5

COPY target/CAT.war $PAYARA_PATH/glassfish/domains/domain1/autodeploy/ROOT.war

COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["startInForeground"]