-- =====================================================
-- SignalForge — V1 Initial Schema
-- PostgreSQL Database Migration
-- =====================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Monitored URLs table
CREATE TABLE IF NOT EXISTS monitored_url (
    id            SERIAL PRIMARY KEY,
    url           VARCHAR(2048) NOT NULL,
    name          VARCHAR(255),
    last_status   VARCHAR(50),
    last_checked  TIMESTAMP,
    response_time BIGINT,
    user_id       INTEGER REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_monitored_url_user_id ON monitored_url(user_id);
CREATE INDEX idx_monitored_url_status ON monitored_url(last_status);

-- Alert History table
CREATE TABLE IF NOT EXISTS alert_history (
    id            BIGSERIAL PRIMARY KEY,
    monitor_id    INTEGER NOT NULL REFERENCES monitored_url(id) ON DELETE CASCADE,
    event_type    VARCHAR(20) NOT NULL,
    status_code   VARCHAR(50),
    response_time BIGINT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified      BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_alert_history_monitor_id ON alert_history(monitor_id);
CREATE INDEX idx_alert_history_created_at ON alert_history(created_at DESC);
