package com.mobilebanking.shared.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify database schema correctness.
 * Tests that all required tables, columns, and constraints exist.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DatabaseSchemaTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testUserTableExists() throws SQLException {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet tables = metaData.getTables(null, null, "USERS", null);

        assertTrue(tables.next(), "Users table should exist");

        // Check columns
        List<String> columns = getTableColumns("USERS");
        assertTrue(columns.contains("ID"), "Users table should have ID column");
        assertTrue(columns.contains("NAME"), "Users table should have NAME column");
        assertTrue(columns.contains("PHONE"), "Users table should have PHONE column");
        assertTrue(columns.contains("PIN_HASH"), "Users table should have PIN_HASH column");
        assertTrue(columns.contains("BALANCE"), "Users table should have BALANCE column");
        assertTrue(columns.contains("CREATED_AT"), "Users table should have CREATED_AT column");
        assertTrue(columns.contains("UPDATED_AT"), "Users table should have UPDATED_AT column");

        // Check indexes
        List<String> indexes = getTableIndexes("USERS");
        assertTrue(indexes.contains("IDX_USERS_PHONE"), "Users table should have phone index");

        // Check constraints
        assertTrue(hasUniqueConstraint("USERS", "PHONE"),
                "Users table should have unique constraint on phone column");
    }

    @Test
    void testTransactionTableExists() throws SQLException {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet tables = metaData.getTables(null, null, "TRANSACTIONS", null);

        assertTrue(tables.next(), "Transactions table should exist");

        // Check columns
        List<String> columns = getTableColumns("TRANSACTIONS");
        assertTrue(columns.contains("ID"), "Transactions table should have ID column");
        assertTrue(columns.contains("SENDER_ID"), "Transactions table should have SENDER_ID column");
        assertTrue(columns.contains("RECEIVER_ID"), "Transactions table should have RECEIVER_ID column");
        assertTrue(columns.contains("AMOUNT"), "Transactions table should have AMOUNT column");
        assertTrue(columns.contains("TRANSACTION_TYPE"), "Transactions table should have TRANSACTION_TYPE column");
        assertTrue(columns.contains("TIMESTAMP"), "Transactions table should have TIMESTAMP column");

        // Check indexes
        List<String> indexes = getTableIndexes("TRANSACTIONS");
        assertTrue(indexes.contains("IDX_TRANSACTIONS_SENDER"), "Transactions table should have sender index");
        assertTrue(indexes.contains("IDX_TRANSACTIONS_RECEIVER"), "Transactions table should have receiver index");
        assertTrue(indexes.contains("IDX_TRANSACTIONS_TIMESTAMP"), "Transactions table should have timestamp index");

        // Check foreign keys
        assertTrue(hasForeignKey("TRANSACTIONS", "SENDER_ID", "USERS", "ID"),
                "Transactions table should have foreign key from SENDER_ID to USERS.ID");
        assertTrue(hasForeignKey("TRANSACTIONS", "RECEIVER_ID", "USERS", "ID"),
                "Transactions table should have foreign key from RECEIVER_ID to USERS.ID");
    }

    @Test
    void testAuditLogsTableExists() throws SQLException {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet tables = metaData.getTables(null, null, "AUDIT_LOGS", null);

        assertTrue(tables.next(), "Audit logs table should exist");

        // Check columns
        List<String> columns = getTableColumns("AUDIT_LOGS");
        assertTrue(columns.contains("ID"), "Audit logs table should have ID column");
        assertTrue(columns.contains("USER_ID"), "Audit logs table should have USER_ID column");
        assertTrue(columns.contains("ACTION_TYPE"), "Audit logs table should have ACTION_TYPE column");
        assertTrue(columns.contains("ENTITY_TYPE"), "Audit logs table should have ENTITY_TYPE column");
        assertTrue(columns.contains("ENTITY_ID"), "Audit logs table should have ENTITY_ID column");
        assertTrue(columns.contains("DETAILS"), "Audit logs table should have DETAILS column");
        assertTrue(columns.contains("IP_ADDRESS"), "Audit logs table should have IP_ADDRESS column");
        assertTrue(columns.contains("CREATED_AT"), "Audit logs table should have CREATED_AT column");

        // Check indexes
        List<String> indexes = getTableIndexes("AUDIT_LOGS");
        assertTrue(indexes.contains("IDX_AUDIT_LOGS_USER"), "Audit logs table should have user index");
        assertTrue(indexes.contains("IDX_AUDIT_LOGS_ACTION"), "Audit logs table should have action index");
        assertTrue(indexes.contains("IDX_AUDIT_LOGS_ENTITY"), "Audit logs table should have entity index");

        // Check foreign keys
        assertTrue(hasForeignKey("AUDIT_LOGS", "USER_ID", "USERS", "ID"),
                "Audit logs table should have foreign key from USER_ID to USERS.ID");
    }

    // Helper methods
    private List<String> getTableColumns(String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        ResultSet rs = dataSource.getConnection().getMetaData().getColumns(null, null, tableName, null);
        while (rs.next()) {
            columns.add(rs.getString("COLUMN_NAME"));
        }
        return columns;
    }

    private List<String> getTableIndexes(String tableName) throws SQLException {
        List<String> indexes = new ArrayList<>();
        ResultSet rs = dataSource.getConnection().getMetaData().getIndexInfo(null, null, tableName, false, false);
        while (rs.next()) {
            String indexName = rs.getString("INDEX_NAME");
            if (indexName != null && !indexName.startsWith("PRIMARY")) {
                indexes.add(indexName);
            }
        }
        return indexes;
    }

    private boolean hasUniqueConstraint(String tableName, String columnName) throws SQLException {
        ResultSet rs = dataSource.getConnection().getMetaData().getIndexInfo(null, null, tableName, true, false);
        while (rs.next()) {
            String indexColumn = rs.getString("COLUMN_NAME");
            boolean nonUnique = rs.getBoolean("NON_UNIQUE");
            if (columnName.equals(indexColumn) && !nonUnique) {
                return true;
            }
        }
        return false;
    }

    private boolean hasForeignKey(String tableName, String columnName, String pkTable, String pkColumn)
            throws SQLException {
        ResultSet rs = dataSource.getConnection().getMetaData().getImportedKeys(null, null, tableName);
        while (rs.next()) {
            String fkColumnName = rs.getString("FKCOLUMN_NAME");
            String pkTableName = rs.getString("PKTABLE_NAME");
            String pkColumnName = rs.getString("PKCOLUMN_NAME");
            if (columnName.equals(fkColumnName) && pkTable.equals(pkTableName) && pkColumn.equals(pkColumnName)) {
                return true;
            }
        }
        return false;
    }
}