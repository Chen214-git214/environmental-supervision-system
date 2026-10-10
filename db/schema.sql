CREATE DATABASE IF NOT EXISTS neusoft_environment_supervision DEFAULT CHARACTER SET utf8mb4;
USE neusoft_environment_supervision;

CREATE TABLE IF NOT EXISTS aqi_level (
  level_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  level_name VARCHAR(20) NOT NULL,
  min_aqi INT NOT NULL,
  max_aqi INT NOT NULL,
  health_effect VARCHAR(255),
  suggestion VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS grid_province (
  province_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  province_code VARCHAR(20) NOT NULL UNIQUE,
  province_name VARCHAR(50) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS grid_city (
  city_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  province_id BIGINT NOT NULL,
  city_code VARCHAR(20) NOT NULL UNIQUE,
  city_name VARCHAR(50) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public_supervisor (
  supervisor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL UNIQUE,
  id_card VARCHAR(30),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public_feedback (
  feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  supervisor_id BIGINT NOT NULL,
  city_id BIGINT NOT NULL,
  level_id BIGINT,
  pollution_type VARCHAR(30) NOT NULL,
  address VARCHAR(200) NOT NULL,
  longitude DECIMAL(10,6),
  latitude DECIMAL(10,6),
  description TEXT NOT NULL,
  image_url VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 0,
  assigned_grid_member_id BIGINT,
  assigned_grid_member_name VARCHAR(50),
  assignment_type VARCHAR(20),
  assigned_at DATETIME,
  so2_value DECIMAL(10,2),
  co_value DECIMAL(10,2),
  pm25_value DECIMAL(10,2),
  so2_level VARCHAR(20),
  co_level VARCHAR(20),
  pm25_level VARCHAR(20),
  aqi_level VARCHAR(20),
  measured_at DATETIME,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS grid_member (
  grid_member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_no VARCHAR(30) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  province_id BIGINT NOT NULL,
  province_name VARCHAR(50) NOT NULL,
  city_id BIGINT NOT NULL,
  city_name VARCHAR(50) NOT NULL,
  grid_name VARCHAR(80) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
