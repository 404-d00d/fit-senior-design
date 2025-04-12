package org.javafx;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Exception;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminPortal {

    public static void main(String[] args) {
        // Hardcoded AWS credentials (ONLY FOR TESTING)
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
            "AKIAS6J7QGOOS2VSJQNP",
            "RpYmWXTZAk4k33zL/tQYUDP/x+L7403SYAjwSx9Y"
        );

        // Create S3 and DynamoDB clients with credentials
        S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1)  // Change region if needed
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)  // Same region as your DynamoDB table
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== ADMIN MENU ===");
            System.out.println("1) List S3 buckets");
            System.out.println("2) List DynamoDB items");
            System.out.println("3) Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listS3Buckets(s3Client);
                    break;
                case "2":
                    System.out.print("Enter DynamoDB table name: ");
                    String tableName = scanner.nextLine().trim();
                    listDynamoDbItems(dynamoClient, tableName);
                    break;
                case "3":
                    running = false;
                    System.out.println("Exiting Admin Portal...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
        s3Client.close();
        dynamoClient.close();
    }

    private static void listS3Buckets(S3Client s3Client) {
        System.out.println("\n=== S3 Buckets ===");
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            for (Bucket bucket : response.buckets()) {
                System.out.println("Bucket Name: " + bucket.name() +
                                   " | Created On: " + bucket.creationDate());
            }
        } catch (S3Exception e) {
            System.err.println("Error listing S3 buckets: " + e.getMessage());
        }
    }

    private static void listDynamoDbItems(DynamoDbClient dynamoClient, String tableName) {
        System.out.println("\n=== DynamoDB Items ===");
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                                                 .tableName(tableName)
                                                 .build();
            ScanResponse response = dynamoClient.scan(scanRequest);

            for (Map<String, AttributeValue> item : response.items()) {
                System.out.println("--- Item ---");
                for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
        } catch (DynamoDbException e) {
            System.err.println("Error scanning DynamoDB table: " + e.getMessage());
        }
    }
}
