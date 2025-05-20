CREATE TABLE constructors (
                              constructor_id VARCHAR(255) PRIMARY KEY,
                              name VARCHAR(255) NOT NULL,
                              nationality VARCHAR(255)
);

CREATE TABLE drivers (
                         driver_id VARCHAR(255) PRIMARY KEY,
                         permanent_number VARCHAR(10),
                         code VARCHAR(5),
                         given_name VARCHAR(255) NOT NULL,
                         family_name VARCHAR(255) NOT NULL,
                         date_of_birth VARCHAR(20),
                         nationality VARCHAR(255)
);

CREATE TABLE season_champions (
                                  season VARCHAR(255) PRIMARY KEY,
                                  driver_id VARCHAR(255) REFERENCES drivers(driver_id),
                                  constructor_id VARCHAR(255) REFERENCES constructors(constructor_id)
);

CREATE TABLE race_winners (
                              id BIGSERIAL PRIMARY KEY,
                              season VARCHAR(255) NOT NULL,
                              round VARCHAR(255) NOT NULL,
                              driver_id VARCHAR(255) REFERENCES drivers(driver_id),
                              constructor_id VARCHAR(255) REFERENCES constructors(constructor_id),
                              time VARCHAR(255)
);
