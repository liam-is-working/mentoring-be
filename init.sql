create table departments
(
    id SERIAL PRIMARY KEY,
    name varchar(255),
    created_at timestamp
)

CREATE table accounts
(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email           varchar(50) UNIQUE NOT NULL,
    firebase_uuid    varchar(50) UNIQUE,
    role            varchar(8) NOT NULL,
    is_authenticated boolean DEFAULT false,
    department_id int REFERENCES departments(id),
    status varchar(15) default 'WAITING'
);

create table user_profiles
(
    account_id   uuid PRIMARY KEY REFERENCES accounts (id),
    full_name    varchar(255),
    description text,
    dob         varchar(255),
    gender      varchar(6),
    avatar_url   varchar(255),
    cover_url    varchar(255),
    cv          json
);

create table seminars
(
    id              SERIAL PRIMARY KEY,
    name            varchar(255),
    start_time       timestamp,
    description     text,
    location        varchar(100),
    image_url varchar(100),
    attachment_url varchar(500),
    department_id int references departments(id)
)

create table seminar_feedbacks
(
    id              SERIAL PRIMARY KEY,
    seminar_id int references seminars(id),
    content json
)

create table seminars_mentors
(
    seminar_id int references seminars(id),
    user_profile_id uuid references user_profiles(account_id),
    primary key (seminar_id,user_profile_id)
)

select *
from seminars s
         left join seminars_mentors sm on (sm.seminar_id = s.id)
         left join user_profiles up on (up.account_id = sm.user_profile_id)
where start_time between '2023-05-01 01:00:00' ::timestamp and '2023-05-31 01:00:00' ::timestamp

select count(*)
from seminars s
where start_time between '2023-05-01 01:00:00'::timestamp and '2023-05-31 01:00:00'::timestamp

select *,
       abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now())) as diffHours
from seminars s
         left join seminars_mentors sm on (sm.seminar_id = s.id)
         left join user_profiles up on (up.account_id = sm.user_profile_id)
where start_time between '2000-05-01 01:00:00'::timestamp and '2024-05-31 01:00:00'::timestamp and up.full_name like '%%'
order by diffHours

SELECT DATE_PART('year', '2012-01-01'::date) - DATE_PART('year', '2011-10-02'::date);
SELECT '2011-12-31 01:00:00'::timestamp - '2012-09-17 23:00:00'::timestamp;
SELECT abs(DATE_PART('day', '2011-12-31 01:00:00'::timestamp - '2014-12-31 01:00:00'::timestamp));


drop table seminars_mentors
drop table seminars
drop table accounts
drop table user_profiles
drop table departments

select s.id, s.name, s.start_time, abs(DATE_PART('day',s.start_time - now())*24 + DATE_PART('hour',s.start_time - now())) as datediff
from seminars s
         left join seminars_mentors sm on (sm.seminar_id = s.id)
         left join user_profiles up on (up.account_id = sm.user_profile_id)
where start_time between '1990-12-31 01:00:00' ::timestamp and '2030-12-31 01:00:00' ::timestamp
  and (up.full_name like '%%' or s.name like '')
GROUP BY(s.id)
order by datediff