// Seed data for ClientCredentials
// This script initializes the database with test client credentials for development
//
// Test Credentials:
// - Client ID: 123456, Secret: secret123
// - Client ID: 789012, Secret: password456

db.client_credentials.insertMany([
  {
    _id: 123456,
    secretHash: "$2a$10$qDlzt7o3NEtdB7WfPvyeY.LSeynBZPnlValRRDUuyxVKw8ZbsxbNG",
    active: true,
    createdAt: ISODate("2025-01-06T00:00:00Z"),
    lastUsedAt: null
  },
  {
    _id: 789012,
    secretHash: "$2a$10$PvVlGs8Rq/H2qHR1tznyq.niJNIW54jJ71pOcE3IXIx4F9MT/wVKK",
    active: true,
    createdAt: ISODate("2025-01-06T00:00:00Z"),
    lastUsedAt: null
  }
]);

print("Client credentials seed data inserted successfully");
print("Total credentials: " + db.client_credentials.countDocuments());
