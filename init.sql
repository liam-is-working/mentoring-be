GRANT ALL PRIVILEGES ON DATABASE postgres TO liam;

CREATE table accounts
(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email           varchar(50) UNIQUE NOT NULL,
    firebase_uuid    varchar(50) UNIQUE NOT NULL,
    is_mentor        boolean
);

CREATE TYPE gender AS ENUM ('male', 'female', 'unknown');

create table user_profiles
(
    account_id   uuid PRIMARY KEY REFERENCES Accounts (id),
    full_name    varchar(255),
    description text,
    dob         varchar(255),
    gender      varchar(255),
    avatar_url   varchar(255),
    cover_url    varchar(255)
)