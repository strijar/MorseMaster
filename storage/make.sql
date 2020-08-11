.separator "\t"

CREATE TABLE codes_group (
    id		INT PRIMARY KEY,
    info	TEXT
);

.import codes_group.cvs codes_group

---

CREATE TABLE codes (
    group_id	INT,
    symbol		TEXT PRIMARY KEY,
    code		TEXT
);

.import codes.cvs codes

---

CREATE TABLE stat (
    symbol		TEXT PRIMARY KEY,
    correct		INT,
    mistake		INT,
    lastseen	INT
);

---

CREATE TABLE opts (
    name	TEXT PRIMARY KEY ON CONFLICT REPLACE,
    val		TEXT
);

.import opts.cvs opts

---

CREATE TABLE lession (
    info	TEXT,
    symbols	TEXT
);

.import lession.cvs lession
