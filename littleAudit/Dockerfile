FROM public.ecr.aws/lambda/java:11
# See https://gallery.ecr.aws/lambda/java
#     https://hub.docker.com/r/amazon/aws-lambda-java
#     https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
#

RUN yum install tar -y

# Copy function code and runtime dependencies from Gradle layout
COPY build/distributions/ ${LAMBDA_TASK_ROOT}/
RUN cd ${LAMBDA_TASK_ROOT} \
    && FOLDER="$(ls -t | grep -e '.tar$' | tail -1 | sed 's/.tar$//')" \
    && tar xvf "${FOLDER}.tar" \
    && mv ${FOLDER}/* . \
    && rmdir ${FOLDER} \
    && rm *.tar *.zip

#
# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
# Test with:
#    curl -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{}' 
#  https://docs.aws.amazon.com/lambda/latest/dg/images-test.html
#
CMD [ "littleware.cloudmgr.service.lambda.SessionHandler::handleRequest" ]
