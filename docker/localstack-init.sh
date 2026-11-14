#!/bin/bash
# Runs inside LocalStack once it is ready: creates the S3 bucket and SQS queue the fraud-service uses.
set -e
awslocal s3 mb s3://payguard-decision-archive --region eu-west-2
awslocal sqs create-queue --queue-name payment-review-queue --region eu-west-2
