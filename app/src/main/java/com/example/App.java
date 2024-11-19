/*
 * This source file was generated by the Gradle 'init' task
 */
package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());

        // Verify AWS credentials are accessible
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        try {
            credentialsProvider.resolveCredentials();
            System.out.println("AWS credentials are accessible.");
        } catch (Exception e) {
            System.err.println("Failed to access AWS credentials.");
            e.printStackTrace();
            return; // Exit if credentials are not accessible
        }

        // Enable detailed logging
        System.setProperty("software.amazon.jdbc.log.level", "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        try {
            // Load environment variables
            String hostname = System.getenv("RDS_HOSTNAME");
            String port = System.getenv("RDS_PORT");
            String database = System.getenv("RDS_DATABASE");
            String username = System.getenv("RDS_USERNAME");
            String region = System.getenv("AWS_REGION");

            // Print environment variables for debugging
            System.out.println("Environment Variables:");
            System.out.println("Hostname: " + hostname);
            System.out.println("Port: " + port);
            System.out.println("Database: " + database);
            System.out.println("Username: " + username);
            System.out.println("AWS_REGION: " + region);

            // Set the AWS region if not set via environment variables
            if (region == null || region.isEmpty()) {
                region = "us-east-1"; // Default region
                System.setProperty("aws.region", region);
            }

            // Build JDBC URL with IAM authentication plugin
            String jdbcUrl = String.format(
                "jdbc:aws-wrapper:postgresql://%s:%s/%s?wrapperPlugins=iam",
                hostname, port, database
            );

            // Set up connection properties
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("ssl", "true");
            props.setProperty("sslmode", "require");

            // Load AWS JDBC Driver
            Class.forName("software.amazon.jdbc.Driver");

            // Establish the connection
            System.out.println("Attempting to connect to the database...");
            Connection conn = DriverManager.getConnection(jdbcUrl, props);
            System.out.println("Connected successfully!");

            // Perform a test query
            ResultSet rs = conn.createStatement().executeQuery("SELECT current_database(), current_user");
            if (rs.next()) {
                System.out.println("Connected to database: " + rs.getString(1));
                System.out.println("As user: " + rs.getString(2));
            }

            conn.close();
        } catch (Exception e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
}