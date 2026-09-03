package com.example.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootApplication
public class TodoappApplication {

	private final DataSource dataSource;

	public TodoappApplication(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@PostConstruct
	public void ensurePinnedColumn() throws SQLException {
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet columns = statement.executeQuery(
					"SELECT COUNT(*) FROM information_schema.columns "
							+ "WHERE table_schema = DATABASE() AND table_name = 'todos' "
							+ "AND column_name = 'pinned'")) {
			columns.next();
			if (columns.getInt(1) == 0) {
				statement.executeUpdate(
						"ALTER TABLE todos ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE AFTER due_date");
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(TodoappApplication.class, args);
	}

}
