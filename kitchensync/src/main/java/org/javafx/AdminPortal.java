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

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;
import software.amazon.awssdk.services.iam.model.IamException;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminPortal {

    private static void deleteRecipe(DynamoDbClient dynamoClient, String tableName, String recipeId) {
        System.out.println("\n=== Delete Recipe ===");
        try {
            Map<String, AttributeValue> keyToDelete = Map.of(
                "Recipe", AttributeValue.builder().s(recipeId).build()
            );
    
            dynamoClient.deleteItem(builder -> builder
                .tableName(tableName)
                .key(keyToDelete)
            );
    
            System.out.println("Recipe with ID '" + recipeId + "' deleted successfully.");
            System.out.println("Sending message to user: ");
    
        } catch (DynamoDbException e) {
            System.err.println("Error deleting recipe: " + e.getMessage());
        }
    }
    

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the accessKeyId: ");
        String userID = scanner.nextLine().trim();
        System.out.print("Enter the secretAccessKey: ");
        String userKey = scanner.nextLine().trim();

        

        // Hardcoded AWS credentials (ONLY FOR TESTING)
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create( userID, userKey);

        // Create S3, DynamoDB, and IAM clients with credentials
        S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        DynamoDbClient dynamoClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        IamClient iamClient = IamClient.builder()
                .region(Region.AWS_GLOBAL)  // IAM uses AWS_GLOBAL region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();


        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== ADMIN MENU ===");
            System.out.println("1) List S3 buckets");
            System.out.println("2) List DynamoDB items");
            System.out.println("3) List IAM Users");
            System.out.println("4) Delete a Recipe from DynamoDB");
            System.out.println("5) Exit");
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
                    listIamUsers(iamClient);
                    break;
                case "4":
                    System.out.print("Enter DynamoDB table name: ");
                    String deleteTable = scanner.nextLine().trim();
                    System.out.print("Enter Recipe ID to delete: ");
                    String recipeId = scanner.nextLine().trim();
                    deleteRecipe(dynamoClient, deleteTable, recipeId);
                    break;
                case "5":
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
        iamClient.close();
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

    private static void listIamUsers(IamClient iamClient) {
        System.out.println("\n=== IAM Users ===");
        try {
            ListUsersResponse response = iamClient.listUsers();
            for (User user : response.users()) {
                System.out.println("Username: " + user.userName() +
                                   " | User ID: " + user.userId() +
                                   " | ARN: " + user.arn());
            }
        } catch (IamException e) {
            System.err.println("Error listing IAM users: " + e.getMessage());
        }
    }
}
