FROM payara/server-full:5.2022.5

# ... your existing steps, including copying CAT.war to ROOT.war ...
COPY target/CAT.war $PAYARA_PATH/glassfish/domains/domain1/autodeploy/ROOT.war

# Add entrypoint wrapper
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["startInForeground"]