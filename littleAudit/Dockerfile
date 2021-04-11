FROM public.ecr.aws/lambda/java:11
# See https://gallery.ecr.aws/lambda/java
#     https://hub.docker.com/r/amazon/aws-lambda-java
#     https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
#

RUN yum install tar -y

# Copy function code and runtime dependencies from Gradle layout
COPY build/distributions/littleAudit-2.6.0-SNAPSHOT.tar ${LAMBDA_TASK_ROOT}
RUN cd ${LAMBDA_TASK_ROOT} \
    && tar xvf littleAudit-2.6.0-SNAPSHOT.tar \
    && mv littleAudit-2.6.0-SNAPSHOT/* . \
    && rmdir littleAudit-2.6.0-SNAPSHOT \
    && rm littleAudit-2.6.0-SNAPSHOT.tar

#
# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
# Test with:
#    curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{}' 
#  https://docs.aws.amazon.com/lambda/latest/dg/images-test.html
#
CMD [ "littleware.cloudmgr.service.lambda.SessionHandler::handleRequest" ]
