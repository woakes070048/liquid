CREATE DATABASE IF NOT EXISTS liquid_user DEFAULT CHARACTER SET = utf8;

GRANT CREATE, DROP, ALTER, INDEX, INSERT, DELETE, UPDATE, SELECT, LOCK TABLES ON liquid_user.* TO 'liquid'@'localhost';