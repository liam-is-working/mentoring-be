CREATE table accounts
(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email           varchar(50) UNIQUE NOT NULL,
    firebase_uuid    varchar(50) UNIQUE,
    role            varchar(8) NOT NULL,
    is_authenticated boolean DEFAULT false
);

create table user_profiles
(
    account_id   uuid PRIMARY KEY REFERENCES Accounts (id),
    full_name    varchar(255),
    description text,
    dob         varchar(255),
    gender      varchar(6),
    avatar_url   varchar(255),
    cover_url    varchar(255),
    cv          json
)

CREATE table topics
(
    id              serial PRIMARY KEY,
    description varchar(255),
    mentor_account_id uuid REFERENCES user_profiles(account_id)
);

CREATE table topics_topic_fields
(
    topic_id int REFERENCES topics(id),
    topic_field_id smallint REFERENCES topic_fields(id),
    PRIMARY KEY (topic_id, topic_field_id)
);

CREATE table topics_topic_categories
(
    topic_id int REFERENCES topics(id),
    topic_category_id smallint REFERENCES topic_categories(id),
    PRIMARY KEY (topic_id, topic_category_id)
);

CREATE table topic_fields
(
    id              smallserial PRIMARY KEY,
    description varchar(255),
    enabled boolean not null default true
);

CREATE table topic_categories
(
    id              smallserial PRIMARY KEY,
    description varchar(255),
    enabled boolean not null default true
);

drop table accounts
drop table topics
drop table user_profiles
drop table topic_fields
drop table topic_categories
drop table topics_topic_categories
drop table topics_topic_fields