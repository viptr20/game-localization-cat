-- db/seed.sql
-- Schema + initial data for catdb

-- Create database if not exists and switch to it
CREATE DATABASE IF NOT EXISTS catdb;
USE catdb;

-- Drop tables in dependency order to make the file idempotent for dev
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS segments;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) USERS
CREATE TABLE users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed users
INSERT INTO users (id, username, password, full_name, role) VALUES
  (1, 'admin', 'admin', 'Admin User', 'ADMIN'),
  (2, 'pm_jane', 'password', 'Jane ProjectManager', 'PM'),
  (3, 'translator_tom', 'password', 'Tom Translator', 'TRANSLATOR');

-- 2) PROJECTS
CREATE TABLE projects (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    owner_id     INT NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed projects
INSERT INTO projects (id, name, description, owner_id, status) VALUES
  (1, 'RPG UI Localization',  'UI strings for fantasy RPG', 2, 'ACTIVE'),
  (2, 'Mobile Gacha EN > BG', 'Live ops event batch 1',      2, 'ACTIVE'),
  (3, 'Finance App Help',     'Help center v1',              1, 'ARCHIVED');

-- 3) SEGMENTS
CREATE TABLE segments (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    project_id    INT NOT NULL,
    segment_index INT NOT NULL,
    source_text   TEXT NOT NULL,
    target_text   TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'NEW',
    assigned_to   INT,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    UNIQUE KEY uk_project_segment_idx (project_id, segment_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed segments
INSERT INTO segments
  (id, project_id, segment_index, source_text, target_text, status, assigned_to)
VALUES
  (1, 1, 1, 'Start Game',              NULL,            'NEW',       3),
  (2, 1, 2, 'Load Game',               'Зареди игра',   'TRANSLATED',3),
  (3, 1, 3, 'Options',                 NULL,            'IN_PROGRESS',3),

  (4, 2, 1, 'Limited-time banner',     NULL,            'NEW',       3),
  (5, 2, 2, '10x summon',             '10 призовавания', 'TRANSLATED',3),

  (6, 3, 1, 'Welcome to the help',     NULL,            'NEW',       NULL),
  (7, 3, 2, 'Getting started',         NULL,            'NEW',       NULL);

-- Quick sanity checks (optional; safe to leave in dev)
SELECT 'Users:' AS section;
SELECT * FROM users;

SELECT 'Projects:' AS section;
SELECT id, name, status FROM projects;

SELECT 'Segments:' AS section;
SELECT project_id, segment_index, status FROM segments;