FROM public.ecr.aws/lambda/java:11
# See https://gallery.ecr.aws/lambda/java
#     https://hub.docker.com/r/amazon/aws-lambda-java
#     https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
#

# Copy function code and runtime dependencies from Gradle layout
COPY target/pack ${LAMBDA_TASK_ROOT}

#
# Set the CMD to your handler (could also be done as a parameter override outside of the Dockerfile)
# See devTest.md for testing instructions.
#
CMD [ "littleware.cloudmgr.service.lambda.SessionLambda::handleRequest" ]
