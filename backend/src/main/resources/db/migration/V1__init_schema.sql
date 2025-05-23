CREATE TABLE IF NOT EXISTS constructors
(
  id             BIGSERIAL PRIMARY KEY,
  constructor_id VARCHAR(255),
  name           VARCHAR(255) NOT NULL,
  nationality    VARCHAR(255)
  );

CREATE TABLE IF NOT EXISTS drivers
(
  id               BIGSERIAL PRIMARY KEY,
  driver_id        VARCHAR(255) UNIQUE,
  permanent_number VARCHAR(10),
  code             VARCHAR(5),
  given_name       VARCHAR(255) NOT NULL,
  family_name      VARCHAR(255) NOT NULL,
  date_of_birth    VARCHAR(20),
  nationality      VARCHAR(255)
  );

CREATE TABLE IF NOT EXISTS season_champions
(
  id             BIGSERIAL PRIMARY KEY,
  season         VARCHAR(255) NOT NULL UNIQUE,
  driver_id      VARCHAR(255) REFERENCES drivers (driver_id),
  constructor_id BIGINT REFERENCES constructors (id)
  );

CREATE TABLE IF NOT EXISTS race_winners
(
  id             BIGSERIAL PRIMARY KEY,
  season         VARCHAR(255) NOT NULL,
  round          VARCHAR(255) NOT NULL,
  driver_id      VARCHAR(255) REFERENCES drivers (driver_id),
  constructor_id BIGINT REFERENCES constructors (id),
  time           VARCHAR(255)
  );
