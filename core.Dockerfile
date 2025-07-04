FROM tomcat:latest

WORKDIR /usr/local/tomcat

RUN ./bin/catalina.sh stop \
&& rm -r ./webapps \
&& mv ./webapps.dist ./webapps

COPY resources/core/webapps/docs/META-INF/context.xml ./webapps/docs/META-INF
COPY resources/core/webapps/manager/META-INF/context.xml ./webapps/manager/META-INF
COPY resources/core/conf/tomcat-users.xml ./conf

ENV JPDA_ADDRESS="*:8000"
ENV JPDA_TRANSPORT="dt_socket"
# COPY ./resources/webapps/examples/META-INF/context.xml /usr/local/tomcat/webapps/examples/META-INF
# RUN grep -q "<!-- <Valve" ./webapps/examples/META-INF
# COPY ./resources/webapps/host-manager/META-INF/context.xml /usr/local/tomcat/webapps/host-manager/META-INF
# RUN grep -q "<!-- <Valve" ./webapps/host-manager/META-INF

EXPOSE 8080 8000

CMD ["catalina.sh", "jpda", "run"]
