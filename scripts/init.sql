CREATE DATABASE bankdash_auth;
CREATE DATABASE bankdash_accounts;
CREATE DATABASE bankdash_transactions;

GRANT ALL PRIVILEGES ON DATABASE bankdash_auth TO bankdash;
GRANT ALL PRIVILEGES ON DATABASE bankdash_accounts TO bankdash;
GRANT ALL PRIVILEGES ON DATABASE bankdash_transactions TO bankdash;