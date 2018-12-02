FROM anapsix/alpine-java:8
MAINTAINER cmdhitman@gmail.com
COPY build/libs/image-upload-1.0.0.jar /opt/image-upload/lib/
ENTRYPOINT ["java"]
CMD ["-jar", "/opt/image-upload/lib/image-upload-1.0.0.jar"]
EXPOSE 8080
