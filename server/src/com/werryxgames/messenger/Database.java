package com.werryxgames.messenger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
  To make database works, install mariadb-server and mariadb-client.
  Setup database.
  Run mariadb-server (by writing "mysqld" and pressing "Enter").
  Run mariadb-client (by writing "mysql" and pressing "Enter").
  (If you already have a database and want to fully reset it, erasing all accounts and messages,
   write:
    DROP DATABASE messenger;
   press "Enter" and write SQL query from "Write:" section)
  Write:
    CREATE DATABASE IF NOT EXISTS messenger;
    USE messenger;
    CREATE TABLE IF NOT EXISTS accounts (
      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
      login VARCHAR(64) NOT NULL,
      passwordHash CHAR(32) NOT NULL,
      passwordSalt CHAR(8) NOT NULL,
      PRIMARY KEY (id)
    );
    CREATE TABLE IF NOT EXISTS privateMessages (
      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
      sender BIGINT UNSIGNED NOT NULL,
      receiver BIGINT UNSIGNED NOT NULL,
      text TEXT NOT NULL,
      PRIMARY KEY (id)
    );
    \q
  Press "Enter"
 */

public class Database {
  public Server server;
  public Connection connection;

  public Database(Server server, String url, String user, String password) throws SQLException {
    this.server = server;
    this.connection = DriverManager.getConnection("jdbc:mariadb://" + url, user, password);
  }

  public PreparedStatement prepare(String defaultStatement, Object... replacements) {
    PreparedStatement statement;

    try {
      statement = this.connection.prepareStatement(defaultStatement);
    } catch (SQLException e) {
      this.server.logException(e);
      return null;
    }

    int replacementsLength = replacements.length;

    for (int i = 0; i < replacementsLength; i++) {
      Object obj = replacements[i];

      if (obj instanceof Long) {
        try {
          statement.setLong(i + 1, (int) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof Integer) {
        try {
          statement.setInt(i + 1, (int) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof Short) {
        try {
          statement.setShort(i + 1, (short) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof Byte) {
        try {
          statement.setByte(i + 1, (byte) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof byte[]) {
        try {
          statement.setBytes(i + 1, (byte[]) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof String) {
        try {
          statement.setString(i + 1, (String) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else if (obj instanceof Boolean) {
        try {
          statement.setBoolean(i + 1, (boolean) obj);
        } catch (SQLException e) {
          this.server.logException(e);
          return null;
        }
      } else {
        this.server.logger.severe("Unknown type passed, aborting.");
        return null;
      }
    }

    return statement;
  }

  public int update(String update, Object... replacements) {
    PreparedStatement statement = this.prepare(update, replacements);

    if (statement == null) {
      return -1;
    }

    try {
      return statement.executeUpdate();
    } catch (SQLException e) {
      this.server.logException(e);
    }

    return -1;
  }

  public ResultSet query(String query, Object... replacements) {
    PreparedStatement statement = this.prepare(query, replacements);

    if (statement == null) {
      return null;
    }

    try {
      return statement.executeQuery();
    } catch (SQLException e) {
      this.server.logException(e);
    }

    return null;
  }

  public void close() {
    try {
      this.connection.close();
    } catch (SQLException e) {
      this.server.logException(e);
    }
  }
}
